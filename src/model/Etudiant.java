package model;

public class Etudiant {
	 int id;
	 String nom;
	 String prenom;
	 String langue;
	 Filiere filiere; 
	 Enseignant encadrant; 
	 //Attributs:
	 public Etudiant(int id, String nom, String prenom, String langue, Filiere filiere, Enseignant encadrant) {
		 this.id=id;
		 this.nom=nom;
		 this.prenom=prenom;
		 this.langue=langue;
		 this.filiere=filiere;
		 this.encadrant=encadrant;
	 }
	//Méthodes :
	 public int getId() {return id;}
	 public String getNom() {return nom;}
	 public String getLangue() {return langue;}
	 public Enseignant getEncadrant() {return encadrant;}
	 
}
