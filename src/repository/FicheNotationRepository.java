package repository;

import model.FicheNotation;
import java.util.ArrayList;
import java.util.List;

public class FicheNotationRepository implements IdRepository<FicheNotation> {

    private List<FicheNotation> fiches = new ArrayList<>();

    public void sauvegarder(FicheNotation f) {
        fiches.add(f);
    }

    public List<FicheNotation> chargerTous() {
        return new ArrayList<>(fiches);
    }

    public FicheNotation trouverParId(int id) {
        for (FicheNotation f : fiches) {
            if (f.getId() == id) return f;
        }
        return null;
    }

    public boolean supprimer(int id) {
        for (FicheNotation f : fiches) {
            if (f.getId() == id) {
                fiches.remove(f);
                return true;
            }
        }
        return false;
    }

    public FicheNotation trouverParJury(int juryId) {
        for (FicheNotation f : fiches) {
            if (f.getJury() != null
                && f.getJury().getId() == juryId) {
                return f;
            }
        }
        return null;
    }

    public double calculerMoyenne() {
        if (fiches.isEmpty()) return 0.0;
        double total = 0;
        for (FicheNotation f : fiches) {
            total += f.getNote();
        }
        return total / fiches.size();
    }

    public List<FicheNotation> trouverParNoteSup(double seuil) {
        List<FicheNotation> resultat = new ArrayList<>();
        for (FicheNotation f : fiches) {
            if (f.getNote() >= seuil) {
                resultat.add(f);
            }
        }
        return resultat;
    }

    public List<FicheNotation> trouverParNoteInf(double seuil) {
        List<FicheNotation> resultat = new ArrayList<>();
        for (FicheNotation f : fiches) {
            if (f.getNote() < seuil) {
                resultat.add(f);
            }
        }
        return resultat;
    }
}