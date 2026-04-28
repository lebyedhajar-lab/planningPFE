package repository;

import model.Contrainte;
import model.Creneau;
import model.Enseignant;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

public class ContrainteRepository implements IdRepository<Contrainte> {

    private List<Contrainte> contraintes = new ArrayList<>();

    public void sauvegarder(Contrainte c) {
        contraintes.add(c);
    }

    public List<Contrainte> chargerTous() {
        return new ArrayList<>(contraintes);
    }

    public Contrainte trouverParId(int id) {
        for (Contrainte c : contraintes) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    public boolean supprimer(int id) {
        return contraintes.removeIf(c -> c.getId() == id);
    }
    
    public List<Contrainte> trouverParEnseignant(int ensId) {
        List<Contrainte> resultat = new ArrayList<>();
        for (Contrainte c : contraintes) {
            if (c.getEnseignant().getId() == ensId) {
                resultat.add(c);
            }
        }
        return resultat;
    }

    public List<Contrainte> trouverParJour(LocalDate jour) {
        List<Contrainte> resultat = new ArrayList<>();
        for (Contrainte c : contraintes) {
            if (c.getJour().equals(jour)) {  // ✅ .equals()
                resultat.add(c);
            }
        }
        return resultat;
    }

    public boolean estDisponible(int ensId, LocalDate j, LocalTime h) {
        for (Contrainte c : contraintes) {
            if (c.getEnseignant().getId() == ensId
                && c.getJour().equals(j)
                && !h.isBefore(c.getHeureDebut())
                && !h.isAfter(c.getHeureFin())) {
                    return false;
            }
        }
        return true;
    }

    public List<Enseignant> trouverConflits(Creneau c) {
        List<Enseignant> enseignantsEnConflit = new ArrayList<>();
        for (Contrainte contrainte : contraintes) {
            if (contrainte.getJour().equals(c.getDateJour())
                && !c.getHeureDebut().isAfter(contrainte.getHeureFin())
                && !c.getHeureFin().isBefore(contrainte.getHeureDebut())) {
                    enseignantsEnConflit.add(contrainte.getEnseignant());
            }
        }
        return enseignantsEnConflit;
    }
    
    public List<Contrainte> chargerParFiliere(int filiereId) {
        List<Contrainte> resultat = new ArrayList<>();
        for (Contrainte c : contraintes) {
            Enseignant ens = c.getEnseignant();
            if (ens != null
                && ens.getFiliere() != null
                && ens.getFiliere().getID() == filiereId) {
                    resultat.add(c);
            }
        }
        return resultat;
    }
}

