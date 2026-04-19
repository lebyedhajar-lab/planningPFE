package model;

import java.util.List;

public class Jury {
	
	private int id;
	private Enseignant encadrant;
	private List<Enseignant> membres;
	
	public Jury(int id, Enseignant encadrant, List<Enseignant> membres) {
		this.id = id;
		this.encadrant = encadrant;
		this.membres = membres;
	}
	
	public int getId() {
		return id;
	}
	
	public Enseignant getEncadrant() {
		return encadrant;
	}
	
	public List<Enseignant> getMembres() {
		return membres;
	}
}
