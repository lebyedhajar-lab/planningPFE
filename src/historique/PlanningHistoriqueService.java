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

    private static final int FORMAT_VERSION = 2;

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
            w.write("version=" + FORMAT_VERSION);
            w.newLine();
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

        int version = 1;
        List<Soutenance> resultat = new ArrayList<>();
        boolean data = false;

        for (String ligne : Files.readAllLines(fichier, StandardCharsets.UTF_8)) {
            if (ligne.startsWith("version=")) {
                version = Integer.parseInt(ligne.substring(8).trim());
                continue;
            }
            if (ligne.equals("---DATA---")) {
                data = true;
                continue;
            }
            if (!data || ligne.isBlank()) continue;
            resultat.add(deserialiserLigne(
                ligne, version, etudiantRepo, enseignantRepo, salleRepo));
        }
        return resultat;
    }

    public void supprimer(String id) throws IOException {
        Files.deleteIfExists(dossier.resolve(id + ".planning"));
    }

    public String lireCheminExcel(String id) throws IOException {
        Path fichier = dossier.resolve(id + ".planning");
        if (!Files.exists(fichier))
            throw new FileNotFoundException("Planning introuvable : " + id);
        for (String ligne : Files.readAllLines(fichier, StandardCharsets.UTF_8)) {
            if (ligne.startsWith("---DATA---")) break;
            if (ligne.startsWith("excel="))
                return ligne.substring(6);
        }
        return "";
    }

    public int lireVersion(String id) throws IOException {
        Path fichier = dossier.resolve(id + ".planning");
        if (!Files.exists(fichier))
            throw new FileNotFoundException("Planning introuvable : " + id);
        for (String ligne : Files.readAllLines(fichier, StandardCharsets.UTF_8)) {
            if (ligne.startsWith("---DATA---")) break;
            if (ligne.startsWith("version="))
                return Integer.parseInt(ligne.substring(8).trim());
        }
        return 1;
    }

    /** Réécrit un ancien fichier au format v2 (avec noms). */
    public void migrerVersV2(String id, List<Soutenance> soutenances,
                             String cheminExcel) throws IOException {
        Path fichier = dossier.resolve(id + ".planning");
        if (!Files.exists(fichier)) return;

        LocalDateTime dateCreation = lireMeta(fichier).getDateCreation();

        try (BufferedWriter w = Files.newBufferedWriter(
                fichier, StandardCharsets.UTF_8)) {
            w.write("version=" + FORMAT_VERSION);
            w.newLine();
            w.write("excel=" + (cheminExcel != null ? cheminExcel : ""));
            w.newLine();
            w.write("date=" + dateCreation.format(FMT_DATE));
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

    /** Format v2 : IDs + noms pour rechargement indépendant de l'ordre Excel. */
    private String serializeLigne(Soutenance s) {
        Creneau c = s.getCreneau();
        Jury j = s.getJury();
        Etudiant et = s.getEtudiant();
        Enseignant enc = j.getEncadrant();
        Salle salle = s.getSalle();

        StringBuilder sb = new StringBuilder();
        sb.append(s.getId()).append(';');
        sb.append(et.getId()).append(';');
        sb.append(et.getNom()).append(';');
        sb.append(et.getPrenom()).append(';');
        sb.append(salle.getId()).append(';');
        sb.append(salle.getNom()).append(';');
        sb.append(c.getDateJour()).append(';');
        sb.append(c.getHeureDebut()).append(';');
        sb.append(c.getHeureFin()).append(';');
        sb.append(s.getLangue()).append(';');
        sb.append(s.getDureeMin()).append(';');
        sb.append(enc.getId()).append(';');
        sb.append(enc.getNom()).append(';');
        sb.append(enc.getPrenom());
        for (Enseignant m : j.getMembres()) {
            sb.append(';').append(m.getId());
            sb.append(';').append(m.getNom());
            sb.append(';').append(m.getPrenom());
        }
        return sb.toString();
    }

    private Soutenance deserialiserLigne(String ligne,
                                          int version,
                                          EtudiantRepository etudiantRepo,
                                          EnseignantRepository enseignantRepo,
                                          SalleRepository salleRepo) {
        String[] p = ligne.split(";", -1);
        if (estLigneFormatV2(p, version)) {
            return deserialiserLigneV2(p, etudiantRepo, enseignantRepo, salleRepo);
        }
        return deserialiserLigneV1(p, etudiantRepo, enseignantRepo, salleRepo);
    }

    /** v2 : date en position 6 ; v1 : date en position 3. */
    private boolean estLigneFormatV2(String[] p, int version) {
        if (p.length < 14) return false;
        if (version >= 2) return true;
        return estDate(p[6]) && !estDate(p[3]);
    }

    private boolean estDate(String s) {
        try {
            LocalDate.parse(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Soutenance deserialiserLigneV2(String[] p,
                                            EtudiantRepository etudiantRepo,
                                            EnseignantRepository enseignantRepo,
                                            SalleRepository salleRepo) {
        int soutenanceId = Integer.parseInt(p[0]);
        int etudiantId   = Integer.parseInt(p[1]);
        String etNom     = p[2];
        String etPrenom  = p[3];
        int salleId      = Integer.parseInt(p[4]);
        String salleNom  = p[5];
        LocalDate date   = LocalDate.parse(p[6]);
        LocalTime debut  = LocalTime.parse(p[7]);
        LocalTime fin    = LocalTime.parse(p[8]);
        String langue    = p[9];
        int duree        = Integer.parseInt(p[10]);
        int encadrantId  = Integer.parseInt(p[11]);
        String encNom    = p[12];
        String encPrenom = p[13];

        Enseignant encadrant = resoudreEnseignant(
            enseignantRepo, encadrantId, encNom, encPrenom);

        Etudiant etudiant = resoudreEtudiant(
            etudiantRepo, etudiantId, etNom, etPrenom, langue, encadrant);

        Salle salle = resoudreSalle(salleRepo, salleId, salleNom);

        List<Enseignant> membres = new ArrayList<>();
        for (int i = 14; i + 2 < p.length; i += 3) {
            if (p[i].isBlank()) continue;
            int membreId = Integer.parseInt(p[i]);
            String membreNom = p[i + 1];
            String membrePrenom = p[i + 2];
            membres.add(resoudreEnseignant(
                enseignantRepo, membreId, membreNom, membrePrenom));
        }

        Creneau creneau = new Creneau(soutenanceId, date, debut, fin, true);
        Jury jury = new Jury(soutenanceId, encadrant, membres);

        return new Soutenance(
            soutenanceId, langue, duree, etudiant, salle, creneau, jury);
    }

    /** Ancien format (IDs seuls) — conservé pour les fichiers existants. */
    private Soutenance deserialiserLigneV1(String[] p,
                                            EtudiantRepository etudiantRepo,
                                            EnseignantRepository enseignantRepo,
                                            SalleRepository salleRepo) {
        if (p.length < 10)
            throw new IllegalStateException("Ligne invalide : " + String.join(";", p));

        int soutenanceId = Integer.parseInt(p[0]);
        int etudiantId   = Integer.parseInt(p[1]);
        int salleId      = Integer.parseInt(p[2]);
        LocalDate date   = LocalDate.parse(p[3]);
        LocalTime debut  = LocalTime.parse(p[4]);
        LocalTime fin    = LocalTime.parse(p[5]);
        String langue    = p[6];
        int duree        = Integer.parseInt(p[7]);
        int encadrantId  = Integer.parseInt(p[8]);

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
        Creneau creneau = new Creneau(soutenanceId, date, debut, fin, true);
        Jury jury = new Jury(soutenanceId, encadrant, membres);

        return new Soutenance(
            soutenanceId, langue, duree, etudiant, salle, creneau, jury);
    }

    private Etudiant resoudreEtudiant(EtudiantRepository repo,
                                       int id, String nom, String prenom,
                                       String langue, Enseignant encadrant) {
        Etudiant e = repo.trouverParNomPrenom(nom, prenom);
        if (e == null) e = repo.trouverParId(id);
        if (e == null) {
            e = new Etudiant(repo.prochainId(), nom, prenom, langue,
                null, encadrant, "");
            repo.sauvegarder(e);
        } else {
            e.setEncadrant(encadrant);
        }
        return e;
    }

    private Enseignant resoudreEnseignant(EnseignantRepository repo,
                                           int id, String nom, String prenom) {
        Enseignant e = repo.trouverParNomPrenom(nom, prenom);
        if (e == null) e = repo.trouverParId(id);
        if (e == null) {
            e = new Enseignant(repo.prochainId(), 0, nom, prenom,
                false, "informatique");
            repo.sauvegarder(e);
        }
        return e;
    }

    private Salle resoudreSalle(SalleRepository repo, int id, String nom) {
        Salle s = repo.trouverParNom(nom);
        if (s == null) s = repo.trouverParId(id);
        if (s == null) {
            s = new Salle(repo.prochainId(), nom, nom, true);
            repo.sauvegarder(s);
        }
        return s;
    }
}
