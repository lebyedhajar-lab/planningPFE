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

    public boolean supprimer(int id) {
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
}
