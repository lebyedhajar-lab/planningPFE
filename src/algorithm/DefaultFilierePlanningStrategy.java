package algorithm;

import Config.ConfigPlanning;
import model.*;
import java.util.List;

public class DefaultFilierePlanningStrategy
        implements FilierePlanningStrategy {

    private final ContrainteValidator validator;
    private final int                 dureeMin;
    private final ConfigPlanning      config;

    public DefaultFilierePlanningStrategy(
            ContrainteValidator validator,
            int dureeMin,
            ConfigPlanning config) {
        this.validator = validator;
        this.dureeMin  = dureeMin;
        this.config    = config;
    }

    @Override
    public List<Soutenance> genererPlanning(
            List<Etudiant>   etudiants,
            List<Enseignant> enseignants,
            List<Salle>      salles,
            List<Creneau>    creneaux) {
        DistributionJuryAlgorithm algo =
            new DistributionJuryAlgorithm(config);
        return algo.distribuer(etudiants, enseignants,
                               creneaux, salles,
                               dureeMin, validator);
    }
}