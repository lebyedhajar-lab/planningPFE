package repository;

import model.Soutenance;
import java.util.ArrayList;
import java.util.List;

public class SoutenanceRepository implements IdRepository<Soutenance> {

    private List<Soutenance> soutenances = new ArrayList<>();

    public void sauvegarder(Soutenance s) {
        soutenances.add(s);
    }

    public List<Soutenance> chargerTous() {
        return new ArrayList<>(soutenances);
    }

    public Soutenance trouverParId(int id) {
        for (Soutenance s : soutenances) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public boolean supprimer(int id) {
        for (Soutenance s : soutenances) {
            if (s.getId() == id) {
                soutenances.remove(s);
                return true;
            }
        }
        return false;
    }

    public Soutenance trouverParEtudiant(int etudiantId) {
        for (Soutenance s : soutenances) {
            if (s.getEtudiant() != null
                && s.getEtudiant().getId() == etudiantId) {
                return s;
            }
        }
        return null;
    }

    public List<Soutenance> trouverParSalle(int salleId) {
        List<Soutenance> resultat = new ArrayList<>();
        for (Soutenance s : soutenances) {
            if (s.getSalle() != null
                && s.getSalle().getId() == salleId) {
                resultat.add(s);
            }
        }
        return resultat;
    }

    public List<Soutenance> trouverParCreneau(int creneauId) {
        List<Soutenance> resultat = new ArrayList<>();
        for (Soutenance s : soutenances) {
            if (s.getCreneau() != null
                && s.getCreneau().getId() == creneauId) {
                resultat.add(s);
            }
        }
        return resultat;
    }

    public List<Soutenance> trouverParLangue(String langue) {
        List<Soutenance> resultat = new ArrayList<>();
        for (Soutenance s : soutenances) {
            if (s.getLangue() != null
                && s.getLangue().equalsIgnoreCase(langue)) {
                resultat.add(s);
            }
        }
        return resultat;
    }

    public boolean salleOccupee(int salleId, int creneauId) {
        for (Soutenance s : soutenances) {
            if (s.getSalle() != null && s.getCreneau() != null
                && s.getSalle().getId() == salleId
                && s.getCreneau().getId() == creneauId) {
                return true;
            }
        }
        return false;
    }
  

}
