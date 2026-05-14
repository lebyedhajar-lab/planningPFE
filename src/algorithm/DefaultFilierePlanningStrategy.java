package algorithm;

import model.*;
import java.util.List;

public class DefaultFilierePlanningStrategy implements FilierePlanningStrategy {

    private final ContrainteValidator validator;

    public DefaultFilierePlanningStrategy(ContrainteValidator validator) {
        this.validator = validator;
    }

    @Override
    public List<Soutenance> genererPlanning(List<Etudiant> etudiants,
                                             List<Enseignant> enseignants,
                                             List<Salle> salles,
                                             List<Creneau> creneaux) {
        DistributionJuryAlgorithm algo = new DistributionJuryAlgorithm();
        return algo.distribuer(etudiants, enseignants, creneaux,
                               salles, 30, validator);
    }
}