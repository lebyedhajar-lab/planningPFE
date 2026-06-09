package Config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ConfigPlanning {

    private int       dureeSoutenanceMin;
    private LocalTime heureDebutJournee;
    private LocalTime heureFinJournee;
    private LocalTime heureDebutPause;
    private LocalTime heureFinPause;
    private int       pauseMinimale;
    private int       minSoutenanceParProfParJour;
    private int       maxSoutenanceParProfParJour;
    private int       nbMembresJury;
    private List<LocalDate> joursDisponibles;
    private int       nbJoursSoutenances;
    private LocalDate dateDebut;
    
    // ── Constructeur vide ─────────────────────────────────────────
    // Les valeurs sont injectées par ExcelConfigLoader
    public ConfigPlanning() {
        this.joursDisponibles = new ArrayList<>();
    }

    // ── Getters ───────────────────────────────────────────────────
    public int       getDureeSoutenanceMin()         { return dureeSoutenanceMin; }
    public LocalTime getHeureDebutJournee()          { return heureDebutJournee; }
    public LocalTime getHeureFinJournee()            { return heureFinJournee; }
    public LocalTime getHeureDebutPause()            { return heureDebutPause; }
    public LocalTime getHeureFinPause()              { return heureFinPause; }
    public int       getPauseMinimale()              { return pauseMinimale; }
    public int       getMinSoutenanceParProfParJour(){ return minSoutenanceParProfParJour; }
    public int       getMaxSoutenanceParProfParJour(){ return maxSoutenanceParProfParJour; }
    public int       getNbMembresJury()              { return nbMembresJury; }
    public List<LocalDate> getJoursDisponibles()     { return joursDisponibles; }
    public int       getNbJoursSoutenances() { return nbJoursSoutenances; }
    public LocalDate getDateDebut()          { return dateDebut; }

    // ── Setters (appelés uniquement par ExcelConfigLoader) ────────
    public void setDureeSoutenanceMin(int d)                  { this.dureeSoutenanceMin = d; }
    public void setHeureDebutJournee(LocalTime h)             { this.heureDebutJournee = h; }
    public void setHeureFinJournee(LocalTime h)               { this.heureFinJournee = h; }
    public void setHeureDebutPause(LocalTime h)               { this.heureDebutPause = h; }
    public void setHeureFinPause(LocalTime h)                 { this.heureFinPause = h; }
    public void setPauseMinimale(int p)                       { this.pauseMinimale = p; }
    public void setMinSoutenancesParProfParJour(int min)      { this.minSoutenanceParProfParJour = min; }
    public void setMaxSoutenancesParProfParJour(int max)      { this.maxSoutenanceParProfParJour = max; }
    public void setNbMembresJury(int nb)                      { this.nbMembresJury = nb; }
    public void setNbJoursSoutenances(int nb)     { this.nbJoursSoutenances = nb; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    // ── Méthodes utilitaires ──────────────────────────────────────
    public void ajouterJour(LocalDate jour){
        if (!joursDisponibles.contains(jour))
            joursDisponibles.add(jour);
    }

    public LocalTime calculerHeureFin(LocalTime heureDebut){
        return heureDebut.plusMinutes(dureeSoutenanceMin);
    }

    /** Écart minimum entre deux débuts de soutenance (aligné sur la vérification). */
    public int getEcartMinEntreSoutenances() {
        int ecart = pauseMinimale > 0 ? pauseMinimale : 60;
        return Math.max(ecart, 60);
    }

    /** Intervalle entre le début de deux créneaux consécutifs. */
    public int getIntervalEntreCreneaux() {
        return Math.max(dureeSoutenanceMin, getEcartMinEntreSoutenances());
    }

    /** Heures de début des créneaux d'une journée (matin + après-midi). */
    public List<LocalTime> genererHeuresCreneauxJournee() {
        List<LocalTime> heures = new ArrayList<>();
        LocalTime heure = heureDebutJournee;
        int interval = getIntervalEntreCreneaux();

        while (!heure.isAfter(heureFinJournee)) {
            if (estDansLaJournee(heure)) {
                heures.add(heure);
            }
            heure = heure.plusMinutes(interval);
            if (heure.isBefore(heureFinPause) && !heure.isBefore(heureDebutPause)) {
                heure = heureFinPause;
            }
        }
        return heures;
    }

    /*public boolean estDansLaJournee(LocalTime heureDebut) {
        LocalTime heureFin = calculerHeureFin(heureDebut);

        if (heureDebut.isBefore(heureDebutJournee) || heureFin.isAfter(heureFinJournee))
            return false;

        if (!heureDebut.isBefore(heureDebutPause) && heureDebut.isBefore(heureFinPause))
            return false;

        if (heureFin.isAfter(heureDebutPause) && !heureFin.isAfter(heureFinPause))
            return false;

        return true;
    }*/ 
    public boolean estDansLaJournee(LocalTime heureDebut) {
        LocalTime heureFin = calculerHeureFin(heureDebut);

        // Dépasse la journée
        if (heureDebut.isBefore(heureDebutJournee) || heureFin.isAfter(heureFinJournee))
            return false;

        // Chevauche la pause (début avant fin de pause ET fin après début de pause)
        if (heureDebut.isBefore(heureFinPause) && heureFin.isAfter(heureDebutPause))
            return false;

        return true;
    }

    public int nbCreneauxParJour() {
        return genererHeuresCreneauxJournee().size();
    }

    // ── Validation après chargement ───────────────────────────────
    // Appelé par ExcelConfigLoader après avoir tout injecté
    public void valider() {
        if (dureeSoutenanceMin <= 0)
            throw new IllegalStateException("dureeSoutenanceMin doit être positif.");
        if (nbMembresJury <= 0)
            throw new IllegalStateException("nbMembresJury doit être positif.");
        if (heureDebutJournee == null || heureFinJournee == null)
            throw new IllegalStateException("Les heures de journée ne sont pas définies.");
        if (heureDebutPause == null || heureFinPause == null)
            throw new IllegalStateException("Les heures de pause ne sont pas définies.");
        if (maxSoutenanceParProfParJour < minSoutenanceParProfParJour)
            throw new IllegalStateException("max doit être >= min pour soutenances par prof.");
        if (nbJoursSoutenances <= 0)
            throw new IllegalStateException("nbJoursSoutenances doit être positif.");
        if (dateDebut == null)
            throw new IllegalStateException("dateDebut ne peut pas être nulle.");
    }

    public String toString() {
        return "ConfigPlanning{"
            + "dureeSoutenance=" + dureeSoutenanceMin + "min"
            + ", journee=" + heureDebutJournee + "-" + heureFinJournee
            + ", pause=" + heureDebutPause + "-" + heureFinPause
            + ", min/maxParProf=" + minSoutenanceParProfParJour + "/" + maxSoutenanceParProfParJour
            + ", nbMembresJury=" + nbMembresJury
            + ", nbJours=" + joursDisponibles.size() + "}";
    }
}