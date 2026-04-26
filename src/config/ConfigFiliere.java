package config;

import model.Filiere;
import java.time.LocalDate;

public class ConfigFiliere {

    private final Filiere  filiere;
    private int            nbJoursSoutenances;
    private LocalDate      dateDebut;

    public ConfigFiliere(Filiere filiere, int nbJoursSoutenances, LocalDate dateDebut) {
        if (filiere == null)
            throw new IllegalArgumentException("La filière ne peut pas être nulle.");
        if (nbJoursSoutenances <= 0)
            throw new IllegalArgumentException("Le nombre de jours doit être positif.");
        if (dateDebut == null)
            throw new IllegalArgumentException("La date de début ne peut pas être nulle.");

        this.filiere            = filiere;
        this.nbJoursSoutenances = nbJoursSoutenances;
        this.dateDebut          = dateDebut;
    }

    public Filiere getFiliere() {
        return filiere;
    }

    public int getNbJoursSoutenances() {
        return nbJoursSoutenances;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateDebut.plusDays(nbJoursSoutenances - 1L);
    }

    public int getNbSoutenancesTotal() {
        return filiere.getNbEtudiants();
    }

    public double getMoyenneSoutenancesParJour() {
        return (double) filiere.getNbEtudiants() / nbJoursSoutenances;
    }
    
    public void setNbJoursSoutenances(int nbJoursSoutenances) {
        if (nbJoursSoutenances <= 0)
            throw new IllegalArgumentException("Le nombre de jours doit être positif.");
        this.nbJoursSoutenances = nbJoursSoutenances;
    }

    public void setDateDebut(LocalDate dateDebut) {
        if (dateDebut == null)
            throw new IllegalArgumentException("La date de début ne peut pas être nulle.");
        this.dateDebut = dateDebut;
    }

    public boolean dateDansPeriode(LocalDate date) {
        return !date.isBefore(dateDebut) && !date.isAfter(getDateFin());
    }

    public String toString() {
        return String.format(
            "ConfigFiliere{filiere=%s, nbJours=%d, du=%s au=%s, nbEtudiants=%d}",
            filiere.getNom(), nbJoursSoutenances, dateDebut, getDateFin(), filiere.getNbEtudiants()
        );
    }
}
