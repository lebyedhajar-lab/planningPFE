package loader;

import Config.ConfigPlanning;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class ExcelConfigLoader {

    private final String cheminFichier;

    public ExcelConfigLoader(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public ConfigPlanning chargerConfig() throws IOException {
        ConfigPlanning config = new ConfigPlanning();

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(cheminFichier))) {
            Sheet sheet = wb.getSheet("configs");

            if (sheet == null)
                throw new IllegalArgumentException("Feuille 'configs' introuvable.");

            for (Row row : sheet) {
                Cell cellParam  = row.getCell(0);
                Cell cellValeur = row.getCell(1);
                if (cellParam == null || cellValeur == null) continue;

                String parametre = cellParam.getStringCellValue().trim();

                switch (parametre) {
                    case "dureeSoutenanceMin":
                        config.setDureeSoutenanceMin((int) cellValeur.getNumericCellValue());
                        break;
                    case "nbMembresJury":
                        config.setNbMembresJury((int) cellValeur.getNumericCellValue());
                        break;
                    case "heureDebutJournee":
                        config.setHeureDebutJournee(LocalTime.parse(cellValeur.getStringCellValue().trim()));
                        break;
                    case "heureFinJournee":
                        config.setHeureFinJournee(LocalTime.parse(cellValeur.getStringCellValue().trim()));
                        break;
                    case "heureDebutPause":
                        config.setHeureDebutPause(LocalTime.parse(cellValeur.getStringCellValue().trim()));
                        break;
                    case "heureFinPause":
                        config.setHeureFinPause(LocalTime.parse(cellValeur.getStringCellValue().trim()));
                        break;
                    case "pauseMinimale":
                        config.setPauseMinimale((int) cellValeur.getNumericCellValue());
                        break;
                    case "minSoutenanceParProfParJour":
                        config.setMinSoutenancesParProfParJour((int) cellValeur.getNumericCellValue());
                        break;
                    case "maxSoutenanceParProfParJour":
                        config.setMaxSoutenancesParProfParJour((int) cellValeur.getNumericCellValue());
                        break;
                    case "nbJoursSoutenances":
                        config.setNbJoursSoutenances((int) cellValeur.getNumericCellValue());
                        break;
                    case "dateDebut":
                        config.setDateDebut(LocalDate.parse(cellValeur.getStringCellValue().trim()));
                        break;
                    default:
                        System.out.println("⚠️ Paramètre inconnu : " + parametre);
                        break;
                }
            }
        }

        config.valider();
        System.out.println("Config chargée : " + config);
        return config;
    }
}