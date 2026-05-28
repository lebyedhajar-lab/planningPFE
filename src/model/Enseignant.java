package model;

public class Enseignant {
	private int id , nbSoutenance ;
	private String nom , prenom ;
	private boolean estAnglophone ; 
	private String specialite;
	
	
	public Enseignant(int id, int nbSoutenance, String nom, String prenom,boolean estAnglophone, String specialite) {
		this.id = id;
		this.nbSoutenance = nbSoutenance;
		this.nom = nom;
		this.prenom = prenom;
		this.estAnglophone = estAnglophone;
		this.specialite = specialite;
	}
	public int getId() {return id ;}
	public int getNbSoutenance() {return nbSoutenance;}
	public String getNom() {return nom;}
	public String getPrenom() {return prenom; }
	public boolean isAnglophone(){return estAnglophone;}
	public String getSpecialite() {return specialite;}
	public void incrementerSoutenances() {nbSoutenance++;}
	}