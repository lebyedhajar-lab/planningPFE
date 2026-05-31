package algorithm;

import Config.ConfigPlanning;
import model.*;
import repository.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PlanningGenerator {

    private final ConfigPlanning          configPlanning;
    private final ContrainteValidator     validator;
    private final FilierePlanningStrategy strategy;
    private final EtudiantRepository      etudiantRepo;
    private final EnseignantRepository    enseignantRepo;
    private final SalleRepository         salleRepo;
    private final SoutenanceRepository    soutenanceRepo;

    public PlanningGenerator(
            ConfigPlanning          configPlanning,
            ContrainteValidator     validator,
            FilierePlanningStrategy strategy,
            EtudiantRepository      etudiantRepo,
            EnseignantRepository    enseignantRepo,
            SalleRepository         salleRepo,
            SoutenanceRepository    soutenanceRepo) {

        this.configPlanning = configPlanning;
        this.validator      = validator;
        this.strategy       = strategy;
        this.etudiantRepo   = etudiantRepo;
        this.enseignantRepo = enseignantRepo;
        this.salleRepo      = salleRepo;
        this.soutenanceRepo = soutenanceRepo;
    }

    public List<Soutenance> generer() {
        List<Etudiant>   etudiants   = etudiantRepo.chargerTous();
        java.util.Collections.shuffle(etudiants);
        List<Enseignant> enseignants = enseignantRepo.chargerTous();
        List<Salle>      salles      = salleRepo.chargerDisponibles();
        List<Creneau>    creneaux    = genererCreneaux();

        int capaciteTotale = creneaux.size() * salles.size();

        System.out.println("Etudiants   : " + etudiants.size());
        System.out.println("Jours       : " + configPlanning.getNbJoursSoutenances());
        System.out.println("Par jour    : " + configPlanning.nbCreneauxParJour());
        System.out.println("Creneaux    : " + creneaux.size());
        System.out.println("Salles      : " + salles.size());
        System.out.println("Capacite    : " + capaciteTotale);

        if (!strategy.estRealisable(
                etudiants.size(), capaciteTotale,
                enseignants.size()))
            throw new IllegalStateException(
                "Planning impossible : " + etudiants.size()
                + " étudiants pour " + capaciteTotale + " places.");

        List<Soutenance> soutenances =
            strategy.genererPlanning(
                etudiants, enseignants, salles, creneaux);

        for (Soutenance s : soutenances)
            soutenanceRepo.sauvegarder(s);

        return soutenances;
    }

    private List<Creneau> genererCreneaux() {
        List<Creneau> creneaux = new ArrayList<>();
        int id = 0;

        LocalDate dateFin = configPlanning.getDateDebut()
            .plusDays(configPlanning.getNbJoursSoutenances() - 1L);

        LocalDate jour = configPlanning.getDateDebut();
        while (!jour.isAfter(dateFin)) {
            for (LocalTime heure : calculerHeuresJournee()) {
                LocalTime heureFin =
                    configPlanning.calculerHeureFin(heure);
                creneaux.add(new Creneau(
                    id++, jour, heure, heureFin, true));
            }
            jour = jour.plusDays(1);
        }
        return creneaux;
    }

    private List<LocalTime> calculerHeuresJournee() {
        List<LocalTime> heures = new ArrayList<>();
        LocalTime heure = configPlanning.getHeureDebutJournee();
        while (configPlanning.estDansLaJournee(heure)) {
            heures.add(heure);
            heure = heure.plusMinutes(
                configPlanning.getDureeSoutenanceMin());
        }
        return heures;
    }

    public void afficherRapport(List<Soutenance> soutenances) {
        System.out.println("=== Rapport Planning ===");
        System.out.println("Periode : "
            + configPlanning.getDateDebut()
            + " -> "
            + configPlanning.getDateDebut()
              .plusDays(configPlanning.getNbJoursSoutenances() - 1L));
        System.out.println("Total   : "
            + soutenances.size() + " soutenance(s)");
        System.out.println("========================");

        for (Soutenance s : soutenances) {
            Creneau c = s.getCreneau();
            System.out.printf(
                "  [%02d] %-25s | %s  %s-%s | Salle %-6s | Encadrant: %s%n",
                s.getId(),
                s.getEtudiant().getNom()
                    + " " + s.getEtudiant().getPrenom(),
                c.getDateJour(),
                c.getHeureDebut(),
                c.getHeureFin(),
                s.getSalle().getNom(),
                s.getJury().getEncadrant().getNom());
        }
    }
}
