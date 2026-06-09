package historique;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlanningHistorique {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String id;
    private final LocalDateTime dateCreation;
    private final String cheminExcel;
    private final int nbSoutenances;

    public PlanningHistorique(String id, LocalDateTime dateCreation,
                              String cheminExcel, int nbSoutenances) {
        this.id = id;
        this.dateCreation = dateCreation;
        this.cheminExcel = cheminExcel;
        this.nbSoutenances = nbSoutenances;
    }

    public String getId() { return id; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getCheminExcel() { return cheminExcel; }
    public int getNbSoutenances() { return nbSoutenances; }

    public String getNomFichierExcel() {
        if (cheminExcel == null || cheminExcel.isBlank()) return "—";
        int sep = Math.max(cheminExcel.lastIndexOf('/'),
                           cheminExcel.lastIndexOf('\\'));
        return sep >= 0 ? cheminExcel.substring(sep + 1) : cheminExcel;
    }

    public String getLibelle() {
        return dateCreation.format(FMT)
            + " — " + nbSoutenances + " soutenance(s)";
    }
}
