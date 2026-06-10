package loader;

import model.Enseignant;
import repository.EnseignantRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ExcelEnseignantLoader {
    private final String cheminFichier;

    public ExcelEnseignantLoader(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public void charger(EnseignantRepository enseignantRepo) throws IOException {
    	
    	enseignantRepo.vider();

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(cheminFichier))) {
        	Sheet sheet = wb.getSheet("profs"); 

            if (sheet == null)
            	throw new IllegalArgumentException("Feuille 'profs' introuvable.");

            int id = 1;
            boolean premiereLigne = true;

            for (Row row : sheet) {
                if (premiereLigne) {premiereLigne = false; continue; }

                Cell cellNom        = row.getCell(0);
                Cell cellPrenom     = row.getCell(1);
                Cell cellSpecialite = row.getCell(2);

                if (cellNom == null || cellNom.getStringCellValue().isBlank()) continue;

                String nom        = cellNom.getStringCellValue().trim();
                String prenom     = cellPrenom.getStringCellValue().trim();
                String specialite = cellSpecialite.getStringCellValue().trim();

                // Anglophone si spécialité = Anglais
                boolean estAnglophone = specialite.equalsIgnoreCase("Anglais");

                Enseignant enseignant = new Enseignant(
                    id++, 0, nom, prenom, estAnglophone, specialite
                );

                enseignantRepo.sauvegarder(enseignant);
                System.out.println( nom + " " + prenom
                                 + " | " + specialite
                                 + (estAnglophone ? " [ANGLOPHONE]" : ""));
            }
        }
    }
}