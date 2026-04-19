package model;

public class Enseignant {
	private int id , nbSoutenance ;
	private String nom , prenom ;
	private boolean estAnglophone ; 
	private Filiere filiere ; 
	
	public Enseignant (int id , int nbSoutenance , String nom , String prenom , boolean estAnglophone , Filiere filiere ) {
		this.id=id;
		this.nbSoutenance=nbSoutenance ; 
		this.nom = nom ;
		this.prenom = prenom ;
		this.estAnglophone = estAnglophone;
		this.filiere=filiere;
	}
	public int getId() {
		return id ; 
	}
	public int getNbSoutenace() {
		return nbSoutenance;
	}
	public String getNom() {
		return nom;
	}
	public String getPrenom() {
		return prenom; 
	}
	public boolean isAnglophone() {
		return estAnglophone;
	}
	public Filiere getFiliere() {
		return filiere;
	}
	public void incrementerSoutenances() {
		nbSoutenance++;
	}
}