package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Contrainte{
	private int id;
	private Enseignant enseignant;
	private LocalDate jour;
	private LocalTime heureDebut;
	private LocalTime heureFin;
	
	public Contrainte(int id,Enseignant enseignant,LocalDate jour, LocalTime heureDebut, LocalTime heureFin) {
		this.id=id;
		this.jour=jour;
		this.enseignant=enseignant;
		this.heureDebut=heureDebut;
		this.heureFin = heureFin;
   	}
	
	public Enseignant getEnseignant(){return enseignant;}
	public LocalDate getJour(){return jour; }
	public int getId() {return id;}
	public LocalTime getHeureFin()   { return heureFin; }
	public LocalTime getHeureDebut() { return heureDebut; }
	
	public boolean estDisponible(){return true;}

}

