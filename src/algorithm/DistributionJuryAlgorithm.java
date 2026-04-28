package algorithm;

import model.Soutenance;
import model.Etudiant;
import model.Jury;
import model.Enseignant;
import model.Creneau;
import java.util.ArrayList;
import java.util.List;

public class DistributionJuryAlgorithm {
	
	private List<Soutenance> soutenances = new ArrayList<>();
	
	public int calculerCharge(Enseignant enseignant) {
	    int compteur = 0;
	    for (Soutenance s : soutenances) {
	        if (s.getJury().contientEnseignant(enseignant)) {
	            compteur++;
	        }
	    }
	    return compteur;
	}
	public Enseignant enseignantLePlusDispo(List<Enseignant> candidats) {
		if (candidats == null || candidats.isEmpty()) {
	        return null;
	    }
		Enseignant EnseignantMinCharge = candidats.get(0);
	    int minCharge = calculerCharge(EnseignantMinCharge);

	    for (Enseignant e : candidats) {
	        int charge = calculerCharge(e);

	        if (charge < minCharge) {
	            minCharge = charge;
	            EnseignantMinCharge = e;
	        }
	    }
	    return EnseignantMinCharge;
	}
	public Jury formerJury(Etudiant e, List<Enseignant> enseignants, String langue, Creneau creneau,ContrainteValidator validator) {
		List<Enseignant> candidats = new ArrayList<>(enseignants);
		
		Enseignant encadrant = e.getEncadrant();
	    candidats.remove(encadrant);
	    
	    List<Enseignant> disponibles = new ArrayList<>();
	    for (Enseignant ens : candidats) {
	        if (validator.enseignantDisponible(ens, creneau)) {
	            disponibles.add(ens);
	        }
	    }
	    
	    Enseignant deuxieme = enseignantLePlusDispo(disponibles);
	    candidats.remove(deuxieme);
	    
	    Enseignant troisieme;
	    if (langue.equals("anglais")) {
	        List<Enseignant> anglophones = new ArrayList<>();
	        for (Enseignant ens : candidats) {
	            if (ens.isAnglophone()) {
	                anglophones.add(ens);
	            }
	        }
	        troisieme = enseignantLePlusDispo(anglophones);
	    } else {
	        troisieme = enseignantLePlusDispo(candidats);
	    }
	    List<Enseignant> membres = new ArrayList<>();
	    membres.add(deuxieme);
	    membres.add(troisieme);
	    return new Jury(0 ,encadrant, membres);
	}	
	public List<Soutenance> distribuer(List<Etudiant> etudiants,List<Enseignant> enseignants , List<Creneau> creneaux , ContrainteValidator validator){
		int i = 0 ;
		for(Etudiant e : etudiants ) {
			Creneau creneau = creneaux.get(i);
			String langue = e.getLangue();
			Jury jury = formerJury(e , enseignants,langue , creneau , validator);
			Soutenance s = new Soutenance(i , e , jury , creneau);
			soutenances.add(s);
			i++;
		}
		return soutenances;
	}
}
