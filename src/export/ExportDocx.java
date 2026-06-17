package export;

import model.*;
import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExportDocx {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String BLEU_FONCE = "1F3864";
    private static final String BLANC      = "FFFFFF";
    private static final String BLEU_TITRE = "2E5496";

    private static final String[] PALETTE_PROFS = {
        "FFD6D6", "FFE8CC", "FFFACC", "D6F5D6", "CCF5FF",
        "D6D6FF", "F5CCF5", "FFB3B3", "FFDAB3", "FFF5B3",
        "B3FFB3", "B3EEFF", "B3B3FF", "EEB3EE", "FF9999",
        "FFCC99", "FFFF99", "99FF99", "99EEFF", "9999FF",
        "EE99EE", "FFD700", "98FB98", "87CEEB", "DDA0DD",
        "F08080", "90EE90", "ADD8E6", "FFB6C1", "FFDEAD",
        "E0FFFF", "D8BFD8"
    };

    private static final String[] PALETTE_FILIERES = {
        "DDEBF7", "E2EFDA", "FCE4D6", "FFF2CC",
        "EDE7F6", "FCE4EC", "E0F7FA", "F3E5F5",
        "E8F5E9", "FFF8E1", "E3F2FD", "FBE9E7"
    };

    private static final String[] PALETTE_CRENEAUX = {
        "EBF5FB", "E9F7EF", "FEF9E7", "FDEDEC",
        "F0F3FF", "E8D5F5", "FFF3E0", "E8F5E9",
        "E3F2FD", "FCE4EC"
    };

    private static String anneeUniversitaire() {
        int annee = LocalDate.now().getYear();


        int mois = LocalDate.now().getMonthValue();
        int debut = (mois >= 9) ? annee : annee - 1;
        return "Annee Universitaire " + debut + "/" + (debut + 1);
    }

    private static String cleProf(Enseignant e) {
        if (e == null) return "";
        return (e.getNom() + " " + e.getPrenom()).trim().toUpperCase();
    }

    public void generer(List<Soutenance> soutenances,String cheminFichier) throws IOException {

        XWPFDocument doc = new XWPFDocument();

       
        Map<String, String> couleurParProf = new LinkedHashMap<>();
        Map<String, String> nomAffichParProf = new LinkedHashMap<>();
        int profIdx = 0;

        for (Soutenance s : soutenances) {
            if (s.getJury() == null) continue;

            // Encadrant
            Enseignant enc = s.getJury().getEncadrant();
            if (enc != null) {
                String cle = cleProf(enc);
                if (!couleurParProf.containsKey(cle)) {
                    couleurParProf.put(cle,
                        PALETTE_PROFS[profIdx % PALETTE_PROFS.length]);
                    nomAffichParProf.put(cle,
                        enc.getNom() + " " + enc.getPrenom());
                    profIdx++;
                }
            }

            // Membres
            if (s.getJury().getMembres() != null) {
                for (Enseignant m : s.getJury().getMembres()) {
                    if (m != null) {
                        String cle = cleProf(m);
                        if (!couleurParProf.containsKey(cle)) {
                            couleurParProf.put(cle,
                                PALETTE_PROFS[profIdx % PALETTE_PROFS.length]);
                            nomAffichParProf.put(cle,
                                m.getNom() + " " + m.getPrenom());
                            profIdx++;
                        }
                    }
                }
            }
        }

        Map<String, String> couleurParFiliere = new LinkedHashMap<>();
        int filIdx = 0;
        for (Soutenance s : soutenances) {
            if (s.getEtudiant() != null && s.getEtudiant().getFiliere() != null) {
                String nom = s.getEtudiant().getFiliere().getNom();
                if (!couleurParFiliere.containsKey(nom)) {
                    couleurParFiliere.put(nom, PALETTE_FILIERES[filIdx % PALETTE_FILIERES.length]);
                    filIdx++;
                }
            }
        }

        Map<LocalTime, String> couleurParCreneau = new LinkedHashMap<>();
        int crIdx = 0;
        for (Soutenance s : soutenances) {
            if (s.getCreneau() != null) {
                LocalTime h = s.getCreneau().getHeureDebut();
                if (!couleurParCreneau.containsKey(h)) {
                    couleurParCreneau.put(h,
                        PALETTE_CRENEAUX[crIdx % PALETTE_CRENEAUX.length]);
                    crIdx++;
                }
            }
        }

        para(doc, "Ecole Nationale des Sciences Appliquees - Al Hoceima",
            true, 12, BLEU_TITRE, ParagraphAlignment.CENTER);
        para(doc, "Departement Mathematiques et Informatique",
            false, 11, BLEU_TITRE, ParagraphAlignment.CENTER);
        para(doc, "", false, 4, "000000", ParagraphAlignment.CENTER);
        para(doc, "Planning des soutenances des Projets de Fin d'Etude",
            true, 14, BLEU_FONCE, ParagraphAlignment.CENTER);

        para(doc, anneeUniversitaire(),
            false, 11, BLEU_FONCE, ParagraphAlignment.CENTER);
        para(doc, "", false, 4, "000000", ParagraphAlignment.CENTER);

        XWPFTable table = doc.createTable(soutenances.size() + 1, 9);
        table.setWidth("100%");

        String[] headers = {
            "ID", "Encadrant", "Membre jury 1",
            "Membre jury 2", "Date", "Heure",
            "Salle", "Nom etudiant", "Prenom etudiant"
        };
        XWPFTableRow headerRow = table.getRow(0);
        for (int j = 0; j < headers.length; j++) {
            XWPFTableCell cell = headerRow.getCell(j);
            cell.setColor(BLEU_FONCE);
            cell.getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = cell.getParagraphs().get(0).createRun();
            run.setText(headers[j]);
            run.setBold(true);
            run.setColor(BLANC);
            run.setFontSize(9);
            run.setFontFamily("Calibri");
        }

        for (int i = 0; i < soutenances.size(); i++) {
            Soutenance s = soutenances.get(i);

            String bgCreneau = BLANC;
            if (s.getCreneau() != null)
                bgCreneau = couleurParCreneau.getOrDefault(
                    s.getCreneau().getHeureDebut(), BLANC);

            String bgEncadrant = BLANC;
            String bgMembre1   = BLANC;
            String bgMembre2   = BLANC;

            if (s.getJury() != null) {
                Enseignant enc = s.getJury().getEncadrant();
                if (enc != null)
                    bgEncadrant = couleurParProf.getOrDefault(
                        cleProf(enc), BLANC);

                List<Enseignant> membres = s.getJury().getMembres();
                if (membres != null) {
                    if (membres.size() > 0 && membres.get(0) != null)
                        bgMembre1 = couleurParProf.getOrDefault(
                            cleProf(membres.get(0)), BLANC);
                    if (membres.size() > 1 && membres.get(1) != null)
                        bgMembre2 = couleurParProf.getOrDefault(
                            cleProf(membres.get(1)), BLANC);
                }
            }

            // Couleur filière
            String nomFiliere = s.getEtudiant() != null && s.getEtudiant().getFiliere() != null ? s.getEtudiant().getFiliere().getNom() : "";
            String bgFiliere = couleurParFiliere.getOrDefault(nomFiliere, BLANC);

            // Valeurs
            List<Enseignant> membres = s.getJury() != null
                ? s.getJury().getMembres() : new ArrayList<>();
            String encadrant = s.getJury() != null
                && s.getJury().getEncadrant() != null
                ? s.getJury().getEncadrant().getNom() + " "
                  + s.getJury().getEncadrant().getPrenom() : "-";
            String membre1 = membres != null && membres.size() > 0
                && membres.get(0) != null
                ? membres.get(0).getNom() + " "
                  + membres.get(0).getPrenom() : "-";
            String membre2 = membres != null && membres.size() > 1
                && membres.get(1) != null
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

            String[] values   = {
                String.valueOf(i + 1),
                encadrant, membre1, membre2,
                date, heure, salle, nom, prenom
            };
            String[] bgColors = {
                bgCreneau,  
                bgEncadrant, 
                bgMembre1,  
                bgMembre2,   
                bgCreneau,   
                bgCreneau,   
                bgCreneau,   
                bgFiliere,   
                bgFiliere    
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
                XWPFRun run = cell.getParagraphs().get(0).createRun();
                run.setText(values[j]);
                run.setFontSize(9);
                run.setFontFamily("Calibri");
            }
        }

        try (FileOutputStream out = new FileOutputStream(cheminFichier)) {
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