package statistiques;

import model.*;
import repository.*;
import Config.ConfigPlanning;
import java.util.ArrayList;
import java.util.List;

public class DashboardService {

    private final SoutenanceRepository soutenanceRepo;
    private final EtudiantRepository   etudiantRepo;
    private final EnseignantRepository enseignantRepo;
    private final SalleRepository salleRepo;
    private final ConfigPlanning        config;


    private static final int TOLERANCE = 1;

    // ─── Constructeur ────────────────────────────────────────────
    public DashboardService(SoutenanceRepository soutenanceRepo,
                            EtudiantRepository   etudiantRepo,
                            EnseignantRepository enseignantRepo,
                            SalleRepository      salleRepo,
                            ConfigPlanning        config) {
        this.soutenanceRepo = soutenanceRepo;
        this.etudiantRepo   = etudiantRepo;
        this.enseignantRepo = enseignantRepo;
        this.salleRepo      = salleRepo;
        this.config         = config;

    }

    //
    // STATISTIQUES DE BASE
  

    // ─── Nb soutenances par prof ─────────────────────────────────
    public int nbSoutenancesParProf(Enseignant prof) {
        int count = 0;
        for (Soutenance s : soutenanceRepo.chargerTous()) {
            if (s.getJury() != null&& s.getJury().contientEnseignant(prof)) {
                    count++;
            }
        }
        return count;
    }

    // ─── Nb étudiants encadrés par prof ──────────────────────────
    public int nbEtudiantsParProf(Enseignant prof) {
        return etudiantRepo.trouverParEncadrant(prof.getId()).size();
    }

    // ─── Nb soutenances par filière ──────────────────────────────
    public int nbSoutenancesParFiliere(Filiere filiere) {
        int count = 0;
        for (Soutenance s : soutenanceRepo.chargerTous()) {
            if (s.getEtudiant() != null
                && s.getEtudiant().getFiliere() != null
                && s.getEtudiant().getFiliere().getID()
                   == filiere.getID()) {
                    count++;
            }
        }
        return count;
    }

    // ─── Récupérer filières depuis les étudiants ─────────────────
    // FiliereRepository supprimé ->on extrait les filières
    // directement depuis les étudiants
    public List<Filiere> getFilieres(){
        List<Filiere> filieres  = new ArrayList<>();
        List<Integer> idsVus    = new ArrayList<>();
        for (Etudiant e : etudiantRepo.chargerTous()) {
            if (e.getFiliere() != null
                && !idsVus.contains(e.getFiliere().getID())) {
                    filieres.add(e.getFiliere());
                    idsVus.add(e.getFiliere().getID());
            }
        }
        return filieres;
    }

    // ─── Totaux ──────────────────────────────────────────────────
    public int totalSoutenances() {
        return soutenanceRepo.chargerTous().size();
    }
    public int totalEtudiants() {
        return etudiantRepo.chargerTous().size();
    }
    public int totalProfs() {
        return enseignantRepo.chargerTous().size();
    }
    public int totalFilieres() {
        return getFilieres().size();
    }

    // ════════════════════════════════════════════════════════════
    // STATISTIQUES CONFIG — depuis ConfigPlanning
    // ════════════════════════════════════════════════════════════

    // ─── Nb créneaux par jour (matin + après-midi) ───────────────
    public int nbCreneauxParJour() {
        return config.nbCreneauxParJour();
    }

    // ─── Capacité totale du planning ─────────────────────────────
    public int capaciteTotale() {
        int nbSalles = salleRepo.chargerTous().size();
        return config.getNbJoursSoutenances()
             * config.nbCreneauxParJour()* nbSalles;
    }

    // ─── Taux de remplissage ─────────────────────────────────────
    // % de créneaux utilisés
    public double tauxRemplissage() {
        int capacite = capaciteTotale();
        if (capacite == 0) return 0;
        return (double) totalSoutenances() / capacite * 100;
    }

    // ─── Nb jours de soutenance ──────────────────────────────────
    public int nbJoursSoutenances() {
        return config.getNbJoursSoutenances();
    }

    // ─── Horaires journée ────────────────────────────────────────
    public String getHorairesJournee() {
        return config.getHeureDebutJournee()
             + " - " + config.getHeureDebutPause()
             + " | " + config.getHeureFinPause()
             + " - " + config.getHeureFinJournee();
    }

    // ════════════════════════════════════════════════════════════
    // EQUILIBRE
    // ════════════════════════════════════════════════════════════

    // ─── Moyenne soutenances par prof ────────────────────────────
    public double calculerMoyenneSoutenances() {
        List<Enseignant> profs = enseignantRepo.chargerTous();
        if (profs.isEmpty()) return 0;
        int totalParticipations = 0;
        for (Enseignant prof : profs) {
            totalParticipations += nbSoutenancesParProf(prof);
        }
        return (double) totalParticipations / profs.size();
    }

    // ─── Moyenne étudiants encadrés par prof ─────────────────────
    public double moyenneEtudiantsParProf() {
        List<Enseignant> profs = enseignantRepo.chargerTous();
        if (profs.isEmpty()) return 0;
        int total = 0;
        for (Enseignant prof : profs) {
            total += nbEtudiantsParProf(prof);
        }
        return (double) total / profs.size();
    }

    // ─── Ecart type ──────────────────────────────────────────────
    public double calculerEcartType() {
        List<Enseignant> profs = enseignantRepo.chargerTous();
        if (profs.isEmpty()) return 0;
        double moyenne = calculerMoyenneSoutenances();
        double somme   = 0;
        for (Enseignant prof : profs) {
            double ecart = nbSoutenancesParProf(prof) - moyenne;
            somme += ecart * ecart;
        }
        return Math.sqrt(somme / profs.size());
    }

    // ─── Ce prof est-il équilibré ? ──────────────────────────────
    public boolean estEquilibre(Enseignant prof) {
        double moyenne = calculerMoyenneSoutenances();
        int nb = nbSoutenancesParProf(prof);
        return nb >= (moyenne - TOLERANCE)
            && nb <= (moyenne + TOLERANCE);
    }

    // ─── Respect min/max par jour depuis config ───────────────────
    public boolean respecteMinParJour(Enseignant prof) {
        return nbSoutenancesParProf(prof)
            >= config.getMinSoutenanceParProfParJour();
    }

    public boolean respecteMaxParJour(Enseignant prof) {
        return nbSoutenancesParProf(prof)
            <= config.getMaxSoutenanceParProfParJour();
    }

    // ─── Profs surchargés ────────────────────────────────────────
    public List<Enseignant> profsSurcharges() {
        List<Enseignant> resultat = new ArrayList<>();
        double moyenne = calculerMoyenneSoutenances();
        for (Enseignant prof : enseignantRepo.chargerTous()) {
            if (nbSoutenancesParProf(prof)
                > moyenne + TOLERANCE) {
                    resultat.add(prof);
            }
        }
        return resultat;
    }

    // ─── Profs sous-chargés ──────────────────────────────────────
    public List<Enseignant> profsSousCharges() {
        List<Enseignant> resultat = new ArrayList<>();
        double moyenne = calculerMoyenneSoutenances();
        for (Enseignant prof : enseignantRepo.chargerTous()) {
            if (nbSoutenancesParProf(prof)
                < moyenne - TOLERANCE) {
                    resultat.add(prof);
            }
        }
        return resultat;
    }

    // ─── Planning globalement équilibré ? ────────────────────────
    public boolean estPlanningEquilibre() {
        for (Enseignant prof : enseignantRepo.chargerTous()) {
            if (!estEquilibre(prof)) return false;
        }
        return true;
    }

    // ════════════════════════════════════════════════════════════
    // EXTREMES
    // ════════════════════════════════════════════════════════════

    public Enseignant profLePlusCharge() {
        List<Enseignant> profs = enseignantRepo.chargerTous();
        if (profs.isEmpty()) return null;
        Enseignant max = profs.get(0);
        for (Enseignant prof : profs) {
            if (nbSoutenancesParProf(prof)
                > nbSoutenancesParProf(max))
                    max = prof;
        }
        return max;
    }

    public Enseignant profLeMoinsCharge() {
        List<Enseignant> profs = enseignantRepo.chargerTous();
        if (profs.isEmpty()) return null;
        Enseignant min = profs.get(0);
        for (Enseignant prof : profs) {
            if (nbSoutenancesParProf(prof)
                < nbSoutenancesParProf(min))
                    min = prof;
        }
        return min;
    }

    public Filiere filiereLaPlusChargee() {
        List<Filiere> filieres = getFilieres();
        if (filieres.isEmpty()) return null;
        Filiere max = filieres.get(0);
        for (Filiere f : filieres) {
            if (nbSoutenancesParFiliere(f)
                > nbSoutenancesParFiliere(max))
                    max = f;
        }
        return max;
    }

    // ════════════════════════════════════════════════════════════
    // RAPPORTS CONSOLE
    // ════════════════════════════════════════════════════════════

    public void afficherRapport() {
        System.out.println("============ DASHBOARD ============");

        // ── Infos générales ──
        System.out.println("Total soutenances     : "
            + totalSoutenances());
        System.out.println("Total étudiants       : "
            + totalEtudiants());
        System.out.println("Total profs           : "
            + totalProfs());
        System.out.println("Total filières        : "
            + totalFilieres());
        System.out.println("Nb jours soutenances  : "
            + nbJoursSoutenances());
        System.out.println("Horaires journée      : "
            + getHorairesJournee());
        System.out.println("Creneaux/jour         : "
            + nbCreneauxParJour());
        System.out.printf ("Capacite totale       : %d%n",
            capaciteTotale());
        System.out.printf ("Taux remplissage      : %.1f%%%n",
            tauxRemplissage());
        System.out.printf ("Moy. etudiants/prof   : %.1f%n",
            moyenneEtudiantsParProf());
        System.out.printf ("Moy. soutenances/prof : %.1f%n",
            calculerMoyenneSoutenances());

        // ── Par prof ──
        System.out.println("\n── Par professeur ──────────────────");
        for (Enseignant prof : enseignantRepo.chargerTous()) {
            String etat = estEquilibre(prof) ? "OK" : "!!";
            System.out.printf(
                "  [%s] %-20s | soutenances: %2d | etudiants: %2d%n",
                etat,
                prof.getNom() + " " + prof.getPrenom(),
                nbSoutenancesParProf(prof),
                nbEtudiantsParProf(prof));
        }

        // ── Par filière ──
        System.out.println("\n── Par filiere ─────────────────────");
        for (Filiere f : getFilieres()) {
            System.out.printf(
                "  %-20s | soutenances: %2d%n",
                f.getNom(),
                nbSoutenancesParFiliere(f));
        }

        // ── Equilibre ──
        afficherRapportEquilibre();
    }

    public void afficherRapportEquilibre() {
        double moyenne   = calculerMoyenneSoutenances();
        double ecartType = calculerEcartType();

        System.out.println("\n── Equilibre ───────────────────────");
        System.out.printf ("Moyenne        : %.1f soutenances%n",
            moyenne);
        System.out.printf ("Ecart type     : %.2f%n", ecartType);
        System.out.printf ("Intervalle OK  : [%.0f , %.0f]%n",
            moyenne - TOLERANCE, moyenne + TOLERANCE);
        System.out.printf ("Min config     : %d%n",
            config.getMinSoutenanceParProfParJour());
        System.out.printf ("Max config     : %d%n",
            config.getMaxSoutenanceParProfParJour());
        System.out.println("Equilibre      : "
            + (estPlanningEquilibre() ? "OUI" : "NON"));

        List<Enseignant> surcharges  = profsSurcharges();
        List<Enseignant> sousCharges = profsSousCharges();

        if (!surcharges.isEmpty()) {
            System.out.println("\nProfs surcharges :");
            for (Enseignant prof : surcharges) {
                System.out.printf(
                    "   -> %-20s : %d soutenances%n",
                    prof.getNom() + " " + prof.getPrenom(),
                    nbSoutenancesParProf(prof));
            }
        }

        if (!sousCharges.isEmpty()) {
            System.out.println("\nProfs sous-charges :");
            for (Enseignant prof : sousCharges) {
                System.out.printf(
                    "   -> %-20s : %d soutenances%n",
                    prof.getNom() + " " + prof.getPrenom(),
                    nbSoutenancesParProf(prof));
            }
        }

        Enseignant max = profLePlusCharge();
        Enseignant min = profLeMoinsCharge();
        if (max != null)
            System.out.println("\nPlus charge  : "
                + max.getNom()
                + " (" + nbSoutenancesParProf(max) + ")");
        if (min != null)
            System.out.println("Moins charge : "
                + min.getNom()
                + " (" + nbSoutenancesParProf(min) + ")");

        System.out.println("===================================");
    }
}