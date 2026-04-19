package repository;

import java.util.ArrayList;
import java.util.List;

import model.Filiere;

public class FiliereRepository {
    
    private List<Filiere> filieres = new ArrayList<>();

    public void ajouter(Filiere f) {
        filieres.add(f);
    }

    public List<Filiere> getAll() {
        return filieres;
    }

    public Filiere findById(int id) {
        for (Filiere f : filieres) {
            if (f.getID() == id) return f;
        }
        return null;
    }
}