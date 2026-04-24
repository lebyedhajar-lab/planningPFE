package model;

public class Filiere{
	private int id , nbEtudiants;
	private String nom ; 
	public Filiere (int id , int nbEtudiants ,String nom) {
		this.id=id;
		this.nom=nom;
		this.nbEtudiants= nbEtudiants ;
	}
	public int getID() {
		return id; 
	}
	public String getNom() {
		return nom;
	}
	public int getNbEtudiants() {
		return nbEtudiants; 
	}
	public void setID(int id) {
		this.id=id;
	}
	public void setNom(String Nom) {
		this.nom=Nom;
	}
	public void setnbEtudiants(int nbEtudiants) {
		this.nbEtudiants=nbEtudiants ;
	}
	@Override
	public String toString() {
	    return "Filiere{id=" + id + ", nom='" + nom + "', nbEtudiants=" + nbEtudiants + "}";
	}
	
}
