package algorithm;

import model.Enseignant;
import model.Etudiant;
import java.util.List;

public class EncadrantAffectationService {

    public void affecter(List<Etudiant> etudiants, List<Enseignant> enseignants) {
        for (Etudiant etudiant : etudiants) {
            if (etudiant.getEncadrant() != null) continue; // déjà affecté

            // Trouver l'enseignant le moins chargé
            Enseignant meilleur = null;
            int minCharge = Integer.MAX_VALUE;

            for (Enseignant ens : enseignants) {
                if (ens.getNbSoutenance() < minCharge) {
                    minCharge = ens.getNbSoutenance();
                    meilleur = ens;
                }
            }

            if (meilleur == null)
                throw new IllegalStateException(
                    "Aucun enseignant disponible pour encadrer : "
                    + etudiant.getNom());

            etudiant.setEncadrant(meilleur);
            meilleur.incrementerSoutenances();
        }
    }
}