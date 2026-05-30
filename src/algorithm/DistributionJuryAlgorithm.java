package algorithm;

import Config.ConfigPlanning;
import model.*;
import java.util.ArrayList;
import java.util.List;

public class DistributionJuryAlgorithm {

    private List<Soutenance> soutenances = new ArrayList<>();
    private final int nbMembres;

    public DistributionJuryAlgorithm(ConfigPlanning config) {
        this.nbMembres = config.getNbMembresJury() - 1; // 2
    }

    public int calculerCharge(Enseignant enseignant) {
        int compteur = 0;
        for (Soutenance s : soutenances) {
            if (s.getJury().contientEnseignant(enseignant))
                compteur++;
        }
        return compteur;
    }

    public Enseignant enseignantLePlusDispo(
            List<Enseignant> candidats) {
        if (candidats == null || candidats.isEmpty()) return null;
        Enseignant meilleur  = candidats.get(0);
        int        minCharge = calculerCharge(meilleur);
        for (Enseignant e : candidats) {
            int charge = calculerCharge(e);
            if (charge < minCharge) {
                minCharge = charge;
                meilleur  = e;
            }
        }
        return meilleur;
    }

    public Jury formerJury(Etudiant e,
                           List<Enseignant> enseignants,
                           String langue,
                           Creneau creneau,
                           ContrainteValidator validator) {

        Enseignant encadrant = e.getEncadrant();

        // Filtrer : exclure encadrant + indisponibles
        List<Enseignant> candidats = new ArrayList<>();
        for (Enseignant ens : enseignants) {
            if (ens.getId() == encadrant.getId()) continue;
            if (!validator.enseignantDisponible(ens, creneau))
                continue;
            candidats.add(ens);
        }

        // Séparer informaticiens et tous les autres
        List<Enseignant> informaticiens = new ArrayList<>();
        List<Enseignant> tousLesAutres  = new ArrayList<>();
        for (Enseignant ens : candidats) {
            if (ens.getSpecialite()
                   .equalsIgnoreCase("informatique"))
                informaticiens.add(ens);
            else
                tousLesAutres.add(ens);
        }

        // Vérifier au moins 1 informaticien disponible
        if (informaticiens.isEmpty())
            throw new IllegalStateException(
                "Pas de prof informatique pour : "
                + e.getNom() + " " + e.getPrenom());

        List<Enseignant> membres = new ArrayList<>();

        // ── Membre 1 : informaticien le moins chargé ─────────
        Enseignant info1 = enseignantLePlusDispo(informaticiens);
        membres.add(info1);
        informaticiens.remove(info1);
        candidats.remove(info1);

        // ── Membre 2 ──────────────────────────────────────────
        Enseignant membre2;

        if (langue != null
                && langue.equalsIgnoreCase("anglais")) {
            // Soutenance anglais → anglophone obligatoire
            List<Enseignant> anglophones = new ArrayList<>();
            for (Enseignant ens : candidats) {
                if (ens.isAnglophone()) anglophones.add(ens);
            }
            if (anglophones.isEmpty())
                throw new IllegalStateException(
                    "Pas de prof anglophone pour : "
                    + e.getNom() + " " + e.getPrenom());
            membre2 = enseignantLePlusDispo(anglophones);
        } else {
            // Soutenance français →
            // le moins chargé parmi TOUS les candidats restants
            // (informaticiens + maths + gestion + anglais)
            membre2 = enseignantLePlusDispo(candidats);
        }

        if (membre2 == null)
            throw new IllegalStateException(
                "Membre 2 null pour : "
                + e.getNom() + " " + e.getPrenom());

        membres.add(membre2);

        // Incrémenter les charges
        encadrant.incrementerSoutenances();
        for (Enseignant m : membres)
            m.incrementerSoutenances();

        return new Jury(soutenances.size(), encadrant, membres);
    }

    public List<Soutenance> distribuer(
            List<Etudiant>      etudiants,
            List<Enseignant>    enseignants,
            List<Creneau>       creneaux,
            List<Salle>         salles,
            int                 dureeMin,
            ContrainteValidator validator) {

        if (salles.isEmpty())
            throw new IllegalStateException(
                "Aucune salle disponible.");

        // Filtrer étudiants sans encadrant
        List<Etudiant> etudiantsValides = new ArrayList<>();
        for (Etudiant et : etudiants) {
            if (et.getEncadrant() == null) {
                System.out.println(
                    "⚠️ Ignoré (encadrant null) : "
                    + et.getNom() + " " + et.getPrenom());
            } else {
                etudiantsValides.add(et);
            }
        }

        if (etudiantsValides.isEmpty())
            throw new IllegalStateException(
                "Aucun étudiant avec encadrant. "
                + "Vérifiez EncadrantAffectationService.");

        int capaciteTotale = creneaux.size() * salles.size();
        if (etudiantsValides.size() > capaciteTotale)
            throw new IllegalStateException(
                "Créneaux insuffisants : " + capaciteTotale
                + " pour " + etudiantsValides.size()
                + " étudiants.");

        // Distribuer
        int i = 0;
        for (Etudiant et : etudiantsValides) {
            Creneau creneau = creneaux.get(i / salles.size());
            Salle   salle   = salles.get(i % salles.size());
            String  langue  = et.getLangue();

            Jury jury = formerJury(
                et, enseignants, langue, creneau, validator);

            Soutenance s = new Soutenance(
                i, langue, dureeMin,
                et, salle, creneau, jury);

            soutenances.add(s);
            i++;
        }

        // Rapport
        System.out.println("\n=== Rapport Jury ===");
        System.out.println("Total : " + soutenances.size());
        for (Enseignant ens : enseignants) {
            int charge = calculerCharge(ens);
            if (charge > 0)
                System.out.println("  "
                    + ens.getNom() + " "
                    + ens.getPrenom()
                    + " [" + ens.getSpecialite() + "]"
                    + " → " + charge + " jury(s)");
        }
        System.out.println("====================");

        return soutenances;
    }
}