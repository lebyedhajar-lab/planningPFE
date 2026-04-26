package repository;

import model.Creneau;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CreneauRepository {

    private List<Creneau> creneaux = new ArrayList<>();

    public void ajouter(Creneau creneau) {
        creneaux.add(creneau);
    }

    public Creneau findById(int id) {
        for (Creneau c : creneaux) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public List<Creneau> findAll() {
        return creneaux;
    }

    public boolean supprimer(int id) {
        return creneaux.removeIf(c -> c.getId() == id);
    }

    public boolean modifier(Creneau creneau) {
        for (int i = 0; i < creneaux.size(); i++) {
            if (creneaux.get(i).getId() == creneau.getId()) {
                creneaux.set(i, creneau);
                return true;
            }
        }
        return false;
    }

    public List<Creneau> findDisponibles() {
        List<Creneau> disponibles = new ArrayList<>();
        for (Creneau c : creneaux) {
            if (c.isDisponible()) {
                disponibles.add(c);
            }
        }
        return disponibles;
    }

    public List<Creneau> findByDate(LocalDate date) {
        List<Creneau> result = new ArrayList<>();
        for (Creneau c : creneaux) {
            if (c.getDateJour().equals(date)) {
                result.add(c);
            }
        }
        return result;
    }

    public boolean estDisponible(LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        for (Creneau c : creneaux) {
            if (c.getDateJour().equals(date) && c.isDisponible()) {
                if (!(heureFin.isBefore(c.getHeureDebut()) || 
                      heureDebut.isAfter(c.getHeureFin()))) {
                    return false; 
                }
            }
        }
        return true;
    }
}