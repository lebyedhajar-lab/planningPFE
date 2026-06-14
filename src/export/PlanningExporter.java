package export;

import model.*;


import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class PlanningExporter {

    private final ExportDocx exportDocx = new ExportDocx();
    private final FicheNotationExporter ficheExporter = new FicheNotationExporter();
    private final AffectationExporter affectationExporter = new AffectationExporter();


    public void exporterPlanning(List<Soutenance> soutenances, String chemin) throws IOException {
        exportDocx.generer(soutenances, chemin + File.separator + "planning.docx");
    }

    public void exporterFiches(List<Soutenance> soutenances, String dossier) throws IOException,InvalidFormatException {
        ficheExporter.genererToutesLesFiches(soutenances, dossier);
    }

    public void exporterTout(List<Soutenance> soutenances, String dossier) throws IOException,InvalidFormatException {
        exporterPlanning(soutenances, dossier);
        exporterFiches(soutenances, dossier);
    }
    public void exporterPV(List<Soutenance> soutenances, String dossier) throws IOException,InvalidFormatException {
        ficheExporter.genererPV(soutenances, dossier);
    }

    public void exporterAffectations(List<Etudiant> etudiants, String dossier)
            throws IOException, InvalidFormatException {
        affectationExporter.exporter(etudiants,
            dossier + File.separator + "affectation_encadrants.docx");
    }
}