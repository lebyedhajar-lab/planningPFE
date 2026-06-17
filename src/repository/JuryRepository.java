package repository;

import model.Jury;
import model.Enseignant;

import java.util.ArrayList;
import java.util.List;

public class JuryRepository implements IdRepository<Jury> {

    private List<Jury> jurys = new ArrayList<>();


    public void sauvegarder(Jury j) {
        jurys.add(j);
    }


    public List<Jury> chargerTous() {
        return new ArrayList<>(jurys);
    }

    public Jury trouverParId(int id) {
        for (Jury j : jurys) {
            if (j.getId() == id) return j;
        }
        return null;
    }

    public boolean supprimer(int id) {
        return jurys.removeIf(j -> j.getId() == id);
    }

   
    public boolean contientEnseignant(int juryId, int enseignantId) {
        Jury j = trouverParId(juryId);
        if (j == null) return false;

        // vérifier encadrant
        if (j.getEncadrant() != null
            && j.getEncadrant().getId() == enseignantId) return true;

        // vérifier membres
        for (Enseignant m : j.getMembres()) {
            if (m.getId() == enseignantId) return true;
        }
        return false;
    }

    
    public List<Jury> trouverParEncadrant(int encadrantId) {
        List<Jury> resultat = new ArrayList<>();
        for (Jury j : jurys) {
            if (j.getEncadrant() != null
                && j.getEncadrant().getId() == encadrantId) {
                    resultat.add(j);
            }
        }
        return resultat;
    }

   
    public List<Jury> trouverParMembre(int enseignantId){
        List<Jury> resultat = new ArrayList<>();
        for (Jury j : jurys){
            for (Enseignant m : j.getMembres()) {
                if (m.getId() == enseignantId) {
                    resultat.add(j);
                    break; 
                }
            }
        }
        return resultat;
    }

    public List<Jury> trouverJurysAnglophones() {
        List<Jury> resultat = new ArrayList<>();
        for (Jury j : jurys) {
            // vérifier encadrant
            if (j.getEncadrant() !=null
                && j.getEncadrant().isAnglophone()) {
                    resultat.add(j);
                    continue;
            }
            // vérifier membres
            for (Enseignant m : j.getMembres()) {
                if (m.isAnglophone()) {
                    resultat.add(j);
                    break;
                }
            }
        }
        return resultat;
    }

  
    public int compterApparitionsEnseignant(int enseignantId) {
        int count = 0;
        for (Jury j : jurys) {
            // compter si encadrant
            if (j.getEncadrant() != null
                && j.getEncadrant().getId() == enseignantId) {
                    count++;
                    continue;
            }
            // compter si membre
            for (Enseignant m : j.getMembres()) {
                if (m.getId() == enseignantId) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
    public void vider() { jurys.clear(); }
}
