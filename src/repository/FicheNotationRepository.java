package repository;

import model.FicheNotation;
import model.Jury;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FicheNotationRepository {

    private List<FicheNotation> fiches = new ArrayList<>();

    public void ajouter(FicheNotation fiche) {
        fiches.add(fiche);
    }

    public FicheNotation findById(int id) {
        for (FicheNotation f : fiches) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null;
    }

    public List<FicheNotation> findAll() {
        return fiches;
    }

    public boolean supprimer(int id) {
        return fiches.removeIf(f -> f.getId() == id);
    }

    public boolean modifier(FicheNotation fiche) {
        for (int i = 0; i < fiches.size(); i++) {
            if (fiches.get(i).getId() == fiche.getId()) {
                fiches.set(i, fiche);
                return true;
            }
        }
        return false;
    }

    public List<FicheNotation> findByJury(Jury jury) {
        List<FicheNotation> result = new ArrayList<>();
        for (FicheNotation f : fiches) {
            if (f.getJury().equals(jury)) {
                result.add(f);
            }
        }
        return result;
    }

    public List<FicheNotation> findByDateRemise(LocalDate date) {
        List<FicheNotation> result = new ArrayList<>();
        for (FicheNotation f : fiches) {
            if (f.getDateRemise().equals(date)) {
                result.add(f);
            }
        }
        return result;
    }

    public double calculerMoyenne() {
        if (fiches.isEmpty()) return 0;
        double total = 0;
        for (FicheNotation f : fiches) {
            total += f.getNote();
        }
        return total / fiches.size();
    }

    public List<FicheNotation> findByNoteMin(double seuil) {
        List<FicheNotation> result = new ArrayList<>();
        for (FicheNotation f : fiches) {
            if (f.getNote() >= seuil) {
                result.add(f);
            }
        }
        return result;
    }
}