package repository; 

import model.FicheNotation;
import model.Jury;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

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
        return fiches.removeIf(f -> f.getId() == id);
    }

    // Fiches d'un jury donné — utilisé lors des délibérations
    public List<FicheNotation> trouverParJury(int juryId) {
        List<FicheNotation> resultat = new ArrayList<>();
        for (FicheNotation f : fiches) {
            if (f.getJury() != null
                && f.getJury().getId() == juryId) {
                    resultat.add(f);
            }
        }
        return resultat;
    }

    // Fiches remises à une date donnée
    public List<FicheNotation> trouverParDate(LocalDate date) {
        List<FicheNotation> resultat = new ArrayList<>();
        for (FicheNotation f : fiches) {
            if (f.getDateRemise() != null
                && f.getDateRemise().equals(date)) {
                    resultat.add(f);
            }
        }
        return resultat;
    }

    // Vérifier si la fiche d'un jury a été remise
    public boolean ficheRemise(int juryId) {
        for (FicheNotation f : fiches) {
            if (f.getJury() != null
                && f.getJury().getId() == juryId) return true;
        }
        return false;
    }
}