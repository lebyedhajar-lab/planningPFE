package repository;

import model.Salle;
import model.Contrainte;
import model.Creneau;

import java.util.ArrayList;
import java.util.List;

public class SalleRepository implements IdRepository<Salle> {

	    private List<Salle> salles = new ArrayList<>();
	    
	    public void sauvegarder(Salle s) {
	        salles.add(s);
	    }

	    public List<Salle> chargerTous() {
	        return new ArrayList<>(salles);
	    }

	    public Salle trouverParId(int id) {
	        for (Salle s : salles) {
	            if (s.getId() == id) return s;
	        }
	        return null;
	    }

	    public boolean supprimer(int id) {
	    	for (Contrainte c : salles) {
	    	    if (c.getId() == id) {
	    	        salles.remove(c);
	    	        return true;
	    	    }
	    	}
	    }

	    public List<Salle> chargerDisponibles() {
	        List<Salle> resultat = new ArrayList<>();
	        for (Salle s : salles) {
	            if (s.isDisponible()) {
	                resultat.add(s);
	            }
	        }
	        return resultat;
	    }

	    public Salle trouverParNom(String nom) {
	        for (Salle s : salles) {
	            if (s.getNom().equalsIgnoreCase(nom)) return s;
	        }
	        return null;
	    }

	    public boolean setDisponible(int id, boolean dispo) {
	        Salle s = trouverParId(id);
	        if (s != null) {
	            s.setDisponible(dispo);
	            return true;
	        }
	        return false;
	    }

	    public List<Salle> chargerDisponiblesPourCreneau(Creneau c) {
	        List<Salle> resultat = new ArrayList<>();
	        for (Salle s : salles) {
	            if (s.isDisponible()) {
	                resultat.add(s);
	            }
	        }
	        return resultat;
	    }
	}
