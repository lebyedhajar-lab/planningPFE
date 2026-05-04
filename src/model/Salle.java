package model;

public class Salle {
	
    int id;
    String nom;
    String numero;
    boolean disponible;

    public Salle(int id, String nom, String numero, boolean disponible) {
        this.id = id;
        this.nom = nom;
        this.numero = numero;
        this.disponible = disponible;
    }

    public int getId()          { return id; }
    public String getNom()      { return nom; }        
    public String getNumero()   { return numero; }    
    public boolean isDisponible() { return disponible; } 
    
    public void setDisponible(boolean disponible) {  
        this.disponible = disponible;
    }
}