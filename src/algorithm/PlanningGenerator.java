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

    private final EtudiantRepository   etudiantRepo;
    private final EnseignantRepository enseignantRepo;
    private final SalleRepository      salleRepo;
    private final SoutenanceRepository soutenanceRepo;

    public PlanningGenerator(
            ConfigPlanning configPlanning,
            ContrainteValidator validator,
            FilierePlanningStrategy strategy,
            EtudiantRepository etudiantRepo,
            EnseignantRepository enseignantRepo,
            SalleRepository salleRepo,
            SoutenanceRepository soutenanceRepo) {
        this.configPlanning = configPlanning;
        this.validator      = validator;
        this.strategy       = strategy;
        this.etudiantRepo   = etudiantRepo;
        this.enseignantRepo = enseignantRepo;
        this.salleRepo      = salleRepo;
        this.soutenanceRepo = soutenanceRepo;
    }

    // ─── Générer le planning complet 
    public List<Soutenance> generer() {
        List<Etudiant>   etudiants   = etudiantRepo.chargerTous();
        List<Enseignant> enseignants = enseignantRepo.chargerTous();
        List<Salle>      salles      = salleRepo.chargerTous();
        List<Creneau>    creneaux    = genererCreneaux();

        int nbJours  = configPlanning.getNbJoursSoutenances();
        int parJour  = (int) Math.ceil(
            (double) etudiants.size() / nbJours);
        int capacite = creneaux.size() * salles.size();

        // ── Affichage infos 
        System.out.println("Etudiants   : " + etudiants.size());
        System.out.println("Jours       : " + nbJours);
        System.out.println("Par jour    : " + parJour);
        System.out.println("Creneaux    : " + creneaux.size());
        System.out.println("Salles      : " + salles.size());
        System.out.println("Capacite    : " + capacite);

        // ── Vérification capacité globale 
        if (capacite < etudiants.size())
            throw new IllegalStateException(
                "Creneaux insuffisants : " + capacite
                + " places pour " + etudiants.size()
                + " etudiants.");

        // ── Répartition équitable par jour ───────────────────────
        List<Soutenance> touteSoutenances = new ArrayList<>();
        int idxEtudiant = 0;
        LocalDate debut = configPlanning.getDateDebut();

        for (int j = 0; j < nbJours; j++) {
            if (idxEtudiant >= etudiants.size()) break;

            LocalDate jour = debut.plusDays(j);

            // Étudiants de ce jour
            int fin = Math.min(
                idxEtudiant + parJour,
                etudiants.size());
            List<Etudiant> etudiantsJour = new ArrayList<>(
                etudiants.subList(idxEtudiant, fin));

            // Créneaux de ce jour
            List<Creneau> creneauxJour = new ArrayList<>();
            for (Creneau c : creneaux) {
                if (c.getDateJour().equals(jour))
                    creneauxJour.add(c);
            }

            // ── Vérification capacité du jour ────────────────────
            int capaciteJour = creneauxJour.size() * salles.size();
            if (capaciteJour < etudiantsJour.size())
                throw new IllegalStateException(
                    "Jour " + jour
                    + " : capacite insuffisante : "
                    + capaciteJour + " places pour "
                    + etudiantsJour.size() + " etudiants.");

            System.out.println("Jour " + (j + 1)
                + " (" + jour + ") : "
                + etudiantsJour.size() + " etudiants / "
                + creneauxJour.size() + " creneaux / "
                + capaciteJour + " places");

            // Générer les soutenances de ce jour
            List<Soutenance> soutenancesJour =
                strategy.genererPlanning(
                    etudiantsJour, enseignants,
                    salles, creneauxJour);

            touteSoutenances.addAll(soutenancesJour);
            idxEtudiant = fin;
        }
        int id = 1;
        for (Soutenance s : touteSoutenances) {
            s.setId(id++);
        }

        // ── Sauvegarder ──────────────────────────────────────────
        for (Soutenance s : touteSoutenances)
            soutenanceRepo.sauvegarder(s);

        System.out.println("Total soutenances : "
            + touteSoutenances.size());
        return touteSoutenances;
    }

    // ─── Générer tous les créneaux ───────────────────────────────
    private List<Creneau> genererCreneaux() {
        List<Creneau> creneaux = new ArrayList<>();
        int id = 0;

        LocalDate dateFin = configPlanning.getDateDebut()
            .plusDays(configPlanning.getNbJoursSoutenances() - 1L);

        LocalDate jour = configPlanning.getDateDebut();
        while (!jour.isAfter(dateFin)) {

            // Matin : heureDebut → heureDebutPause
            LocalTime heure = configPlanning.getHeureDebutJournee();
            while (true) {
                LocalTime fin = heure.plusMinutes(
                    configPlanning.getDureeSoutenanceMin());
                if (fin.isAfter(configPlanning.getHeureDebutPause()))
                    break;
                creneaux.add(new Creneau(
                    id++, jour, heure, fin, true));
                heure = fin;
            }

            // Après-midi : heureFinPause → heureFinJournee
            heure = configPlanning.getHeureFinPause();
            while (true) {
                LocalTime fin = heure.plusMinutes(
                    configPlanning.getDureeSoutenanceMin());
                if (fin.isAfter(configPlanning.getHeureFinJournee()))
                    break;
                creneaux.add(new Creneau(
                    id++, jour, heure, fin, true));
                heure = fin;
            }

            jour = jour.plusDays(1);
        }
        return creneaux;
    }

    // ─── Afficher le rapport ─────────────────────────────────────
    public void afficherRapport(List<Soutenance> soutenances) {
        System.out.println("=== Rapport Planning ===");
        System.out.println("Periode : "
            + configPlanning.getDateDebut()
            + " -> "
            + configPlanning.getDateDebut()
              .plusDays(configPlanning
                  .getNbJoursSoutenances() - 1L));
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