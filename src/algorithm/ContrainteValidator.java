package algorithm;



import model.*;
import repository.*;
import java.util.List;
import java.time.LocalDate;

public class ContrainteValidator{
	
	private ContrainteRepository contrainteRepo;
	private SoutenanceRepository soutenanceRepo;
	
	public ContrainteValidator(ContrainteRepository contrainteRepo,SoutenanceRepository soutenanceRepo) {
		this.contrainteRepo = contrainteRepo;
		this.soutenanceRepo = soutenanceRepo;
	}
	
	//Enseignant disponible sur ce créneau ?
	public boolean enseignantDisponible(Enseignant e,Creneau c) {
        List<Enseignant> enConflit = contrainteRepo.trouverConflits(c);
        for (Enseignant conflit : enConflit) {
            if (conflit.getId() == e.getId()) {
                return false;
            }
        }
        return true;
    }
	//Salle libre sur ce créneau ?
	public boolean salleDisponible(Salle s,Creneau c) {
        return !soutenanceRepo.salleOccupee(s.getId(), c.getId());
    }
	
	//Jury adapté à la langue de la soutenance ?
	public boolean juryRespecteLangue(Jury jury,String langue) {
        if(!langue.equalsIgnoreCase("anglais")) {
            return true; // pas de contrainte en français
        }
     // Vérifie si au moins un prof du jury est anglophone
        if(jury.getEncadrant().isAnglophone()) {
            return true;
        }
     // vérifier les membres un par un
        for(Enseignant membre : jury.getMembres()) {
            if (membre.isAnglophone()) {
                return true;
            }
        }
        return false;
    }
	
	// Enseignant pas surchargé ce jour-là ?
	public boolean respecteMaxParJour(Enseignant e, LocalDate jour, int maxParJour) {
	    int count = 0;
	    for (Soutenance s : soutenanceRepo.chargerTous()) {
	        if (!s.getCreneau().getDateJour().equals(jour)) continue;
	        if (estDansJury(e, s.getJury())) count++;
	    }
	    return count < maxParJour;
	}
	
	// Écart suffisant entre deux soutenances (entre les heures de début) ?
	public boolean respecteEcartMinimum(Enseignant e, Creneau c, int ecartMin) {
	    for (Soutenance s : soutenanceRepo.chargerTous()) {
	        if (!s.getCreneau().getDateJour().equals(c.getDateJour())) continue;
	        if (!estDansJury(e, s.getJury())) continue;

	        long ecart = Math.abs(c.getHeureDebut().toSecondOfDay() -s.getCreneau().getHeureDebut().toSecondOfDay()) / 60;

	        if (ecart < ecartMin) return false;
	    }
	    return true;
	}

	// Méthode utilitaire privée
	private boolean estDansJury (Enseignant e, Jury jury) {
	    if (jury.getEncadrant().getId() == e.getId()) return true;
	    for (Enseignant m : jury.getMembres()) {
	        if (m.getId() == e.getId()) return true;
	    }
	    return false;
	}
}