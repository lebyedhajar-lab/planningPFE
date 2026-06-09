package historique;

import model.*;
import repository.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlanningHistoriqueService {

    private static final DateTimeFormatter FMT_ID =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter FMT_DATE =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Path dossier;

    public PlanningHistoriqueService() {
        this.dossier = Paths.get("historique");
    }

    public String sauvegarder(List<Soutenance> soutenances,
                              String cheminExcel) throws IOException {
        Files.createDirectories(dossier);

        String id = LocalDateTime.now().format(FMT_ID);
        Path fichier = dossier.resolve(id + ".planning");

        try (BufferedWriter w = Files.newBufferedWriter(
                fichier, StandardCharsets.UTF_8)) {
            w.write("excel=" + (cheminExcel != null ? cheminExcel : ""));
            w.newLine();
            w.write("date=" + LocalDateTime.now().format(FMT_DATE));
            w.newLine();
            w.write("nb=" + soutenances.size());
            w.newLine();
            w.write("---DATA---");
            w.newLine();

            for (Soutenance s : soutenances) {
                w.write(serializeLigne(s));
                w.newLine();
            }
        }
        return id;
    }

    public List<PlanningHistorique> lister() throws IOException {
        if (!Files.exists(dossier)) return new ArrayList<>();

        List<PlanningHistorique> resultat = new ArrayList<>();
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(dossier, "*.planning")) {
            for (Path p : stream) {
                resultat.add(lireMeta(p));
            }
        }
        resultat.sort((a, b) ->
            b.getDateCreation().compareTo(a.getDateCreation()));
        return resultat;
    }

    public List<Soutenance> charger(String id,
                                    EtudiantRepository etudiantRepo,
                                    EnseignantRepository enseignantRepo,
                                    SalleRepository salleRepo) throws IOException {
        Path fichier = dossier.resolve(id + ".planning");
        if (!Files.exists(fichier))
            throw new FileNotFoundException("Planning introuvable : " + id);

        List<Soutenance> resultat = new ArrayList<>();
        boolean data = false;

        for (String ligne : Files.readAllLines(fichier, StandardCharsets.UTF_8)) {
            if (ligne.equals("---DATA---")) {
                data = true;
                continue;
            }
            if (!data || ligne.isBlank()) continue;
            resultat.add(deserialiserLigne(
                ligne, etudiantRepo, enseignantRepo, salleRepo));
        }
        return resultat;
    }

    public void supprimer(String id) throws IOException {
        Files.deleteIfExists(dossier.resolve(id + ".planning"));
    }

    private PlanningHistorique lireMeta(Path fichier) throws IOException {
        String id = fichier.getFileName().toString().replace(".planning", "");
        String cheminExcel = "";
        LocalDateTime date = LocalDateTime.now();
        int nb = 0;

        for (String ligne : Files.readAllLines(fichier, StandardCharsets.UTF_8)) {
            if (ligne.startsWith("---DATA---")) break;
            if (ligne.startsWith("excel="))
                cheminExcel = ligne.substring(6);
            else if (ligne.startsWith("date="))
                date = LocalDateTime.parse(ligne.substring(5), FMT_DATE);
            else if (ligne.startsWith("nb="))
                nb = Integer.parseInt(ligne.substring(3));
        }
        return new PlanningHistorique(id, date, cheminExcel, nb);
    }

    private String serializeLigne(Soutenance s) {
        Creneau c = s.getCreneau();
        Jury j = s.getJury();
        StringBuilder sb = new StringBuilder();
        sb.append(s.getId()).append(';');
        sb.append(s.getEtudiant().getId()).append(';');
        sb.append(s.getSalle().getId()).append(';');
        sb.append(c.getDateJour()).append(';');
        sb.append(c.getHeureDebut()).append(';');
        sb.append(c.getHeureFin()).append(';');
        sb.append(s.getLangue()).append(';');
        sb.append(s.getDureeMin()).append(';');
        sb.append(j.getEncadrant().getId());
        for (Enseignant m : j.getMembres()) {
            sb.append(';').append(m.getId());
        }
        return sb.toString();
    }

    private Soutenance deserialiserLigne(String ligne,
                                          EtudiantRepository etudiantRepo,
                                          EnseignantRepository enseignantRepo,
                                          SalleRepository salleRepo) {
        String[] p = ligne.split(";", -1);
        if (p.length < 10)
            throw new IllegalStateException("Ligne invalide : " + ligne);

        int soutenanceId   = Integer.parseInt(p[0]);
        int etudiantId     = Integer.parseInt(p[1]);
        int salleId        = Integer.parseInt(p[2]);
        LocalDate date     = LocalDate.parse(p[3]);
        LocalTime debut    = LocalTime.parse(p[4]);
        LocalTime fin      = LocalTime.parse(p[5]);
        String langue      = p[6];
        int duree          = Integer.parseInt(p[7]);
        int encadrantId    = Integer.parseInt(p[8]);

        Etudiant etudiant = etudiantRepo.trouverParId(etudiantId);
        Salle salle = salleRepo.trouverParId(salleId);
        Enseignant encadrant = enseignantRepo.trouverParId(encadrantId);

        if (etudiant == null)
            throw new IllegalStateException(
                "Étudiant id=" + etudiantId + " introuvable. Rechargez l'Excel.");
        if (salle == null)
            throw new IllegalStateException(
                "Salle id=" + salleId + " introuvable. Rechargez l'Excel.");
        if (encadrant == null)
            throw new IllegalStateException(
                "Encadrant id=" + encadrantId + " introuvable. Rechargez l'Excel.");

        List<Enseignant> membres = new ArrayList<>();
        for (int i = 9; i < p.length; i++) {
            if (p[i].isBlank()) continue;
            Enseignant m = enseignantRepo.trouverParId(Integer.parseInt(p[i]));
            if (m == null)
                throw new IllegalStateException(
                    "Membre jury id=" + p[i] + " introuvable.");
            membres.add(m);
        }

        etudiant.setEncadrant(encadrant);
        Creneau creneau = new Creneau(
            soutenanceId, date, debut, fin, true);
        Jury jury = new Jury(soutenanceId, encadrant, membres);

        return new Soutenance(
            soutenanceId, langue, duree, etudiant, salle, creneau, jury);
    }
}
