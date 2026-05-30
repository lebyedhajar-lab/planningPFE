package algorithm;

import model.Enseignant;
import model.Etudiant;
import java.util.List;

public class EncadrantAffectationService {

    public void affecter(List<Etudiant> etudiants,
                         List<Enseignant> enseignants) {

        for (Etudiant etudiant : etudiants) {
            if (etudiant.getEncadrant() != null) continue;

            // Trouver l'enseignant avec le moins d'étudiants encadrés
            Enseignant meilleur = null;
            int minCharge = Integer.MAX_VALUE;

            for (Enseignant ens : enseignants) {
                int charge = compterEtudiantsEncadres(ens, etudiants);
                if (charge < minCharge) {
                    minCharge = charge;
                    meilleur  = ens;
                }
            }

            if (meilleur == null)
                throw new IllegalStateException(
                    "Aucun enseignant disponible pour : "
                    + etudiant.getNom());

            etudiant.setEncadrant(meilleur);
           
        }
    }

    // ── Compter les étudiants déjà encadrés par cet enseignant ──
    private int compterEtudiantsEncadres(Enseignant ens,
                                          List<Etudiant> etudiants) {
        int count = 0;
        for (Etudiant e : etudiants) {
            if (e.getEncadrant() != null
                && e.getEncadrant().getId() == ens.getId())
                count++;
        }
        return count;
    }
}