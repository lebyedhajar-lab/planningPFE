package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Contrainte {
	//Attributs:
	int id;
	Enseignant enseignant;
	LocalDate jour;
	LocalTime heureDebut;
	LocalTime heureFin;
	
	public Contrainte(int id, Enseignant enseignant, LocalDate jour, LocalTime heureDebut, LocalTime heureFin) {
		this.id=id;
		this.jour=jour;
		this.enseignant=enseignant;
		this.heureDebut=heureDebut;
   	}
	//Méthodes :
	public Enseignant getEnseignant(){return enseignant; }
	public LocalDate getJour(){return jour; }
	public boolean estDisponible(){return true; }
	public int getId() {return id;}
	public LocalTime getHeureFin() {
		return null;
	}
	public LocalTime getHeureDebut() {
		return null;
	}
}
