package export;

import java.io.File;
import model.Soutenance;
import model.Enseignant;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.math.BigInteger;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
// CTTabStop, STTabJc, CTPPr, CTTabs sont inclus dans le wildcard ci-dessus

public class FicheNotationExporter {

    // ── HELPERS ───────────────────────────────────────────────────────────────

    /** Paragraph vide (ligne vide) */
    private void ajouterLigneVide(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(" ");
        r.setFontSize(11);
    }

    /** Paragraphe texte simple */
    private void ajouterLigne(XWPFDocument doc, String texte) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(texte);
        r.setFontSize(12);
    }

    /** Crée un run bold+underline dans un paragraphe existant */
    private XWPFRun runBoldUnderline(XWPFParagraph p, String texte) {
        XWPFRun r = p.createRun();
        r.setText(texte);
        r.setBold(true);
        r.setUnderline(UnderlinePatterns.SINGLE);
        r.setFontSize(12);
        return r;
    }

    /** Applique une bordure complète à toutes les cellules d'un tableau */
    private void setBorduresTableau(XWPFTable table) {
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        if (tblPr == null) tblPr = table.getCTTbl().addNewTblPr();
        CTTblBorders borders = tblPr.isSetTblBorders()
            ? tblPr.getTblBorders() : tblPr.addNewTblBorders();

        for (CTBorder b : new CTBorder[]{
                borders.addNewTop(), borders.addNewBottom(),
                borders.addNewLeft(), borders.addNewRight(),
                borders.addNewInsideH(), borders.addNewInsideV()}) {
            b.setVal(STBorder.SINGLE);
            b.setSz(BigInteger.valueOf(4));
            b.setSpace(BigInteger.valueOf(0));
            b.setColor("000000");
        }
    }

    /** Largeur d'une cellule en twips (DXA) */
    private void setCellWidth(XWPFTableCell cell, int twips) {
        CTTcPr tcPr = cell.getCTTc().isSetTcPr()
            ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTTblWidth w = tcPr.isSetTcW() ? tcPr.getTcW() : tcPr.addNewTcW();
        w.setW(BigInteger.valueOf(twips));
        w.setType(STTblWidth.DXA);
    }

    /**
     * Crée une ligne jury : "– Pr. NOM" à gauche et "Président/Rapporteur" à droite
     * grâce à un tab stop aligné à droite (comme dans le modèle du prof).
     */
    private void ajouterLigneJury(XWPFDocument doc, String nomPart, String role) {
        XWPFParagraph p = doc.createParagraph();

        // Tab stop à droite à ~10080 twips (fin de la zone de texte)
        CTPPr pPr = p.getCTP().isSetPPr() ? p.getCTP().getPPr() : p.getCTP().addNewPPr();
        CTTabs tabs = pPr.isSetTabs() ? pPr.getTabs() : pPr.addNewTabs();
        CTTabStop tab = tabs.addNewTab();
        tab.setVal(STTabJc.RIGHT);
        tab.setPos(BigInteger.valueOf(9800));

        XWPFRun rNom = p.createRun();
        rNom.setText(nomPart);
        rNom.setFontSize(12);

        XWPFRun rTab = p.createRun();
        rTab.addTab();

        XWPFRun rRole = p.createRun();
        rRole.setText(role);
        rRole.setFontSize(12);
    }

    // ── MÉTHODE PRINCIPALE ────────────────────────────────────────────────────

    public void genererFiche(Soutenance soutenance, String cheminFichier)
            throws IOException, InvalidFormatException {

        XWPFDocument document = new XWPFDocument();

        // Marges de page (A4, marges réduites pour ressembler au modèle)
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setTop(BigInteger.valueOf(720));
        pageMar.setBottom(BigInteger.valueOf(720));
        pageMar.setLeft(BigInteger.valueOf(1080));
        pageMar.setRight(BigInteger.valueOf(1080));

        // ── EN-TÊTE : tableau 1 ligne x 3 colonnes (logo | texte | logo) ──────
        // Largeur totale ~9360 twips (page A4 avec marges 1080 gauche+droite = 12240-2160 = 10080)
        int totalWidth = 10080;
        int logoCol   = 1400;
        int textCol   = totalWidth - 2 * logoCol; // 7280

        XWPFTable tableHeader = document.createTable(1, 3);
        tableHeader.setWidth(totalWidth);
        CTTblPr hPr = tableHeader.getCTTbl().getTblPr();
        // Supprimer les bordures de l'en-tête (invisible)
        CTTblBorders hb = hPr.isSetTblBorders() ? hPr.getTblBorders() : hPr.addNewTblBorders();
        for (CTBorder b : new CTBorder[]{
                hb.addNewTop(), hb.addNewBottom(),
                hb.addNewLeft(), hb.addNewRight(),
                hb.addNewInsideH(), hb.addNewInsideV()}) {
            b.setVal(STBorder.NONE);
            b.setSz(BigInteger.ZERO);
            b.setSpace(BigInteger.ZERO);
            b.setColor("FFFFFF");
        }

        // Cellule gauche — Logo Université
        XWPFTableCell cellLogoLeft = tableHeader.getRow(0).getCell(0);
        setCellWidth(cellLogoLeft, logoCol);
        XWPFParagraph pLogoLeft = cellLogoLeft.getParagraphs().get(0);
        pLogoLeft.setAlignment(ParagraphAlignment.LEFT);
        try (InputStream isUniv = getClass().getResourceAsStream("/resources/logo_universite.png")) {
            if (isUniv != null) {
                XWPFRun rLogo = pLogoLeft.createRun();
                rLogo.addPicture(isUniv,
                    XWPFDocument.PICTURE_TYPE_PNG, "logo_universite.png",
                    Units.toEMU(75), Units.toEMU(75));
            }
        }

        // Cellule centrale — Textes centrés
        XWPFTableCell cellTexte = tableHeader.getRow(0).getCell(1);
        setCellWidth(cellTexte, textCol);

        // "UNIVERSITE ABDELMALEK ESSAADI"
        XWPFParagraph pU = cellTexte.getParagraphs().get(0);
        pU.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rU = pU.createRun();
        rU.setText("UNIVERSITE ABDELMALEK ESSAADI");
        rU.setBold(true);
        rU.setFontSize(13);

        // "Ecole Nationale..."
        XWPFParagraph pE = cellTexte.addParagraph();
        pE.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rE = pE.createRun();
        rE.setText("Ecole Nationale des Sciences Appliqu\u00e9es d\u2019Al-Hoceima - Maroc");
        rE.setFontSize(10);

        // "Département..."
        XWPFParagraph pD = cellTexte.addParagraph();
        pD.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rD = pD.createRun();
        rD.setText("D\u00e9partement de Math\u00e9matiques et Informatique");
        rD.setBold(true);
        rD.setFontSize(12);

        // "Fiche d'évaluation..."
        XWPFParagraph pF = cellTexte.addParagraph();
        pF.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rF = pF.createRun();
        rF.setText("Fiche d\u2019\u00e9valuation du Projet de Fin d\u2019\u00c9tude");
        rF.setFontSize(11);

        // "Année Universitaire"
        XWPFParagraph pA = cellTexte.addParagraph();
        pA.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rA = pA.createRun();
        int annee = java.time.LocalDate.now().getYear();
        int mois = java.time.LocalDate.now().getMonthValue();
        String anneeUniv = mois >= 9
            ? annee + "-" + (annee + 1)
            : (annee - 1) + "-" + annee;
        rA.setText("Ann\u00e9e Universitaire : " + anneeUniv);
        rA.setFontSize(11);
        

        // Cellule droite — Logo ENSAH
        XWPFTableCell cellLogoRight = tableHeader.getRow(0).getCell(2);
        setCellWidth(cellLogoRight, logoCol);
        XWPFParagraph pLogoRight = cellLogoRight.getParagraphs().get(0);
        pLogoRight.setAlignment(ParagraphAlignment.RIGHT);
        try (InputStream isEnsah = getClass().getResourceAsStream("/resources/logo_ensah.png")) {
            if (isEnsah != null) {
                XWPFRun rLogo = pLogoRight.createRun();
                rLogo.addPicture(isEnsah,
                    XWPFDocument.PICTURE_TYPE_PNG, "logo_ensah.png",
                    Units.toEMU(75), Units.toEMU(75));
            }
        }

        ajouterLigneVide(document);

        // ── NOM ÉTUDIANT ──────────────────────────────────────────────────────
        XWPFParagraph pNom = document.createParagraph();
        runBoldUnderline(pNom, "Nom - Pr\u00e9nom de l\u2019\u00e9l\u00e8ve ing\u00e9nieur :");

        XWPFParagraph pNomVal = document.createParagraph();
        XWPFRun rNomVal = pNomVal.createRun();
        rNomVal.setText("    \u2013 "
            + soutenance.getEtudiant().getNom()
            + " " + soutenance.getEtudiant().getPrenom());
        rNomVal.setFontSize(12);

     // ── FILIÈRE ───────────────────────────────────────────────────────────
        XWPFParagraph pFil = document.createParagraph();
        runBoldUnderline(pFil, "Filière :");

        XWPFRun rFil = pFil.createRun();
        rFil.setText("     " + soutenance.getEtudiant().getFiliere().getNom());
        rFil.setFontSize(12);

        // ── TITRE PFE ─────────────────────────────────────────────────────────
        XWPFParagraph pTitre = document.createParagraph();
        runBoldUnderline(pTitre, "Intitul\u00e9 du rapport :");

        XWPFParagraph pTitreVal = document.createParagraph();
        XWPFRun rTitreVal = pTitreVal.createRun();
        rTitreVal.setText("    \u2013 " + soutenance.getEtudiant().getTitrePFE());
        rTitreVal.setFontSize(12);

        // ── ENCADRANT ─────────────────────────────────────────────────────────
        XWPFParagraph pEnc = document.createParagraph();
        runBoldUnderline(pEnc, "L\u2019encadrant (e) interne:");

        XWPFParagraph pEncVal = document.createParagraph();
        XWPFRun rEncVal = pEncVal.createRun();
        rEncVal.setText("    \u2013 Pr.  "
            + soutenance.getJury().getEncadrant().getNom()
            + " " + soutenance.getJury().getEncadrant().getPrenom());
        rEncVal.setFontSize(12);

        // ── MEMBRES DU JURY ───────────────────────────────────────────────────
        XWPFParagraph pJury = document.createParagraph();
        runBoldUnderline(pJury, "Membres du jury :");

        List<Enseignant> membres = soutenance.getJury().getMembres();

        // ── Jury : tirets comme dans le modèle du prof ────────────────────────
        // Ligne Président
        String nomPresident = membres.size() > 0
            ? membres.get(0).getNom() + " " + membres.get(0).getPrenom() : "………………………………………………………";
        ajouterLigneJury(document, "    \u2013  Pr.  " + nomPresident, "Pr\u00e9sident");

        // Ligne Rapporteur 1
        String nomRap1 = membres.size() > 1
            ? membres.get(1).getNom() + " " + membres.get(1).getPrenom() : "………………………………………………………";
        ajouterLigneJury(document, "    \u2013  Pr.  " + nomRap1, "Rapporteur");

        // Ligne Rapporteur 2 (encadrant)
        String nomRap2 = soutenance.getJury().getEncadrant().getNom()
            + " " + soutenance.getJury().getEncadrant().getPrenom();
        ajouterLigneJury(document, "    \u2013  Pr.  " + nomRap2, "Rapporteur");

        ajouterLigneVide(document);

        // ── NOTE DU CONTENU ───────────────────────────────────────────────────
        XWPFParagraph pNoteC = document.createParagraph();
        runBoldUnderline(pNoteC, "Note du Contenu");
        XWPFRun rNoteCStar = pNoteC.createRun();
        rNoteCStar.setText(" ");
        rNoteCStar.setBold(false);
        XWPFRun rNoteCAst = pNoteC.createRun();
        rNoteCAst.setText("*");
        rNoteCAst.setItalic(true);
        rNoteCAst.setFontSize(12);
        XWPFRun rNoteCParenth = pNoteC.createRun();
        rNoteCParenth.setText("(En prenant en compte l\u2019appr\u00e9ciation de l\u2019entreprise)");
        rNoteCParenth.setBold(true);
        rNoteCParenth.setFontSize(12);

        XWPFParagraph pC = document.createParagraph();
        XWPFRun rC = pC.createRun();
        rC.setText("C  =");
        rC.setBold(true);
        rC.setFontSize(12);

        // ── NOTE DU MÉMOIRE ───────────────────────────────────────────────────
        XWPFParagraph pNoteM = document.createParagraph();
        runBoldUnderline(pNoteM, "Note du M\u00e9moire");

        XWPFParagraph pM = document.createParagraph();
        XWPFRun rM = pM.createRun();
        rM.setText("M  =");
        rM.setBold(true);
        rM.setFontSize(12);

        // ── NOTE DE LA SOUTENANCE ─────────────────────────────────────────────
        XWPFParagraph pNoteS = document.createParagraph();
        runBoldUnderline(pNoteS, "Note de la Soutenance");

        XWPFParagraph pS = document.createParagraph();
        XWPFRun rS = pS.createRun();
        rS.setText("S  =");
        rS.setBold(true);
        rS.setFontSize(12);

        ajouterLigneVide(document);

        // ── TABLEAU MOYENNE ───────────────────────────────────────────────────
        XWPFTable tableMoy = document.createTable(2, 1);
        tableMoy.setWidth(totalWidth);
        setBorduresTableau(tableMoy);

        // Ligne 1 — "MOYENNE"
        XWPFTableCell cellMoyTitle = tableMoy.getRow(0).getCell(0);
        setCellWidth(cellMoyTitle, totalWidth);
        XWPFParagraph pMoyTitle = cellMoyTitle.getParagraphs().get(0);
        pMoyTitle.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rMoyTitle = pMoyTitle.createRun();
        rMoyTitle.setText("MOYENNE");
        rMoyTitle.setBold(true);
        rMoyTitle.setFontSize(12);

        // Ligne 2 — Formule
        XWPFTableCell cellFormule = tableMoy.getRow(1).getCell(0);
        setCellWidth(cellFormule, totalWidth);
        XWPFParagraph pFormule = cellFormule.getParagraphs().get(0);
        pFormule.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun rFormule = pFormule.createRun();
        rFormule.setText("Moyenne   = C");
        rFormule.setBold(true);
        rFormule.setFontSize(12);
        XWPFRun rMult1 = pFormule.createRun();
        rMult1.setText(" \u00d7 ");
        rMult1.setBold(true);
        rMult1.setFontSize(12);
        XWPFRun rCoef1 = pFormule.createRun();
        rCoef1.setText("0,5 + M");
        rCoef1.setBold(true);
        rCoef1.setFontSize(12);
        XWPFRun rMult2 = pFormule.createRun();
        rMult2.setText(" \u00d7 ");
        rMult2.setBold(true);
        rMult2.setFontSize(12);
        XWPFRun rCoef2 = pFormule.createRun();
        rCoef2.setText("0,2 + S");
        rCoef2.setBold(true);
        rCoef2.setFontSize(12);
        XWPFRun rMult3 = pFormule.createRun();
        rMult3.setText(" \u00d7 ");
        rMult3.setBold(true);
        rMult3.setFontSize(12);
        XWPFRun rCoef3 = pFormule.createRun();
        rCoef3.setText("0,3  =  ");
        rCoef3.setBold(true);
        rCoef3.setFontSize(12);

        ajouterLigneVide(document);

        // ── DATE ──────────────────────────────────────────────────────────────
        XWPFParagraph pDate = document.createParagraph();
        XWPFRun rDate = pDate.createRun();
        rDate.setText("Le : \t……………………");
        rDate.setFontSize(12);

        ajouterLigneVide(document);

        // ── SIGNATURES ────────────────────────────────────────────────────────
        XWPFParagraph pSigLabel = document.createParagraph();
        XWPFRun rSigLabel = pSigLabel.createRun();
        rSigLabel.setText("Signature des membres du jury :");
        rSigLabel.setFontSize(12);

        ajouterLigneVide(document);

        // Tableau signatures : 1 ligne x 3 colonnes (un par membre)
        int sigColWidth = totalWidth / 3;
        XWPFTable tableSig = document.createTable(1, 3);
        tableSig.setWidth(totalWidth);
        // Bordures invisibles pour la table de signatures
        CTTblPr sPr = tableSig.getCTTbl().getTblPr();
        CTTblBorders sb = sPr.isSetTblBorders() ? sPr.getTblBorders() : sPr.addNewTblBorders();
        for (CTBorder b : new CTBorder[]{
                sb.addNewTop(), sb.addNewBottom(),
                sb.addNewLeft(), sb.addNewRight(),
                sb.addNewInsideH(), sb.addNewInsideV()}) {
            b.setVal(STBorder.NONE);
            b.setSz(BigInteger.ZERO);
            b.setSpace(BigInteger.ZERO);
            b.setColor("FFFFFF");
        }

        String[] nomsSig = {
            membres.size() > 0 ? "Pr.  " + membres.get(0).getNom() : "Pr.  ……………",
            membres.size() > 1 ? "Pr.  " + membres.get(1).getNom() : "Pr.  ……………",
            "Pr.  " + soutenance.getJury().getEncadrant().getNom()
        };

        for (int i = 0; i < 3; i++) {
            XWPFTableCell cell = tableSig.getRow(0).getCell(i);
            setCellWidth(cell, sigColWidth);
            XWPFParagraph p = cell.getParagraphs().get(0);
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r = p.createRun();
            r.setText(nomsSig[i]);
            r.setFontSize(12);
        }

        // ── SAUVEGARDER ───────────────────────────────────────────────────────
        try (FileOutputStream out = new FileOutputStream(cheminFichier)) {
            document.write(out);
        }
        document.close();
    }

    // ── GÉNÉRATION EN LOT ─────────────────────────────────────────────────────

    public void genererToutesLesFiches(List<Soutenance> soutenances, String dossier)
            throws IOException, InvalidFormatException {
        for (Soutenance s : soutenances) {
            String nomFichier = dossier
                + s.getEtudiant().getNom()
                + "_" + s.getEtudiant().getPrenom()
                + "_fiche.docx";
            genererFiche(s, nomFichier);
        }
    }

    public void genererPV(List<Soutenance> soutenances, String dossierRacine)
            throws IOException, InvalidFormatException {

        File dossierPV = new File(dossierRacine + File.separator + "PV_Soutenances");
        viderDossier(dossierPV);
        dossierPV.mkdirs();

        for (Soutenance s : soutenances) {
            Enseignant encadrant = s.getJury().getEncadrant();

            String nomDossier = "Prof_" + encadrant.getNom()
                              + "_" + encadrant.getPrenom();
            File dossierProf = new File(dossierPV, nomDossier);
            dossierProf.mkdirs();

            String nomFichier = "Fiche_Evaluation_PFE_"
                + s.getEtudiant().getNom()
                + "_" + s.getEtudiant().getPrenom()
                + ".docx";
            genererFiche(s, dossierProf + File.separator + nomFichier);
        }
    }

    private void viderDossier(File dossier) {
        if (dossier.exists() && dossier.listFiles() != null) {
            for (File f : dossier.listFiles()) {
                if (f.isDirectory()) viderDossier(f);
                f.delete();
            }
        }
    }
}