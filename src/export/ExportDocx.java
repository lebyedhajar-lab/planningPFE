package export;

import model.*;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportDocx {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void generer(List<Soutenance> soutenances,
                        String cheminFichier) throws IOException {

        XWPFDocument doc = new XWPFDocument();

        // ── Titre ─────────────────────────────────────────────
        XWPFParagraph titre = doc.createParagraph();
        titre.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runTitre = titre.createRun();
        runTitre.setText("Planning des Soutenances PFE");
        runTitre.setBold(true);
        runTitre.setFontSize(16);
        runTitre.setColor("1F3864");

        // ── Sous-titre ────────────────────────────────────────
        XWPFParagraph sousTitre = doc.createParagraph();
        sousTitre.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runSous = sousTitre.createRun();
        runSous.setText("Généré le "
            + LocalDate.now().format(FMT)
            + "  |  Total : " + soutenances.size()
            + " soutenance(s)");
        runSous.setItalic(true);
        runSous.setFontSize(11);
        runSous.setColor("595959");
        runSous.addBreak();

        // ── Tableau ───────────────────────────────────────────
        XWPFTable table = doc.createTable(
            soutenances.size() + 1, 7);
        table.setWidth("100%");

        // En-têtes
        String[] headers = {
            "Étudiant", "Filière", "Encadrant",
            "Membres Jury", "Date / Heure", "Salle", "Langue"
        };
        XWPFTableRow headerRow = table.getRow(0);
        for (int j = 0; j < headers.length; j++) {
            XWPFTableCell cell = headerRow.getCell(j);
            cell.setColor("1F3864");
            cell.getParagraphs().get(0).setAlignment(
                ParagraphAlignment.CENTER);
            XWPFRun run = cell.getParagraphs().get(0).createRun();
            run.setText(headers[j]);
            run.setBold(true);
            run.setColor("FFFFFF");
            run.setFontSize(10);
        }

        // ── Lignes de données ─────────────────────────────────
        for (int i = 0; i < soutenances.size(); i++) {
            Soutenance s    = soutenances.get(i);
            String bgColor  = (i % 2 == 0) ? "D9E1F2" : "FFFFFF";

            String etudiant = s.getEtudiant() != null
                ? s.getEtudiant().getNom() + " "
                  + s.getEtudiant().getPrenom() : "—";

            String filiere  = (s.getEtudiant() != null
                && s.getEtudiant().getFiliere() != null)
                ? s.getEtudiant().getFiliere().getNom() : "—";

            String encadrant = (s.getJury() != null
                && s.getJury().getEncadrant() != null)
                ? s.getJury().getEncadrant().getNom() + " "
                  + s.getJury().getEncadrant().getPrenom() : "—";

            String membres  = construireListeMembres(s.getJury());

            String dateHeure = s.getCreneau() != null
                ? s.getCreneau().getDateJour().format(FMT)
                  + " " + s.getCreneau().getHeureDebut()
                  + "–" + s.getCreneau().getHeureFin() : "—";

            String salle    = s.getSalle() != null
                ? s.getSalle().getNom() : "—";

            String langue   = s.getLangue() != null
                ? s.getLangue() : "—";

            String[] values = {
                etudiant, filiere, encadrant,
                membres, dateHeure, salle, langue
            };

            XWPFTableRow row = table.getRow(i + 1);
            for (int j = 0; j < values.length; j++) {
                XWPFTableCell cell = row.getCell(j);
                cell.setColor(bgColor);
                // Vider le contenu existant
                cell.getParagraphs().get(0)
                    .getRuns().forEach(r -> r.setText("", 0));
                XWPFRun run = cell.getParagraphs()
                    .get(0).createRun();
                run.setText(values[j]);
                run.setFontSize(9);
            }
        }

        // ── Sauvegarder ───────────────────────────────────────
        FileOutputStream out = new FileOutputStream(cheminFichier);
        doc.write(out);
        out.close();
        doc.close();

        System.out.println("✅ Fichier DOCX généré : "
            + cheminFichier);
    }

    private String construireListeMembres(Jury jury) {
        if (jury == null || jury.getMembres() == null
            || jury.getMembres().isEmpty()) return "—";
        StringBuilder sb = new StringBuilder();
        for (Enseignant m : jury.getMembres()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(m.getNom()).append(" ").append(m.getPrenom());
        }
        return sb.length() > 0 ? sb.toString() : "—";
    }
}