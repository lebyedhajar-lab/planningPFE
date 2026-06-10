package repository;

import model.Enseignant;

import java.util.ArrayList;
import java.util.List;

public class EnseignantRepository implements IdRepository<Enseignant> {

    private List<Enseignant> enseignants = new ArrayList<>();

    public void sauvegarder(Enseignant e) {
        enseignants.add(e);
    }

    public List<Enseignant> chargerTous() {
        return new ArrayList<>(enseignants);
    }

    public Enseignant trouverParId(int id) {
        for (Enseignant e : enseignants) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    public Enseignant trouverParNomPrenom(String nom, String prenom) {
        if (nom == null || prenom == null) return null;
        String n = nom.trim();
        String p = prenom.trim();
        for (Enseignant e : enseignants) {
            if (e.getNom().equalsIgnoreCase(n) && e.getPrenom().equalsIgnoreCase(p)) {
                return e;
            }
            if (e.getNom().equalsIgnoreCase(p) && e.getPrenom().equalsIgnoreCase(n)) {
                return e;
            }
        }
        return null;
    }

    public int prochainId() {
        int max = 0;
        for (Enseignant e : enseignants) {
            if (e.getId() > max) max = e.getId();
        }
        return max + 1;
    }

    public boolean supprimer(int id){
        return enseignants.removeIf(e -> e.getId() == id);
    }

    public List<Enseignant> trouverParSpecialite(String specialite) {
        List<Enseignant> resultat = new ArrayList<>();
        for (Enseignant e : enseignants) {
            if (e.getSpecialite() != null
                && e.getSpecialite().equalsIgnoreCase(specialite)) {
                resultat.add(e);
            }
        }
        return resultat;
    }

    public List<Enseignant> trouverAnglophones() {
        List<Enseignant> resultat = new ArrayList<>();
        for (Enseignant e : enseignants) {
            if (e.isAnglophone()) {
                resultat.add(e);
            }
        }
        return resultat;
    }
    
    public void vider() {
        enseignants.clear();
    }
}
