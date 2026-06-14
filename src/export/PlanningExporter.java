package export;

import model.Soutenance;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PlanningExporter {

    private final ExportDocx exportDocx = new ExportDocx();
    private final FicheNotationExporter ficheExporter = new FicheNotationExporter();

    public void exporterPlanning(List<Soutenance> soutenances,String chemin) throws IOException {
    	// ── Trier par date puis heure puis salle ──────────────
    	List<Soutenance> tries = new ArrayList<>(soutenances);
    	tries.sort((a, b) -> {
    		// Comparer par date
    		int cmp = a.getCreneau().getDateJour().compareTo(b.getCreneau().getDateJour());
    		if (cmp != 0) return cmp;

    		// Même date → comparer par heure
    		cmp = a.getCreneau().getHeureDebut().compareTo(b.getCreneau().getHeureDebut());
    		if (cmp != 0) return cmp;

    		// Même heure → comparer par salle
    		return a.getSalle().getNom().compareTo(b.getSalle().getNom());
    	});

    	String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    	exportDocx.generer(tries,chemin + File.separator+ "planning_" + timestamp + ".docx");
    }
    
    public void exporterFiches(List<Soutenance> soutenances, String dossier) throws IOException {
        ficheExporter.genererToutesLesFiches(soutenances, dossier);
    }

    public void exporterTout(List<Soutenance> soutenances, String dossier) throws IOException {
        exporterPlanning(soutenances, dossier);
        exporterFiches(soutenances, dossier);
    }
}
