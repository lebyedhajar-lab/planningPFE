package export;

import model.*;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExportDocx {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Couleurs header ───────────────────────────────────────
    private static final String BLEU_FONCE  = "1F3864";
    private static final String BLANC       = "FFFFFF";
    private static final String BLEU_TITRE  = "2E5496";

    // ── Couleurs par filière ──────────────────────────────────
    private static final Map<String, String> COULEURS_FILIERES
        = new LinkedHashMap<>();
    static {
        COULEURS_FILIERES.put("GL",   "DDEBF7"); // bleu clair
        COULEURS_FILIERES.put("ID",   "E2EFDA"); // vert clair
        COULEURS_FILIERES.put("TDIA", "FCE4D6"); // orange clair
    }

    // ── Couleurs par prof (palette variée) ────────────────────
    private static final String[] COULEURS_PROFS = {
        "FFE699", "C6EFCE", "FCE4D6", "FFF2CC",
        "E8D5F5", "FADADD", "D5F5E3", "FAE5D3",
        "D6EAF8", "FDEBD0", "E8F8F5", "FDEDEC",
        "EBF5FB", "F9EBEA", "E9F7EF", "FEF9E7",
        "F0F3FF", "FDF2E9", "E8F4FD", "D0E4F5",
        "F4CCCC", "D9D9D9", "FADADD", "D5F5E3"
    };

    // ── Couleurs par jour ─────────────────────────────────────
    private static final String[] COULEURS_JOURS = {
        "EBF5FB", "E9F7EF", "FEF9E7",
        "FDEDEC", "F0F3FF", "E8D5F5"
    };

    public void generer(List<Soutenance> soutenances,
                        String cheminFichier) throws IOException {

        XWPFDocument doc = new XWPFDocument();

        // ── Mapper encadrants -> couleurs ──────────────────────
        Map<Integer, String> couleurParProf = new LinkedHashMap<>();
        int idx = 0;
        for (Soutenance s : soutenances) {
            if (s.getJury() != null
                    && s.getJury().getEncadrant() != null) {
                int id = s.getJury().getEncadrant().getId();
                if (!couleurParProf.containsKey(id)) {
                    couleurParProf.put(id,
                        COULEURS_PROFS[idx % COULEURS_PROFS.length]);
                    idx++;
                }
            }
        }

        // ── Mapper filières -> couleurs ────────────────────────
        Map<String, String> couleurParFiliere = new LinkedHashMap<>();
        for (Soutenance s : soutenances) {
            if (s.getEtudiant() != null
                    && s.getEtudiant().getFiliere() != null) {
                String nom = s.getEtudiant().getFiliere().getNom();
                if (!couleurParFiliere.containsKey(nom)) {
                    couleurParFiliere.put(nom,
                        COULEURS_FILIERES.getOrDefault(nom,
                            COULEURS_PROFS[(couleurParFiliere.size() + 8)
                                % COULEURS_PROFS.length]));
                }
            }
        }

        // ── Mapper jours -> couleurs ───────────────────────────
        Map<LocalDate, String> couleurParJour = new LinkedHashMap<>();
        int jIdx = 0;
        for (Soutenance s : soutenances) {
            if (s.getCreneau() != null) {
                LocalDate j = s.getCreneau().getDateJour();
                if (!couleurParJour.containsKey(j)) {
                    couleurParJour.put(j,
                        COULEURS_JOURS[jIdx % COULEURS_JOURS.length]);
                    jIdx++;
                }
            }
        }

        // ── En-tête institution ───────────────────────────────
        para(doc, "Ecole Nationale des Sciences Appliquees - Al Hoceima",
            true, 12, BLEU_TITRE, ParagraphAlignment.CENTER);
        para(doc, "Departement Mathematiques et Informatique",
            false, 11, BLEU_TITRE, ParagraphAlignment.CENTER);
        para(doc, "", false, 4, "000000", ParagraphAlignment.CENTER);

        // ── Titre ─────────────────────────────────────────────
        para(doc,
            "Planning des soutenances des Projets de Fin d'Etude",
            true, 14, BLEU_FONCE, ParagraphAlignment.CENTER);
        para(doc, "Annee Universitaire 2024/2025",
            false, 11, BLEU_FONCE, ParagraphAlignment.CENTER);
        para(doc,
            "Genere le " + LocalDate.now().format(FMT)
            + "  |  Total : " + soutenances.size()
            + " soutenance(s)",
            false, 10, "595959", ParagraphAlignment.CENTER);
        para(doc, "", false, 4, "000000", ParagraphAlignment.CENTER);

        // ── Légende filières ──────────────────────────────────
        para(doc, "Legende Filieres :", true, 10,
            "000000", ParagraphAlignment.LEFT);

        XWPFTable legendeTable = doc.createTable(1,
            couleurParFiliere.size());
        legendeTable.setWidth("60%");
        XWPFTableRow legendeRow = legendeTable.getRow(0);
        int lIdx = 0;
        for (Map.Entry<String, String> e
                : couleurParFiliere.entrySet()) {
            XWPFTableCell lCell = legendeRow.getCell(lIdx++);
            lCell.setColor(e.getValue());
            lCell.getParagraphs().get(0)
                .setAlignment(ParagraphAlignment.CENTER);
            XWPFRun lRun = lCell.getParagraphs()
                .get(0).createRun();
            lRun.setText(e.getKey());
            lRun.setBold(true);
            lRun.setFontSize(9);
            lRun.setFontFamily("Calibri");
        }
        para(doc, "", false, 4, "000000", ParagraphAlignment.CENTER);

        // ── Légende encadrants ────────────────────────────────
        para(doc, "Legende Encadrants :", true, 10,
            "000000", ParagraphAlignment.LEFT);

        // Créer tableau légende profs (5 par ligne)
        List<Map.Entry<Integer, String>> profEntries =
            new ArrayList<>(couleurParProf.entrySet());

        // Trouver le nom de chaque prof
        Map<Integer, String> nomParProf = new LinkedHashMap<>();
        for (Soutenance s : soutenances) {
            if (s.getJury() != null
                    && s.getJury().getEncadrant() != null) {
                Enseignant enc = s.getJury().getEncadrant();
                nomParProf.put(enc.getId(),
                    enc.getNom() + " " + enc.getPrenom());
            }
        }

        int colsParLigne = 5;
        int nbLignes = (int) Math.ceil(
            (double) profEntries.size() / colsParLigne);

        XWPFTable legendeProfs = doc.createTable(nbLignes, colsParLigne);
        legendeProfs.setWidth("100%");
        for (int li = 0; li < nbLignes; li++) {
            XWPFTableRow lr = legendeProfs.getRow(li);
            for (int ci = 0; ci < colsParLigne; ci++) {
                int pIdx = li * colsParLigne + ci;
                XWPFTableCell lc = lr.getCell(ci);
                if (pIdx < profEntries.size()) {
                    int profId = profEntries.get(pIdx).getKey();
                    String couleur = profEntries.get(pIdx).getValue();
                    lc.setColor(couleur);
                    lc.getParagraphs().get(0)
                        .setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun lr2 = lc.getParagraphs()
                        .get(0).createRun();
                    lr2.setText(nomParProf.getOrDefault(profId, ""));
                    lr2.setFontSize(8);
                    lr2.setFontFamily("Calibri");
                } else {
                    lc.setColor(BLANC);
                    lc.getParagraphs().get(0).createRun()
                        .setText("");
                }
            }
        }
        para(doc, "", false, 4, "000000", ParagraphAlignment.CENTER);

        // ── Tableau principal ─────────────────────────────────
        XWPFTable table = doc.createTable(
            soutenances.size() + 1, 9);
        table.setWidth("100%");

        // En-têtes
        String[] headers = {
            "ID", "Encadrant", "Membre jury 1",
            "Membre jury 2", "Date", "Heure",
            "Salle", "Nom etudiant", "Prenom etudiant"
        };
        XWPFTableRow headerRow = table.getRow(0);
        for (int j = 0; j < headers.length; j++) {
            XWPFTableCell cell = headerRow.getCell(j);
            cell.setColor(BLEU_FONCE);
            cell.getParagraphs().get(0)
                .setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = cell.getParagraphs()
                .get(0).createRun();
            run.setText(headers[j]);
            run.setBold(true);
            run.setColor(BLANC);
            run.setFontSize(9);
            run.setFontFamily("Calibri");
        }

        // Lignes données
        for (int i = 0; i < soutenances.size(); i++) {
            Soutenance s = soutenances.get(i);

            // Couleur filière
            String nomFiliere = s.getEtudiant() != null
                && s.getEtudiant().getFiliere() != null
                ? s.getEtudiant().getFiliere().getNom() : "";
            String bgFiliere = couleurParFiliere
                .getOrDefault(nomFiliere, BLANC);

            // Couleur prof
            String bgProf = BLANC;
            if (s.getJury() != null
                    && s.getJury().getEncadrant() != null) {
                bgProf = couleurParProf.getOrDefault(
                    s.getJury().getEncadrant().getId(), BLANC);
            }

            // Couleur jour
            String bgJour = BLANC;
            if (s.getCreneau() != null) {
                bgJour = couleurParJour.getOrDefault(
                    s.getCreneau().getDateJour(), BLANC);
            }

            // Valeurs
            String encadrant = s.getJury() != null
                && s.getJury().getEncadrant() != null
                ? s.getJury().getEncadrant().getNom() + " "
                  + s.getJury().getEncadrant().getPrenom() : "-";

            List<Enseignant> membres = s.getJury() != null
                ? s.getJury().getMembres() : new ArrayList<>();
            String membre1 = membres.size() > 0
                ? membres.get(0).getNom() + " "
                  + membres.get(0).getPrenom() : "-";
            String membre2 = membres.size() > 1
                ? membres.get(1).getNom() + " "
                  + membres.get(1).getPrenom() : "-";

            String date  = s.getCreneau() != null
                ? s.getCreneau().getDateJour().format(FMT) : "-";
            String heure = s.getCreneau() != null
                ? s.getCreneau().getHeureDebut() + "h" : "-";
            String salle = s.getSalle() != null
                ? s.getSalle().getNom() : "-";
            String nom    = s.getEtudiant() != null
                ? s.getEtudiant().getNom() : "-";
            String prenom = s.getEtudiant() != null
                ? s.getEtudiant().getPrenom() : "-";

            String[] values = {
                String.valueOf(i + 1),
                encadrant, membre1, membre2,
                date, heure, salle, nom, prenom
            };

            // Couleurs par colonne
            String[] bgColors = {
                bgJour,    // ID      -> couleur du jour
                bgProf,    // Encadrant - couleur du prof
                bgProf,    // Membre 1  → couleur du prof
                bgProf,    // Membre 2  → couleur du prof
                bgJour,    // Date      → couleur du jour
                bgJour,    // Heure     → couleur du jour
                bgJour,    // Salle     → couleur du jour
                bgFiliere, // Nom       → couleur filière
                bgFiliere  // Prénom    → couleur filière
            };

            boolean[] centered = {
                true, false, false, false,
                true, true, true, false, false
            };

            XWPFTableRow row = table.getRow(i + 1);
            for (int j = 0; j < values.length; j++) {
                XWPFTableCell cell = row.getCell(j);
                cell.setColor(bgColors[j]);
                cell.getParagraphs().get(0).getRuns()
                    .forEach(r -> r.setText("", 0));
                if (centered[j])
                    cell.getParagraphs().get(0)
                        .setAlignment(ParagraphAlignment.CENTER);
                XWPFRun run = cell.getParagraphs()
                    .get(0).createRun();
                run.setText(values[j]);
                run.setFontSize(9);
                run.setFontFamily("Calibri");
            }
        }

        // ── Sauvegarder ───────────────────────────────────────
        try (FileOutputStream out =
                new FileOutputStream(cheminFichier)) {
            doc.write(out);
        }
        doc.close();
        System.out.println("DOCX genere : " + cheminFichier);
    }

    private void para(XWPFDocument doc, String texte,
                      boolean bold, int size, String color,
                      ParagraphAlignment align) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(align);
        XWPFRun r = p.createRun();
        r.setText(texte);
        r.setBold(bold);
        r.setFontSize(size);
        r.setColor(color);
        r.setFontFamily("Calibri");
    }
}