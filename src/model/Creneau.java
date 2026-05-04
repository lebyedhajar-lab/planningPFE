package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Creneau{
	private int id ;
	private LocalDate dateJour;
	private LocalTime heureDebut ; 
	private LocalTime heureFin; 
	private boolean disponible ; 
	
	public Creneau (int id,LocalDate dateJour,LocalTime heureDebut,LocalTime heureFin,boolean disponible){
		this.id= id;
		this.dateJour=dateJour;
		this.heureDebut=heureDebut;
		this.heureFin=heureFin;
		this.disponible=disponible ; 
	}
	
	public int getId() {return id ;}
	public LocalTime getHeureDebut() {return heureDebut;}
	public boolean isDisponible() {return disponible;}
	public void setDisponible( boolean b) {this.disponible=b;}
	public LocalDate getDateJour() {return dateJour;}
    public LocalTime getHeureFin() {return heureFin;}
}
