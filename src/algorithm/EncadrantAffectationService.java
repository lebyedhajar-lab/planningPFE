package algorithm;

import model.Enseignant;
import model.Etudiant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EncadrantAffectationService {

    public void affecter(List<Etudiant> etudiants,List<Enseignant> enseignants) {
    	

        List<Etudiant> sansEncadrant = new ArrayList<>();
        for (Etudiant etudiant : etudiants) {
            if (etudiant.getEncadrant() == null) {
                sansEncadrant.add(etudiant);
            }
        }

        while (!sansEncadrant.isEmpty()) {
            List<Enseignant> candidats = new ArrayList<>(enseignants);
            candidats.sort(Comparator.comparingInt(
                ens -> compterEtudiantsEncadres(ens, etudiants)));

            int minCharge = compterEtudiantsEncadres(candidats.get(0), etudiants);
            List<Enseignant> moinsCharges = new ArrayList<>();
            for (Enseignant ens : candidats) {
                if (compterEtudiantsEncadres(ens, etudiants) == minCharge) {
                    moinsCharges.add(ens);
                }
            }

            Collections.shuffle(moinsCharges);
            Enseignant meilleur = moinsCharges.get(0);
            Etudiant etudiant = sansEncadrant.remove(0);
            etudiant.setEncadrant(meilleur);
        }
    }

    private int compterEtudiantsEncadres(Enseignant ens, List<Etudiant> etudiants) {
        int count = 0;
        for (Etudiant e : etudiants) {
            if (e.getEncadrant() != null
                && e.getEncadrant().getId() == ens.getId())
                count++;
        }
        return count;
    }
}