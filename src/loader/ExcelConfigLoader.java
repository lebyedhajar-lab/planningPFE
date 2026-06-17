package loader;

import Config.ConfigPlanning;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
                if (cellParam.getCellType() != CellType.STRING) continue;

                String parametre = cellParam.getStringCellValue().trim();
                if (parametre.isEmpty()) continue;

                switch (parametre){
                    case "dureeSoutenanceMin":
                        config.setDureeSoutenanceMin((int) getNumeric(cellValeur));
                        break;
                    case "nbMembresJury":
                        config.setNbMembresJury((int) getNumeric(cellValeur));
                        break;
                    case "heureDebutJournee":
                        config.setHeureDebutJournee(getHeure(cellValeur));
                        break;
                    case "heureFinJournee":
                        config.setHeureFinJournee(getHeure(cellValeur));
                        break;
                    case "heureDebutPause":
                        config.setHeureDebutPause(getHeure(cellValeur));
                        break;
                    case "heureFinPause":
                        config.setHeureFinPause(getHeure(cellValeur));
                        break;
                    case "pauseMinimale":
                        config.setPauseMinimale((int) getNumeric(cellValeur));
                        break;
                    case "minSoutenanceParProfParJour":
                        config.setMinSoutenancesParProfParJour((int) getNumeric(cellValeur));
                        break;
                    case "maxSoutenanceParProfParJour":
                        config.setMaxSoutenancesParProfParJour((int) getNumeric(cellValeur));
                        break;
                    case "nbJoursSoutenances":
                        config.setNbJoursSoutenances((int) getNumeric(cellValeur));
                        break;
                    case "dateDebut":
                        config.setDateDebut(getDate(cellValeur));
                        break;
                    default:
                        System.out.println(" Paramètre inconnu : " + parametre);
                        break;
                }
            }
        }
        config.valider();
        System.out.println("Config chargée : " + config);
        return config;
    }

    private double getNumeric(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC)
            return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING)
            return Double.parseDouble(cell.getStringCellValue().trim());
        throw new IllegalArgumentException(
            "Valeur numérique attendue ligne " + (cell.getRowIndex() + 1));
    }
    private LocalTime getHeure(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            int totalMinutes = (int) Math.round(val * 24 * 60);
            return LocalTime.of(totalMinutes / 60, totalMinutes % 60);
        }
        if (cell.getCellType() == CellType.STRING) {
            String s = cell.getStringCellValue().trim();

            if (s.matches("\\d:\\d{2}")) s = "0" + s;
            return LocalTime.parse(s);
        }
        throw new IllegalArgumentException(
            "Heure attendue ligne " + (cell.getRowIndex() + 1));
    }
    private LocalDate getDate(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {

            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        if (cell.getCellType() == CellType.STRING) {
            String s = cell.getStringCellValue().trim();

            if (s.matches("\\d{2}/\\d{2}/\\d{4}"))
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return LocalDate.parse(s);
        }
        throw new IllegalArgumentException(
            "Date attendue ligne " + (cell.getRowIndex() + 1));
    }
}