package algorithm;

import Config.ConfigFiliere;
import Config.ConfigPlanning;
import model.*;
import repository.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PlanningGenerator {

    private final ConfigPlanning              configPlanning;
    private final ConfigFiliere               configFiliere;
    private final ContrainteValidator         validator;
    private final DistributionJuryAlgorithm   distributionJury;
    private final FilierePlanningStrategy     strategy;

    private final EtudiantRepository   etudiantRepo;
    private final EnseignantRepository enseignantRepo;
    private final SalleRepository      salleRepo;
    private final SoutenanceRepository soutenanceRepo;

    public PlanningGenerator(
            ConfigPlanning configPlanning,
            ConfigFiliere configFiliere,
            ContrainteValidator validator,
            FilierePlanningStrategy strategy,
            EtudiantRepository etudiantRepo,
            EnseignantRepository enseignantRepo,
            SalleRepository salleRepo,
            SoutenanceRepository soutenanceRepo) {

        this.configPlanning   = configPlanning;
        this.configFiliere    = configFiliere;
        this.validator        = validator;
        this.distributionJury = new DistributionJuryAlgorithm();
        this.strategy         = strategy;
        this.etudiantRepo     = etudiantRepo;
        this.enseignantRepo   = enseignantRepo;
        this.salleRepo        = salleRepo;
        this.soutenanceRepo   = soutenanceRepo;
    }

    public List<Soutenance> generer() {
        List<Etudiant>   etudiants   = etudiantRepo.chargerTous();
        List<Enseignant> enseignants = enseignantRepo.chargerTous();
        List<Salle>      salles      = salleRepo.chargerTous();

        List<Creneau> creneaux = genererCreneaux();

        int capaciteTotale = creneaux.size() * salles.size();

        if (!strategy.estRealisable(etudiants.size(), capaciteTotale, enseignants.size())) {
            throw new IllegalStateException(
                "Planning impossible : " + etudiants.size() + " étudiants pour seulement "
                + capaciteTotale + " places disponibles (" + creneaux.size()
                + " créneaux × " + salles.size() + " salles).");
        }

        List<Soutenance> soutenances = strategy.genererPlanning(etudiants, enseignants, salles, creneaux);

        for (Soutenance s : soutenances) {
            soutenanceRepo.sauvegarder(s);
        }

        return soutenances;
    }

    private List<Creneau> genererCreneaux() {
        List<Creneau> creneaux = new ArrayList<>();
        int id = 0;

        for (LocalDate jour : configPlanning.getJoursDisponibles()) {
            if (!configFiliere.dateDansPeriode(jour)) continue;

            for (LocalTime heure : calculerHeuresJournee()) {
                LocalTime heureFin = configPlanning.calculerHeureFin(heure);
                creneaux.add(new Creneau(id++, jour, heure, heureFin, true));
            }
        }

        return creneaux;
    }

    private List<LocalTime> calculerHeuresJournee() {
        List<LocalTime> heures = new ArrayList<>();
        LocalTime heure = configPlanning.getHeureDebutJournee();

        while (configPlanning.estDansLaJournee(heure)) {
            heures.add(heure);
            heure = heure.plusMinutes(configPlanning.getdureeSoutenanceMin());
        }

        return heures;
    }

    public void afficherRapport(List<Soutenance> soutenances) {
        System.out.println("=== Rapport Planning ===");
        System.out.println("Filière  : " + configFiliere.getFiliere().getNom());
        System.out.println("Période  : " + configFiliere.getDateDebut()
                           + " → " + configFiliere.getDateFin());
        System.out.println("Total    : " + soutenances.size()
                           + " / " + configFiliere.getNbSoutenancesTotal());
        System.out.printf ("Moy/jour : %.1f%n", configFiliere.getMoyenneSoutenancesParJour());
        System.out.println("========================");

        for (Soutenance s : soutenances) {
            Creneau c = s.getCreneau();
            System.out.printf("  [%02d] %-25s | %s  %s-%s | Salle %-6s | Encadrant: %s%n",
                s.getId(),
                s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom(),
                c.getDateJour(),
                c.getHeureDebut(),
                c.getHeureFin(),
                s.getSalle().getNom(),
                s.getJury().getEncadrant().getNom());
        }
    }
}