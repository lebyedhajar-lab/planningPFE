package repository;

import java.util.ArrayList;
import java.util.List;

import model.Filiere;

public class FiliereRepository implements IdRepository<Filiere>{
    
    private List<Filiere> filieres = new ArrayList<>();

    public void sauvegarder(Filiere f) {
        filieres.add(f);
    }

    public List<Filiere> chargerTous() {
        return filieres;
    }

    public Filiere trouverParId(int id) {
        for (Filiere f : filieres) {
            if (f.getID() == id) return f;
        }
        return null;
    }
    public boolean supprimer(int id) {
        for (Filiere f : filieres) {
            if (f.getID() == id) {
                filieres.remove(f);
                return true;
            }
        }
        return false;
    }
    
    /*
    public void sauvegarder(Filiere f) {
        List<Filiere> liste = lireFichier();  // charger depuis le fichier
        liste.add(f);
        ecrireFichier(liste);                 // réécrire le fichier
    }

    public List<Filiere> chargerTous() {
        return lireFichier();                 // lire depuis le fichier
    }

    public Filiere trouverParId(int id) {
        for (Filiere f : lireFichier()) {     // chercher dans le fichier
            if (f.getId() == id) return f;
        }
        return null;
    }

    public boolean supprimer(int id) {
        List<Filiere> liste = lireFichier();
        for (Filiere f : liste) {
            if (f.getId() == id) {
                liste.remove(f);
                ecrireFichier(liste);         // réécrire sans l'élément supprimé
                return true;
            }
        }
        return false;
    }*/
}