package Config;

import model.Filiere;
import java.time.LocalDate;

public class ConfigFiliere {
    private Filiere filiere;
    private int nbJoursSoutenances;
    private LocalDate dateDebut;

    // ── Constructeur vide ─────────────────────────────────────────
    // Injecté par ExcelConfigLoader
    public ConfigFiliere() {}

    // ── Getters ───────────────────────────────────────────────────
    public Filiere   getFiliere()            { return filiere; }
    public int       getNbJoursSoutenances() { return nbJoursSoutenances; }
    public LocalDate getDateDebut()          { return dateDebut; }

    public LocalDate getDateFin() {
        return dateDebut.plusDays(nbJoursSoutenances - 1L);
    }
    public int getNbSoutenancesTotal() {
        return filiere.getNbEtudiants();
    }
    public double getMoyenneSoutenancesParJour() {
        return (double) filiere.getNbEtudiants() / nbJoursSoutenances;
    }
    
    // ── Setters (appelés uniquement par ExcelConfigLoader) ────────
    public void setFiliere(Filiere filiere)                  { this.filiere = filiere; }
    public void setNbJoursSoutenances(int nb)                { this.nbJoursSoutenances = nb; }
    public void setDateDebut(LocalDate dateDebut)            { this.dateDebut = dateDebut; }

    // ── Validation après chargement ───────────────────────────────
    public void valider() {
        if (filiere == null)
            throw new IllegalStateException("La filière ne peut pas être nulle.");
        if (nbJoursSoutenances <= 0)
            throw new IllegalStateException("Le nombre de jours doit être positif.");
        if (dateDebut == null)
            throw new IllegalStateException("La date de début ne peut pas être nulle.");
    }

    public boolean dateDansPeriode(LocalDate date) {
        return !date.isBefore(dateDebut) && !date.isAfter(getDateFin());
    }

    public String toString() {
        return String.format("ConfigFiliere{filiere=%s, nbJours=%d, du=%s au=%s, nbEtudiants=%d}",filiere.getNom(), nbJoursSoutenances, dateDebut,getDateFin(), filiere.getNbEtudiants());
    }
}