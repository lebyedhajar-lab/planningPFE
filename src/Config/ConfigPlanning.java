package Config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ConfigPlanning {
	private int dureeSoutenanceMin ;
	private LocalTime heureDebutJournee;
	private LocalTime heureFinJournee;
	private int pauseMinimale;
	private int MinSoutenanceParProfParJour;
	private int MaxSoutenanceParProfParJour;
	private int nbMembresJury;
	private List<LocalDate> joursDisponibles;
	
	public ConfigPlanning() {
		this.dureeSoutenanceMin = 60;
		this.heureDebutJournee = LocalTime.of(8,0);
		this.heureFinJournee = LocalTime.of(18, 0);
		this.pauseMinimale = 60;
		this.MinSoutenanceParProfParJour=3;
		this.nbMembresJury = 3;
		this.joursDisponibles = new ArrayList<>();
	}
	
	//Getters
	public int getdureeSoutenanceMin() { return dureeSoutenanceMin ;}
	public LocalTime getHeureDebutJournee() { return heureDebutJournee;}
	public LocalTime getHeureFinJournee() { return heureFinJournee;}
	public int getPauseMinimale() { return pauseMinimale;}
	public int getMinSoutenanceParProfParJour() { return MinSoutenanceParProfParJour;}
	public int getNbMembresJury() { return nbMembresJury;}
	public List<LocalDate> getJoursDisponibles() { return joursDisponibles;}
	public int getMaxSoutenanceParProfParJour() { return MaxSoutenanceParProfParJour; }
	
	//Setters 
	public void setDureeSoutenanceMin(int d) {
		this.dureeSoutenanceMin = d;}
	public void setHeureDebutJournee(LocalTime h) {
		this.heureDebutJournee = h;}
	public void setHeureFinJournee(LocalTime h){ 
		this.heureFinJournee = h; }
    public void setPauseMinimaleMin(int p) {
    	this.pauseMinimale = p; }
    public void setMinSoutenancesParProfParJour(int min)  {
    	this.MinSoutenanceParProfParJour = min; }
    public void setMaxSoutenancesParProfParJour(int max)   {
    	this.MaxSoutenanceParProfParJour = max; }
    public void setNbMembresJury(int nb)                   {
    	this.nbMembresJury = nb; }
    
    
    
 // Méthodes utilitaires
    public void ajouterJour(LocalDate jour) {
    	if (!joursDisponibles.contains(jour)) {
    		joursDisponibles.add(jour);
    	}
    }
    
    public int nbCreneauxParJour() {
        int dureeJournee = (int) java.time.Duration
            .between(heureDebutJournee, heureFinJournee)
            .toMinutes();
        return dureeJournee / dureeSoutenanceMin;
    }
    
    public LocalTime calculerHeureFin(LocalTime heureDebut) {
        return heureDebut.plusMinutes(dureeSoutenanceMin);
    }
    
    public boolean estDansLaJournee(LocalTime heureDebut) {
        LocalTime heureFin = calculerHeureFin(heureDebut);
        return !heureDebut.isBefore(heureDebutJournee)
            && !heureFin.isAfter(heureFinJournee);
    }
    
    public String toString() {
        return "ConfigPlanning{"
            + "dureeSoutenance=" + dureeSoutenanceMin + "min"
            + ", journee=" + heureDebutJournee + "-" + heureFinJournee
            + ", pauseMin=" + pauseMinimale + "min"
            + ", maxParProf=" + MaxSoutenanceParProfParJour
            + ", nbJours=" + joursDisponibles.size() + "}";
    }
}

