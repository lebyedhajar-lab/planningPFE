package repository;

import model.FicheNotation;
import model.Jury;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FicheNotationRepository implements IdRepository<FicheNotation> {

    private List<FicheNotation> fiches = new ArrayList<>();

    public void sauvegarder(FicheNotation f) {
        fiches.add(f);
    }

    public List<FicheNotation> chargerTous() {
        return fiches;
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

    public List<FicheNotation> findByJury(Jury jury) {
        List<FicheNotation> result = new ArrayList<>();
        for (FicheNotation f : fiches) {
        	if (f.getJury().getId() == jury.getId()) result.add(f);
        }
        return result;
    }

    public List<FicheNotation> findByDateRemise(LocalDate date) {
        List<FicheNotation> result = new ArrayList<>();
        for (FicheNotation f : fiches) {
            if (f.getDateRemise().equals(date)) result.add(f);
        }
        return result;
    }

    public double calculerMoyenne() {
        if (fiches.isEmpty()) return 0;
        double total = 0;
        int count = 0;
        for (FicheNotation f : fiches) {
            if (f.getNote() != null) { 
                total += f.getNote();
                count++;
            }
        }
        return count == 0 ? 0 : total / count;
    }

    public List<FicheNotation> findByNoteMin(double seuil) {
        List<FicheNotation> result = new ArrayList<>();
        for (FicheNotation f : fiches) {
            if (f.getNote() != null && f.getNote() >= seuil)
                result.add(f);
        }
        return result;
    }
}