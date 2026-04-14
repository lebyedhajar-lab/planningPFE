package model;

public class Filiere {
	private int id , nbEtudiants;
	private String code , nom ; 
	public Filiere (int id , int nbEtudiants , String code , String nom) {
		this.id=id;
		this.code=code;
		this.nom=nom;
		this.nbEtudiants= nbEtudiants ;
	}
	public int getID() {
		return id ; 
	}
	public String getNom() {
		return nom;
	}
	public String getCode() {
		return code ; 
	}
	public int getNbEtudiants() {
		return nbEtudiants; 
	}
}
