package model;

public class Etudiant {
	private int id;
	private String nom;
	private String prenom;
	private String langue;
	private String titrePFE;
	private Filiere filiere;
	private Enseignant encadrant;

    public Etudiant(int id, String nom, String prenom, String langue, Filiere filiere, Enseignant encadrant , String titrePFE) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.langue = langue;
        this.filiere = filiere;
        this.encadrant = encadrant;
        this.titrePFE = titrePFE;
    }
    public int getId()               { return id; }
    public String getNom()            { return nom; }
    public String getPrenom()         { return prenom; }  
    public String getLangue()         { return langue; }
    public Filiere getFiliere()       { return filiere; } 
    public Enseignant getEncadrant()  { return encadrant; }
    public String getTitrePFE() { return titrePFE; }

}