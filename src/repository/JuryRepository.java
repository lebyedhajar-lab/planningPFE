package repository;

import model.Jury;
import model.Enseignant;

import java.util.ArrayList;
import java.util.List;

public class JuryRepository implements IdRepository<Jury> {

    private List<Jury> jurys = new ArrayList<>();

    // ─── Méthodes héritées ───────────────────────────────────────


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

    // ─── Méthodes métier spécifiques ────────────────────────────

    // Vérifier si un prof est déjà dans un jury (encadrant OU membre)
    // Utilisé lors de la composition du jury pour éviter les doublons
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

    // Jurys où ce prof est encadrant
    // Utilisé pour générer PV/prof/nom.prenom.docx
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

    // Jurys où ce prof est simple membre
    // Utilisé pour vérifier l'équité de distribution
    public List<Jury> trouverParMembre(int enseignantId){
        List<Jury> resultat = new ArrayList<>();
        for (Jury j : jurys){
            for (Enseignant m : j.getMembres()) {
                if (m.getId() == enseignantId) {
                    resultat.add(j);
                    break; // éviter doublons
                }
            }
        }
        return resultat;
    }

    // Jurys qui contiennent au moins un prof anglophone
    // Utilisé pour les soutenances en anglais
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

    // Nombre total de jurys où ce prof apparaît (encadrant + membre)
    // Utilisé pour garantir l'équité de distribution entre les profs
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
}
