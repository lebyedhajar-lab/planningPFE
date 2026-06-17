package loader;
import model.Etudiant;
import model.Filiere;
import model.Enseignant;
import repository.EtudiantRepository;
import repository.EnseignantRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelEtudiantLoader {
    private final String cheminFichier;

    public ExcelEtudiantLoader(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public void charger(EtudiantRepository etudiantRepo,
                        EnseignantRepository enseignantRepo) throws IOException {
    	etudiantRepo.vider();
        List<Filiere> filieres = new ArrayList<>();
        int filiereId = 1;

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(cheminFichier))) {
            Sheet sheet = wb.getSheet("etudiants");
            if (sheet == null)
                throw new IllegalArgumentException("Feuille 'etudiants' introuvable.");

            int id = 1;
            boolean premiereLigne = true;

            for (Row row : sheet) {
                if (premiereLigne) { premiereLigne = false; continue; }

                Cell cellNom       = row.getCell(0);
                Cell cellPrenom    = row.getCell(1);
                Cell cellFiliere   = row.getCell(2);
                Cell cellLangue    = row.getCell(3);
                Cell cellTitrePFE  = row.getCell(4);
                Cell cellEncadrant = row.getCell(5);

                if (cellNom == null || cellNom.getStringCellValue().isBlank()) continue;

                String nom      = cellNom.getStringCellValue().trim();
                String prenom   = cellPrenom != null ? cellPrenom.getStringCellValue().trim() : "";
                String langue   = cellLangue != null ? cellLangue.getStringCellValue().trim() : "français";
                String titrePFE = cellTitrePFE != null ? cellTitrePFE.getStringCellValue().trim() : "";
                
                //filiere 
                Filiere filiere = null;
                if (cellFiliere != null && !cellFiliere.getStringCellValue().isBlank()) {
                    String nomFiliere = cellFiliere.getStringCellValue().trim();
                    for (Filiere f : filieres) {
                        if (f.getNom().equalsIgnoreCase(nomFiliere)) {
                            filiere = f;
                            break;
                        }
                    }
                    if (filiere == null) {
                        filiere = new Filiere(filiereId++, 0, nomFiliere);
                        filieres.add(filiere);
                        System.out.println(" Filière créée : " + nomFiliere);
                    }
                }
                
                //encadrant 
                Enseignant encadrant = null;
                if (cellEncadrant != null && !cellEncadrant.getStringCellValue().isBlank()) {
                    String nomEncadrant = cellEncadrant.getStringCellValue().trim();

                    for (Enseignant e : enseignantRepo.chargerTous()) {
                        String v1 = e.getNom() + " " + e.getPrenom(); 
                        String v2 = e.getPrenom() + " " + e.getNom(); 
                        if (v1.equalsIgnoreCase(nomEncadrant) || v2.equalsIgnoreCase(nomEncadrant)) {
                            encadrant = e;
                            break;
                        }
                    }
                    if (encadrant == null) {
                        System.out.println(" Encadrant introuvable : '"+ nomEncadrant + "' pour : " + nom + " " + prenom);
                        System.out.println("   Enseignants disponibles :");
                        for (Enseignant e : enseignantRepo.chargerTous()) {
                            System.out.println("     → '" + e.getNom() + " " + e.getPrenom() + "'");
                        }
                    }
                } else {
                    System.out.println(" Colonne encadrant vide pour : " + nom + " " + prenom);
                }

                Etudiant etudiant = new Etudiant(id++, nom, prenom, langue, filiere, encadrant, titrePFE);
                etudiantRepo.sauvegarder(etudiant);

                if (filiere != null) filiere.incrementerNbEtudiants();

                System.out.println( nom + " " + prenom+ " | " + langue+ " | " + (filiere  != null ? filiere.getNom()   : "—")+ " | encadrant: " + (encadrant != null ? encadrant.getNom() + " "+ encadrant.getPrenom() : " NULL"));
            }
        }
    }
}