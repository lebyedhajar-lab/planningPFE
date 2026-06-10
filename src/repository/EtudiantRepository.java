package repository;

import model.Etudiant;

import java.util.ArrayList;
import java.util.List;

public class EtudiantRepository implements IdRepository<Etudiant> {

    private List<Etudiant> etudiants = new ArrayList<>();

    public void sauvegarder(Etudiant e) {
        etudiants.add(e);
    }
    
    public void vider() {
        etudiants.clear();
    }

    public List<Etudiant> chargerTous() {
        return new ArrayList<>(etudiants);
    }
 
    public Etudiant trouverParId(int id) {
        for (Etudiant e : etudiants) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    public Etudiant trouverParNomPrenom(String nom, String prenom) {
        if (nom == null || prenom == null) return null;
        for (Etudiant e : etudiants) {
            if (e.getNom().equalsIgnoreCase(nom.trim())
                && e.getPrenom().equalsIgnoreCase(prenom.trim())) {
                return e;
            }
        }
        return null;
    }

    public int prochainId() {
        int max = 0;
        for (Etudiant e : etudiants) {
            if (e.getId() > max) max = e.getId();
        }
        return max + 1;
    }

    public boolean supprimer(int id) {
        return etudiants.removeIf(e -> e.getId() == id);
    }


    // Tous les étudiants d'une filière donnée
    public List<Etudiant> trouverParFiliere(int filiereId) {
        List<Etudiant> resultat = new ArrayList<>();
        for (Etudiant e : etudiants) {
            if (e.getFiliere() != null
                && e.getFiliere().getID() == filiereId) {
                    resultat.add(e);
            }
        }
        return resultat;
    }

    // Étudiants encadrés par un prof donné
    // Utilisé pour générer le PV/prof/nom.prenom.docx
    public List<Etudiant> trouverParEncadrant(int encadrantId) {
        List<Etudiant> resultat = new ArrayList<>();
        for (Etudiant e : etudiants) {
            if (e.getEncadrant() != null
                && e.getEncadrant().getId() == encadrantId) {
                    resultat.add(e);
            }
        }
        return resultat;
    }


    public List<Etudiant> trouverParLangue(String langue) {
        List<Etudiant> resultat = new ArrayList<>();
        for (Etudiant e : etudiants) {
            if (e.getLangue().equalsIgnoreCase(langue)) {
                resultat.add(e);
            }
        }
        return resultat;
    }

    // Combinaison filière + langue
    // Utilisé directement par PlanningService pour distribuer les jurys
    public List<Etudiant> trouverParFiliereEtLangue(int filiereId, String langue) {
        List<Etudiant> resultat = new ArrayList<>();
        for (Etudiant e : etudiants) {
            if (e.getFiliere() != null
                && e.getFiliere().getID() == filiereId
                && e.getLangue().equalsIgnoreCase(langue)) {
                    resultat.add(e);
            }
        }
        return resultat;
    }

    public int compterParFiliere(int filiereId) {
        int count = 0;
        for (Etudiant e : etudiants) {
            if (e.getFiliere() != null
                && e.getFiliere().getID() == filiereId) {
                    count++;
            }
        }
        return count;
    }
}
