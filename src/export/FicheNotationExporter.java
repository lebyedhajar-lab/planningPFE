package export;


import model.Soutenance;
import model.Enseignant;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class FicheNotationExporter {
	private void ajouterLigne(XWPFDocument doc, String texte) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(texte);
        run.setFontSize(12);
    }


    public void genererFiche(Soutenance soutenance, String cheminFichier) throws IOException {
        XWPFDocument document = new XWPFDocument();
        // ── TITRE ─────────────────────────────────────────────
        XWPFParagraph titre = document.createParagraph();
        titre.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runTitre = titre.createRun();
        runTitre.setText("FICHE DE NOTATION - SOUTENANCE PFE");
        runTitre.setBold(true);
        runTitre.setFontSize(16);
        
        // ── INFOS ÉTUDIANT ────────────────────────────────────
        ajouterLigne(document, "Nom & Prénom : " + soutenance.getEtudiant().getNom()  + " " + soutenance.getEtudiant().getPrenom());
        ajouterLigne(document, "Titre PFE : " + soutenance.getEtudiant().getTitrePFE());
        ajouterLigne(document, "Filière : " + soutenance.getEtudiant().getFiliere().getNom());

        // ── INFOS SOUTENANCE ──────────────────────────────────
        ajouterLigne(document, "Date : "  + soutenance.getCreneau().getDateJour());
        ajouterLigne(document, "Heure : "  + soutenance.getCreneau().getHeureDebut());

        ajouterLigne(document, "Salle : " 
            + soutenance.getSalle().getNom());

        // ── JURY ──────────────────────────────────────────────
        ajouterLigne(document, "Encadrant : " 
            + soutenance.getJury().getEncadrant().getNom()
            + " " + soutenance.getJury().getEncadrant().getPrenom());

        ajouterLigne(document, "Membres du jury :");
        for (Enseignant m : soutenance.getJury().getMembres()) {
            ajouterLigne(document, "    - " + m.getNom() + " " + m.getPrenom());
        }

        // ── ZONE DE NOTATION ──────────────────────────────────
        ajouterLigne(document, " ");
        ajouterLigne(document, "Note        : _________ / 20");
        ajouterLigne(document, "Mention     : _________________");
        ajouterLigne(document, "Appréciation: _________________");
        ajouterLigne(document, "Décision    : _________________");

        // ── SIGNATURES ────────────────────────────────────────
        ajouterLigne(document, " ");
        ajouterLigne(document, "Signatures des membres du jury :");
        ajouterLigne(document, " ");
        ajouterLigne(document, "1. ____________________");
        ajouterLigne(document, "2. ____________________");
        ajouterLigne(document, "3. ____________________");

        // ── SAUVEGARDER ───────────────────────────────────────
        FileOutputStream out = new FileOutputStream(cheminFichier);
        document.write(out);
        out.close();
        document.close();
    }

    // ── Méthode utilitaire ────────────────────────────────────
    
    // ── Générer toutes les fiches d'un planning ───────────────
    public void genererToutesLesFiches(List<Soutenance> soutenances, 
                                       String dossier) throws IOException {
        for (Soutenance s : soutenances) {
            String nomFichier = dossier 
                + s.getEtudiant().getNom() 
                + "_" + s.getEtudiant().getPrenom() 
                + "_fiche.docx";
            genererFiche(s, nomFichier);
        }
    }
}