package loader;

import model.Salle;
import repository.SalleRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ExcelSalleLoader {

    private final String cheminFichier;

    public ExcelSalleLoader(String cheminFichier){
        this.cheminFichier = cheminFichier;
    }

    public void charger(SalleRepository salleRepo)throws IOException{
    	salleRepo.vider();

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(cheminFichier))){
            Sheet sheet = wb.getSheet("salles");

            if (sheet == null)
                throw new IllegalArgumentException("Feuille 'salles' introuvable dans le fichier Excel.");

            int id = 1;
            boolean premiereLigne = true;

            for (Row row : sheet) {
                if (premiereLigne) { premiereLigne = false; continue; }

                Cell cellNom    = row.getCell(0);
                Cell cellNumero = row.getCell(1);

                if (cellNom == null || cellNom.getStringCellValue().isBlank()) continue;

                String nom    = cellNom.getStringCellValue().trim();
                String numero = cellNumero != null ? cellNumero.getStringCellValue().trim() : nom;

                Salle salle = new Salle(id++, nom, numero, true);
                salleRepo.sauvegarder(salle);
                System.out.println("Salle : " + nom + " | " + numero);
            }
        }
    }
}