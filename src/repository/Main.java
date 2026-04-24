package repository;

import model.Filiere;

public class Main {
	public static void main (String[] args) {
		FiliereRepository repo = new FiliereRepository();
		Filiere f1 = new Filiere(1, 30, "Génie Informatique");
	    Filiere f2 = new Filiere(2, 25, "Ingénieurie des données");
		repo.sauvegarder(f1); 
		repo.sauvegarder(f2);
		repo.chargerTous();	
		for (Filiere f : repo.chargerTous()) {
		    System.out.println(f);
		}
	}
}
