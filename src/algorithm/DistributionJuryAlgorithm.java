package algorithm;

import Config.ConfigPlanning;
import model.*;
import java.util.ArrayList;
import java.util.List;

public class DistributionJuryAlgorithm {

    private List<Soutenance> soutenances = new ArrayList<>();
    private final int nbMembres;

    public DistributionJuryAlgorithm(ConfigPlanning config) {
        this.nbMembres = config.getNbMembresJury() - 1;
    }

    public int calculerCharge(Enseignant enseignant) {
        int compteur = 0;
        for (Soutenance s : soutenances) {
            if (s.getJury().contientEnseignant(enseignant))
                compteur++;
        }
        return compteur;
    }

    public Enseignant enseignantLePlusDispo(List<Enseignant> candidats) {
        if (candidats == null || candidats.isEmpty()) return null;
        Enseignant meilleur = candidats.get(0);
        int minCharge = calculerCharge(meilleur);
        for (Enseignant e : candidats) {
            int charge = calculerCharge(e);
            if (charge < minCharge) {
                minCharge = charge;
                meilleur = e;
            }
        }
        return meilleur;
    }

    public Jury formerJury(Etudiant e, List<Enseignant> enseignants,
                           String langue, Creneau creneau,
                           ContrainteValidator validator) {

        Enseignant encadrant = e.getEncadrant();

        List<Enseignant> candidats = new ArrayList<>();
        for (Enseignant ens : enseignants) {
            if (ens.getId() == encadrant.getId()) continue;
            if (!validator.enseignantDisponible(ens, creneau)) continue;
            candidats.add(ens);
        }

        if (candidats.size() < nbMembres)
            throw new IllegalStateException(
                "Pas assez d'enseignants pour : "
                + e.getNom() + " (besoin: " + nbMembres
                + ", dispo: " + candidats.size() + ")");

        List<Enseignant> membres = new ArrayList<>();

        // Membre 1 : toujours un informaticien
        List<Enseignant> informaticiens = new ArrayList<>();
        for (Enseignant ens : candidats) {
            if (ens.getSpecialite().equalsIgnoreCase("informatique"))
                informaticiens.add(ens);
        }
        if (informaticiens.isEmpty())
            throw new IllegalStateException(
                "Pas de prof informatique pour : "
                + e.getNom() + " " + e.getPrenom());

        Enseignant membre1 = enseignantLePlusDispo(informaticiens);
        membres.add(membre1);
        candidats.remove(membre1);

        // Membres restants
        for (int i = 1; i < nbMembres; i++) {
            Enseignant membre;
            boolean dernierMembre = (i == nbMembres - 1);

            if (dernierMembre && langue != null
                    && langue.equalsIgnoreCase("anglais")) {
                List<Enseignant> anglophones = new ArrayList<>();
                for (Enseignant ens : candidats) {
                    if ("Anglais".equalsIgnoreCase(ens.getSpecialite()))
                        anglophones.add(ens);
                }
                if (anglophones.isEmpty())
                    throw new IllegalStateException(
                        "Pas de prof anglophone pour : "
                        + e.getNom() + " " + e.getPrenom());
                membre = enseignantLePlusDispo(anglophones);
            } else {
                membre = enseignantLePlusDispo(candidats);
            }

            if (membre == null)
                throw new IllegalStateException(
                    "Pas assez d'enseignants pour : "
                    + e.getNom() + " " + e.getPrenom());

            membres.add(membre);
            candidats.remove(membre);
        }

        return new Jury(soutenances.size(), encadrant, membres);
    }

    public List<Soutenance> distribuer(List<Etudiant> etudiants,
                                       List<Enseignant> enseignants,
                                       List<Creneau> creneaux,
                                       List<Salle> salles,
                                       int dureeMin,
                                       ContrainteValidator validator) {

        int capaciteTotale = creneaux.size() * salles.size();
        if (etudiants.size() > capaciteTotale)
            throw new IllegalStateException(
                "Créneaux insuffisants : " + capaciteTotale
                + " pour " + etudiants.size() + " étudiants.");

        if (salles.isEmpty())
            throw new IllegalStateException("Aucune salle disponible.");

        int i = 0;
        for (Etudiant e : etudiants) {
            Creneau creneau = creneaux.get(i / salles.size());
            Salle   salle   = salles.get(i % salles.size());
            String  langue  = e.getLangue();
            Jury    jury    = formerJury(e, enseignants,
                                        langue, creneau, validator);
            Soutenance s = new Soutenance(
                i, langue, dureeMin, e, salle, creneau, jury);
            soutenances.add(s);
            i++;
        }
        return soutenances;
    }
}