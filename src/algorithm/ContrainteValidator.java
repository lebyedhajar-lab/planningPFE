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
	
	public boolean enseignantDisponible(Enseignant e,Creneau c) {
        List<Enseignant> enConflit = contrainteRepo.trouverConflits(c);
        for (Enseignant conflit : enConflit) {
            if (conflit.getId() == e.getId()) {
                return false;
            }
        }
        return true;
    }
	public boolean salleDisponible(Salle s,Creneau c) {
        return !soutenanceRepo.salleOccupee(s.getId(), c.getId());
    }
	
	public boolean juryRespecteLangue(Jury jury,String langue) {
        if(!langue.equalsIgnoreCase("anglais")) {
            return true; 
        }
        if(jury.getEncadrant().isAnglophone()) {
            return true;
        }
        for(Enseignant membre : jury.getMembres()) {
            if (membre.isAnglophone()) {
                return true;
            }
        }
        return false;
    }
	
	public boolean respecteMaxParJour(Enseignant e, LocalDate jour, int maxParJour) {
	    int count = 0;
	    for (Soutenance s : soutenanceRepo.chargerTous()) {
	        if (!s.getCreneau().getDateJour().equals(jour)) continue;
	        if (estDansJury(e, s.getJury())) count++;
	    }
	    return count < maxParJour;
	}
	
	public boolean respecteEcartMinimum(Enseignant e, Creneau c, int ecartMin) {
	    for (Soutenance s : soutenanceRepo.chargerTous()) {
	        if (!s.getCreneau().getDateJour().equals(c.getDateJour())) continue;
	        if (!estDansJury(e, s.getJury())) continue;

	        long ecart = Math.abs(c.getHeureDebut().toSecondOfDay() -s.getCreneau().getHeureDebut().toSecondOfDay()) / 60;

	        if (ecart < ecartMin) return false;
	    }
	    return true;
	}

	private boolean estDansJury (Enseignant e, Jury jury) {
	    if (jury.getEncadrant().getId() == e.getId()) return true;
	    for (Enseignant m : jury.getMembres()) {
	        if (m.getId() == e.getId()) return true;
	    }
	    return false;
	}
}