package export;

import model.Enseignant;
import model.Etudiant;
import model.Filiere;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;

public class AffectationExporter {

 
    private static final String[] COULEURS = {
        "C6EFCE", 
        "BDD7EE", 
        "FFE699", 
        "F4CCCC", 
        "D9D2E9", 
        "FCE5CD"  
    };

    public void exporter(List<Etudiant> etudiants,
                         String cheminFichier)
            throws IOException, InvalidFormatException {

        XWPFDocument document = new XWPFDocument();

        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setTop(BigInteger.valueOf(720));
        pageMar.setBottom(BigInteger.valueOf(720));
        pageMar.setLeft(BigInteger.valueOf(1080));
        pageMar.setRight(BigInteger.valueOf(1080));

        int totalWidth = 10080;

        ajouterEntete(document, totalWidth);

       
        Map<Integer, Enseignant> encadrants = new LinkedHashMap<>();
        Map<Integer, List<Etudiant>> affectations = new LinkedHashMap<>();

        for (Etudiant e : etudiants) {
            if (e.getEncadrant() == null) continue;
            int id = e.getEncadrant().getId();
            encadrants.put(id, e.getEncadrant());
            affectations.computeIfAbsent(id, k -> new ArrayList<>()).add(e);
        }

        Map<String, String> couleurParFiliere = new LinkedHashMap<>();
        int idx = 0;
        for (Etudiant e : etudiants) {
            if (e.getFiliere() == null) continue;
            String nom = e.getFiliere().getNom();
            if (!couleurParFiliere.containsKey(nom)) {
                couleurParFiliere.put(nom, COULEURS[idx % COULEURS.length]);
                idx++;
            }
        }

        ajouterLegende(document, couleurParFiliere);

  
        int maxEtudiants = 0;
        for (List<Etudiant> list : affectations.values()) {
            maxEtudiants = Math.max(maxEtudiants, list.size());
        }

        int nbCols = 1 + maxEtudiants; 
        XWPFTable table = document.createTable(1 + affectations.size(), nbCols);
        table.setWidth(totalWidth);

        int colEncadrant = 2500;
        int colEtudiant = (totalWidth - colEncadrant) / maxEtudiants;

        XWPFTableRow rowHeader = table.getRow(0);
        setCellule(rowHeader.getCell(0), "Encadrant", colEncadrant, "1F497D", true, "FFFFFF");
        for (int i = 0; i < maxEtudiants; i++) {
            setCellule(rowHeader.getCell(i + 1),
                "Etudiant " + (i + 1), colEtudiant, "1F497D", true, "FFFFFF");
        }

        int rowIdx = 1;
        for (Map.Entry<Integer, Enseignant> entry : encadrants.entrySet()) {
            Enseignant enc = entry.getValue();
            List<Etudiant> ses_etudiants = affectations.get(entry.getKey());

            XWPFTableRow row = table.getRow(rowIdx);

            // Cellule encadrant
            setCellule(row.getCell(0),
                enc.getNom() + " " + enc.getPrenom(),
                colEncadrant, "D9D9D9", true, "000000");

            // Cellules étudiants
            for (int i = 0; i < maxEtudiants; i++) {
                if (i < ses_etudiants.size()) {
                    Etudiant et = ses_etudiants.get(i);
                    String filiere = et.getFiliere() != null
                        ? et.getFiliere().getNom() : "";
                    String couleur = couleurParFiliere.getOrDefault(filiere, "FFFFFF");
                    setCellule(row.getCell(i + 1),
                        et.getNom() + " " + et.getPrenom(),
                        colEtudiant, couleur, false, "000000");
                } else {
                    setCellule(row.getCell(i + 1), "", colEtudiant, "FFFFFF", false, "000000");
                }
            }
            rowIdx++;
        }

        try (FileOutputStream out = new FileOutputStream(cheminFichier)) {
            document.write(out);
        }
        document.close();
    }


    private void ajouterEntete(XWPFDocument doc, int totalWidth)
            throws IOException, InvalidFormatException {

        XWPFTable tableHeader = doc.createTable(1, 3);
        tableHeader.setWidth(totalWidth);
        supprimerBordures(tableHeader);

        int logoCol = 1400;
        int textCol = totalWidth - 2 * logoCol;

        // Logo université
        XWPFTableCell cellLeft = tableHeader.getRow(0).getCell(0);
        setCellWidth(cellLeft, logoCol);
        try (InputStream is = getClass().getResourceAsStream("/resources/logo_universite.png")) {
            if (is != null) {
                XWPFRun r = cellLeft.getParagraphs().get(0).createRun();
                r.addPicture(is, XWPFDocument.PICTURE_TYPE_PNG,
                    "logo_universite.png", Units.toEMU(75), Units.toEMU(75));
            }
        }

        // Texte central
        XWPFTableCell cellCenter = tableHeader.getRow(0).getCell(1);
        setCellWidth(cellCenter, textCol);

        String[] lignes = {
            "Ecole Nationale des Sciences Appliqu\u00e9es - Al Hoceima",
            "D\u00e9partement Math\u00e9matiques et Informatique",
            "Affectation des encadrants de Projet de Fin d\u2019Etude",
        };
        boolean[] bolds = {false, false, true};

        for (int i = 0; i < lignes.length; i++) {
            XWPFParagraph p = i == 0
                ? cellCenter.getParagraphs().get(0)
                : cellCenter.addParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r = p.createRun();
            r.setText(lignes[i]);
            r.setBold(bolds[i]);
            r.setFontSize(11);
        }

        // Année universitaire
        int annee = LocalDate.now().getYear();
        int mois = LocalDate.now().getMonthValue();
        String anneeUniv = mois >= 9
            ? annee + "/" + (annee + 1)
            : (annee - 1) + "/" + annee;

        XWPFParagraph pAnnee = cellCenter.addParagraph();
        pAnnee.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rAnnee = pAnnee.createRun();
        rAnnee.setText("Ann\u00e9e Universitaire " + anneeUniv);
        rAnnee.setFontSize(11);

        // Logo ENSAH
        XWPFTableCell cellRight = tableHeader.getRow(0).getCell(2);
        setCellWidth(cellRight, logoCol);
        cellRight.getParagraphs().get(0).setAlignment(ParagraphAlignment.RIGHT);
        try (InputStream is = getClass().getResourceAsStream("/resources/logo_ensah.png")) {
            if (is != null) {
                XWPFRun r = cellRight.getParagraphs().get(0).createRun();
                r.addPicture(is, XWPFDocument.PICTURE_TYPE_PNG,
                    "logo_ensah.png", Units.toEMU(75), Units.toEMU(75));
            }
        }

        // Ligne vide
        XWPFParagraph pVide = doc.createParagraph();
        pVide.createRun().setText(" ");
    }

    private void ajouterLegende(XWPFDocument doc, Map<String, String> couleurs) {
        XWPFParagraph p = doc.createParagraph();
        for (Map.Entry<String, String> entry : couleurs.entrySet()) {
            XWPFRun r = p.createRun();
            r.setText("  " + entry.getKey() + "  ");
            r.setFontSize(11);
        }
        // Légende via tableau coloré
        XWPFTable tableLeg = doc.createTable(1, couleurs.size());
        supprimerBordures(tableLeg);
        int i = 0;
        for (Map.Entry<String, String> entry : couleurs.entrySet()) {
            XWPFTableCell cell = tableLeg.getRow(0).getCell(i);
            setCellule(cell, entry.getKey(), 2000, entry.getValue(), false, "000000");
            i++;
        }
        doc.createParagraph().createRun().setText(" ");
    }

    private void setCellule(XWPFTableCell cell, String texte,
                             int width, String bgColor,
                             boolean bold, String textColor) {
        setCellWidth(cell, width);
        CTTcPr tcPr = cell.getCTTc().isSetTcPr()
            ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTShd shd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
        shd.setVal(STShd.CLEAR);
        shd.setFill(bgColor);
        shd.setColor("auto");

        XWPFParagraph p = cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setText(texte);
        r.setBold(bold);
        r.setFontSize(11);
        r.setColor(textColor);
    }

    private void setCellWidth(XWPFTableCell cell, int twips) {
        CTTcPr tcPr = cell.getCTTc().isSetTcPr()
            ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTTblWidth w = tcPr.isSetTcW() ? tcPr.getTcW() : tcPr.addNewTcW();
        w.setW(BigInteger.valueOf(twips));
        w.setType(STTblWidth.DXA);
    }

    private void supprimerBordures(XWPFTable table) {
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        if (tblPr == null) tblPr = table.getCTTbl().addNewTblPr();
        CTTblBorders borders = tblPr.isSetTblBorders()
            ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        for (CTBorder b : new CTBorder[]{
                borders.addNewTop(), borders.addNewBottom(),
                borders.addNewLeft(), borders.addNewRight(),
                borders.addNewInsideH(), borders.addNewInsideV()}) {
            b.setVal(STBorder.NONE);
            b.setSz(BigInteger.ZERO);
            b.setSpace(BigInteger.ZERO);
            b.setColor("FFFFFF");
        }
    }
}