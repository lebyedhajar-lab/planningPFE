package algorithm;

import model.*;
import java.util.ArrayList;
import java.util.List;

public class DistributionJuryAlgorithm {

    private List<Soutenance> soutenances = new ArrayList<>();

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

        List<Enseignant> informaticiens = new ArrayList<>();
        for (Enseignant ens : candidats) {
            if (ens.getSpecialite().equalsIgnoreCase("informatique"))
                informaticiens.add(ens);
        }
        if (informaticiens.size() < 2)
            throw new IllegalStateException(
                "Pas assez de profs informatique pour : "
                + e.getNom() + " " + e.getPrenom());

        Enseignant info1 = enseignantLePlusDispo(informaticiens);
        informaticiens.remove(info1);
        candidats.remove(info1);

        Enseignant info2 = enseignantLePlusDispo(informaticiens);
        candidats.remove(info2);

        Enseignant troisieme;
        if (langue != null && langue.equalsIgnoreCase("anglais")) {
            List<Enseignant> anglophones = new ArrayList<>();
            for (Enseignant ens : candidats) {
                if ("Anglais".equalsIgnoreCase(ens.getSpecialite()))
                    anglophones.add(ens);
            }
            if (anglophones.isEmpty())
                throw new IllegalStateException(
                    "Pas de prof anglophone pour : "
                    + e.getNom() + " " + e.getPrenom());
            troisieme = enseignantLePlusDispo(anglophones);
        } else {
            troisieme = enseignantLePlusDispo(candidats);
        }

        if (troisieme == null)
            throw new IllegalStateException(
                "Pas assez d'enseignants pour : "
                + e.getNom() + " " + e.getPrenom());

        encadrant.incrementerSoutenances();
        info1.incrementerSoutenances();
        info2.incrementerSoutenances();
        troisieme.incrementerSoutenances();

        List<Enseignant> membres = new ArrayList<>();
        membres.add(info1);
        membres.add(info2);
        membres.add(troisieme);

        return new Jury(soutenances.size(), encadrant, membres);
    }

    public List<Soutenance> distribuer(List<Etudiant> etudiants,
                                       List<Enseignant> enseignants,
                                       List<Creneau> creneaux,
                                       List<Salle> salles,
                                       int dureeMin,
                                       ContrainteValidator validator) {

        java.util.Collections.shuffle(etudiants);

        int capaciteTotale = creneaux.size() * salles.size();
        if (etudiants.size() > capaciteTotale)
            throw new IllegalStateException(
                "Créneaux insuffisants : " + capaciteTotale
                + " pour " + etudiants.size() + " étudiants.");

        if (salles.isEmpty())
            throw new IllegalStateException("Aucune salle disponible.");

        int i = 1;
        for (Etudiant e : etudiants) {
            Creneau creneau = creneaux.get(i / salles.size());
            Salle   salle   = salles.get(i % salles.size());
            String  langue  = e.getLangue();
            Jury    jury    = formerJury(e, enseignants, langue, creneau, validator);

            Soutenance s = new Soutenance(i, langue, dureeMin, e, salle, creneau, jury);
            soutenances.add(s);
            i++;
        }
        return soutenances;
    }
}