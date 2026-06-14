package export;

import model.*;
import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExportDocx {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void generer(List<Soutenance> soutenances,
                        String cheminFichier) throws IOException {

        // Trier par ID comme l'interface
        soutenances = new ArrayList<>(soutenances);
        soutenances.sort((a, b) -> Integer.compare(a.getId(), b.getId()));

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

        // ── Tableau 10 colonnes ───────────────────────────────
        XWPFTable table = doc.createTable(
            soutenances.size() + 1, 10);
        table.setWidth("100%");

        // En-têtes
        String[] headers = {
            "#", "Étudiant", "Filière", "Date", "Heure",
            "Salle", "Encadrant", "Membre 1", "Membre 2", "Langue"
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
            Soutenance s   = soutenances.get(i);
            String bgColor = (i % 2 == 0) ? "D9E1F2" : "FFFFFF";

            String numero  = String.valueOf(s.getId());

            String etudiant = s.getEtudiant() != null
                ? s.getEtudiant().getNom() + " "
                  + s.getEtudiant().getPrenom() : "—";

            String filiere = (s.getEtudiant() != null
                && s.getEtudiant().getFiliere() != null)
                ? s.getEtudiant().getFiliere().getNom() : "—";

            String date = s.getCreneau() != null
                ? s.getCreneau().getDateJour().format(FMT) : "—";

            String heure = s.getCreneau() != null
                ? s.getCreneau().getHeureDebut()
                  + " - " + s.getCreneau().getHeureFin() : "—";

            String salle = s.getSalle() != null
                ? s.getSalle().getNom() : "—";

            String encadrant = (s.getJury() != null
                && s.getJury().getEncadrant() != null)
                ? s.getJury().getEncadrant().getNom() + " "
                  + s.getJury().getEncadrant().getPrenom() : "—";

            String membre1 = "—", membre2 = "—";
            if (s.getJury() != null
                    && s.getJury().getMembres() != null) {
                List<Enseignant> membres = s.getJury().getMembres();
                if (membres.size() > 0)
                    membre1 = membres.get(0).getNom() + " "
                            + membres.get(0).getPrenom();
                if (membres.size() > 1)
                    membre2 = membres.get(1).getNom() + " "
                            + membres.get(1).getPrenom();
            }

            String langue = s.getLangue() != null
                ? s.getLangue() : "—";

            String[] values = {
                numero, etudiant, filiere, date, heure,
                salle, encadrant, membre1, membre2, langue
            };

            XWPFTableRow row = table.getRow(i + 1);
            for (int j = 0; j < values.length; j++) {
                XWPFTableCell cell = row.getCell(j);
                cell.setColor(bgColor);
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

        System.out.println("Fichier DOCX généré : "
            + cheminFichier);
    }
}