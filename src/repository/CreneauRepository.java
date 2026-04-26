package repository;

import model.Creneau;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CreneauRepository implements IdRepository<Creneau> {

    private List<Creneau> creneaux = new ArrayList<>();

    public void sauvegarder(Creneau c) {
        creneaux.add(c);
    }

    public List<Creneau> chargerTous() {
        return creneaux;
    }

    public Creneau trouverParId(int id) {
        for (Creneau c : creneaux) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    public boolean supprimer(int id) {
        for (Creneau c : creneaux) {
            if (c.getId() == id) {
                creneaux.remove(c);
                return true;
            }
        }
        return false;
    }

    public List<Creneau> findDisponibles() {
        List<Creneau> result = new ArrayList<>();
        for (Creneau c : creneaux) {
            if (c.isDisponible()) result.add(c);
        }
        return result;
    }

    public List<Creneau> findByDate(LocalDate date) {
        List<Creneau> result = new ArrayList<>();
        for (Creneau c : creneaux) {
            if (c.getDateJour().equals(date)) result.add(c);
        }
        return result;
    }

    public boolean estDisponible(LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        for (Creneau c : creneaux) {
            if (c.getDateJour().equals(date)) {
                if (!(heureFin.isBefore(c.getHeureDebut()) ||
                      heureDebut.isAfter(c.getHeureFin()))) {
                    return false;
                }
            }
        }
        return true;
    }
}