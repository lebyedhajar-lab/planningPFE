package algorithm;

import model.*;
import java.util.ArrayList;
import java.util.List;

public class DistributionJuryAlgorithm {

    private List<Soutenance> soutenances = new ArrayList<>();

    // ── Calculer la charge actuelle d'un enseignant ───────────
    public int calculerCharge(Enseignant enseignant) {
        int compteur = 0;
        for (Soutenance s : soutenances) {
            if (s.getJury().contientEnseignant(enseignant))
                compteur++;
        }
        return compteur;
    }

    // ── Enseignant le moins chargé parmi les candidats ────────
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

    // ── Former un jury pour un étudiant ───────────────────────
    public Jury formerJury(Etudiant e, List<Enseignant> enseignants, String langue, Creneau creneau,ContrainteValidator validator) {

        Enseignant encadrant = e.getEncadrant();

        // Filtrer : exclure encadrant + indisponibles
        List<Enseignant> candidats = new ArrayList<>();
        for (Enseignant ens : enseignants) {
            if (ens.getId() == encadrant.getId()) continue;
            if (!validator.enseignantDisponible(ens, creneau)) continue;
            candidats.add(ens);
        }

        // Contrainte : au moins 2 informaticiens
        List<Enseignant> informaticiens = new ArrayList<>();
        for (Enseignant ens : candidats) {
            if (ens.getSpecialite().equalsIgnoreCase("informatique"))
                informaticiens.add(ens);
        }
        if (informaticiens.size() < 2)
            throw new IllegalStateException(
                "Pas assez de profs informatique disponibles pour : "
                + e.getNom() + " " + e.getPrenom());

        // Choisir les 2 informaticiens les moins chargés
        Enseignant info1 = enseignantLePlusDispo(informaticiens);
        informaticiens.remove(info1);
        candidats.remove(info1);

        Enseignant info2 = enseignantLePlusDispo(informaticiens);
        candidats.remove(info2);

        // 3ème membre : anglophone si soutenance en anglais
        Enseignant troisieme;
        /*if (langue.equalsIgnoreCase("anglais")) {
            List<Enseignant> anglophones = new ArrayList<>();
            for (Enseignant ens : candidats) {
                if (ens.isAnglophone()) anglophones.add(ens);
            }
            if (anglophones.isEmpty())
                throw new IllegalStateException(
                    "Pas de prof anglophone disponible pour : "
                    + e.getNom() + " " + e.getPrenom());
            troisieme = enseignantLePlusDispo(anglophones);
        } else {
            troisieme = enseignantLePlusDispo(candidats);
        }*/
        if (langue.equalsIgnoreCase("anglais")) {
            List<Enseignant> anglophones = new ArrayList<>();
            for (Enseignant ens : candidats) {
                if ("Anglais".equalsIgnoreCase(ens.getSpecialite()))
                    anglophones.add(ens);
            }
            if (anglophones.isEmpty())
                throw new IllegalStateException(
                    "Pas de prof anglophone disponible pour : "
                    + e.getNom() + " " + e.getPrenom());
            troisieme = enseignantLePlusDispo(anglophones);
        } else {
            troisieme = enseignantLePlusDispo(candidats);
        }

        if (troisieme == null)
            throw new IllegalStateException(
                "Pas assez d'enseignants disponibles pour : "
                + e.getNom() + " " + e.getPrenom());

        // Incrémenter les charges
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

    // ── Distribuer tous les jurys ─────────────────────────────
    public List<Soutenance> distribuer(List<Etudiant> etudiants,
                                       List<Enseignant> enseignants,
                                       List<Creneau> creneaux,
                                       List<Salle> salles,
                                       int dureeMin,
                                       ContrainteValidator validator) {

        if (etudiants.size() > creneaux.size())
            throw new IllegalStateException(
                "Créneaux insuffisants : " + creneaux.size()
                + " pour " + etudiants.size() + " étudiants.");

        if (salles.isEmpty())
            throw new IllegalStateException("Aucune salle disponible.");

        int i = 0;
        for (Etudiant e : etudiants) {
            Creneau creneau = creneaux.get(i);
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