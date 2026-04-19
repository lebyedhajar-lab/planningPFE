package model;


public class Salle {
	//Attributs:
	int id; 
	String nom; 
	String numero; 
	boolean disponible;
	
	public Salle(int id , String nom ,String numero , boolean disponible){
		this.id=id;
		this.nom=nom;
		this.numero=numero;
		this.disponible=disponible;
	}
	//Méthode :
	public int getId() {return id;}
}