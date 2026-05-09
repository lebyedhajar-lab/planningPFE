package export;

import model.Soutenance;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PlanningExporter {

    private final ExportDocx exportDocx = new ExportDocx();
    private final FicheNotationExporter ficheExporter = new FicheNotationExporter();

    public void exporterPlanning(List<Soutenance> soutenances, String chemin) throws IOException {
        exportDocx.generer(soutenances, chemin + File.separator + "planning.docx");
    }

    public void exporterFiches(List<Soutenance> soutenances, String dossier) throws IOException {
        ficheExporter.genererToutesLesFiches(soutenances, dossier);
    }

    public void exporterTout(List<Soutenance> soutenances, String dossier) throws IOException {
        exporterPlanning(soutenances, dossier);
        exporterFiches(soutenances, dossier);
    }
}