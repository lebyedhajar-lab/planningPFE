package algorithm;

import model.Creneau;
import model.Enseignant;
import model.Etudiant;
import model.Salle;
import model.Soutenance;

import java.util.List;

public interface FilierePlanningStrategy {

    List<Soutenance> genererPlanning(
            List<Etudiant>   etudiants,
            List<Enseignant> enseignants,
            List<Salle>      salles,
            List<Creneau>    creneaux
    );

    default String getNom() {
        return this.getClass().getSimpleName();
    }

    default boolean estRealisable(int nbEtudiants, int nbCreneaux, int nbEnseignants) {
        return nbCreneaux >= nbEtudiants && nbEnseignants >= 2;
    }
}
