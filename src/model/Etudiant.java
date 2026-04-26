package model;

public class Etudiant {
    int id;
    String nom;
    String prenom;
    String langue;
    Filiere filiere;
    Enseignant encadrant;

    public Etudiant(int id, String nom, String prenom, String langue, Filiere filiere, Enseignant encadrant) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.langue = langue;
        this.filiere = filiere;
        this.encadrant = encadrant;
    }

    public int getId()                { return id; }
    public String getNom()            { return nom; }
    public String getPrenom()         { return prenom; }  
    public String getLangue()         { return langue; }
    public Filiere getFiliere()       { return filiere; } 
    public Enseignant getEncadrant()  { return encadrant; }
}