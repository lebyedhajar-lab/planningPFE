package loader;

import model.Etudiant;
import model.Filiere;
import model.Enseignant;
import repository.EtudiantRepository;
import repository.FiliereRepository;
import repository.EnseignantRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ExcelEtudiantLoader {

    private final String cheminFichier;

    public ExcelEtudiantLoader(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public void charger(EtudiantRepository etudiantRepo,
                        FiliereRepository filiereRepo,
                        EnseignantRepository enseignantRepo) throws IOException {

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(cheminFichier))) {
            Sheet sheet = wb.getSheet("etudiants");

            if (sheet == null)
                throw new IllegalArgumentException("Feuille 'etudiants' introuvable dans le fichier Excel.");

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

                // Trouver la filière par nom
                Filiere filiere = null;
                if (cellFiliere != null) {
                    String nomFiliere = cellFiliere.getStringCellValue().trim();
                    for (Filiere f : filiereRepo.chargerTous()) {
                        if (f.getNom().equalsIgnoreCase(nomFiliere)) {
                            filiere = f;
                            break;
                        }
                    }
                }

                // Trouver l'encadrant par nom
                Enseignant encadrant = null;
                if (cellEncadrant != null) {
                    String nomEncadrant = cellEncadrant.getStringCellValue().trim();
                    for (Enseignant e : enseignantRepo.chargerTous()) {
                        if ((e.getNom() + " " + e.getPrenom()).equalsIgnoreCase(nomEncadrant)) {
                            encadrant = e;
                            break;
                        }
                    }
                }

                Etudiant etudiant = new Etudiant(id++, nom, prenom, langue, filiere, encadrant, titrePFE);
                etudiantRepo.sauvegarder(etudiant);
                System.out.println(  nom + " " + prenom + " | " + langue + " | " + (filiere != null ? filiere.getNom() : "—"));
            }
        }
    }
}