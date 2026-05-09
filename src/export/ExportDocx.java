package export;

import model.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportDocx {

    public void generer(List<Soutenance> soutenances, String cheminFichier) throws IOException {

        StringBuilder contenu = new StringBuilder();

        contenu.append("<html><head><meta charset='UTF-8'>");
        contenu.append("<style>");
        contenu.append("body { font-family: Calibri, sans-serif; margin: 40px; }");
        contenu.append("h1 { color: #1F3864; text-align: center; font-size: 22px; }");
        contenu.append("p  { text-align: center; color: #595959; font-style: italic; }");
        contenu.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        contenu.append("th { background-color: #1F3864; color: white; padding: 8px; font-size: 11px; }");
        contenu.append("td { padding: 7px; font-size: 11px; border: 1px solid #cccccc; }");
        contenu.append(".pair   { background-color: #D9E1F2; }");
        contenu.append(".impair { background-color: #FFFFFF; }");
        contenu.append("</style></head><body>");

        String dateAujourdhui = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        contenu.append("<h1>Planning des Soutenances</h1>");
        contenu.append("<p>Généré le " + dateAujourdhui
                + " &nbsp;|&nbsp; Total : " + soutenances.size()
                + " soutenance(s)</p>");

        contenu.append("<table>");
        contenu.append("<tr>");
        contenu.append("<th>Étudiant</th>");
        contenu.append("<th>Filière</th>");
        contenu.append("<th>Encadrant</th>");
        contenu.append("<th>Membres du Jury</th>");
        contenu.append("<th>Date / Heure</th>");
        contenu.append("<th>Salle</th>");
        contenu.append("<th>Langue</th>");
        contenu.append("</tr>");

        for (int i = 0; i < soutenances.size(); i++) {
            Soutenance s = soutenances.get(i);
            String style = (i % 2 == 0) ? "pair" : "impair";

            contenu.append("<tr class='" + style + "'>");
            String etudiant = s.getEtudiant() != null
                    ? s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom()
                    : "—";
            contenu.append("<td>" + etudiant + "</td>");

            String filiere = (s.getEtudiant() != null && s.getEtudiant().getFiliere() != null)
                    ? s.getEtudiant().getFiliere().getNom()
                    : "—";
            contenu.append("<td>" + filiere + "</td>");

            String encadrant = (s.getJury() != null && s.getJury().getEncadrant() != null)
                    ? s.getJury().getEncadrant().getNom() + " " + s.getJury().getEncadrant().getPrenom()
                    : "—";
            contenu.append("<td>" + encadrant + "</td>");

            contenu.append("<td>" + construireListeMembres(s.getJury()) + "</td>");

            String dateHeure = s.getCreneau() != null
                    ? s.getCreneau().getDateJour().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                      + " " + s.getCreneau().getHeureDebut()
                      + " – " + s.getCreneau().getHeureFin()
                    : "—";
            contenu.append("<td>" + dateHeure + "</td>");

            String salle = s.getSalle() != null ? s.getSalle().getNom() : "—";
            contenu.append("<td>" + salle + "</td>");

            String langue = s.getLangue() != null ? s.getLangue() : "—";
            contenu.append("<td>" + langue + "</td>");

            contenu.append("</tr>");
        }

        contenu.append("</table></body></html>");

        FileWriter writer = new FileWriter(cheminFichier);
        writer.write(contenu.toString());
        writer.close();

        System.out.println("Fichier généré : " + cheminFichier);
    }

    private String construireListeMembres(Jury jury) {
        if (jury == null || jury.getMembres() == null || jury.getMembres().isEmpty()) return "—";
        StringBuilder resultat = new StringBuilder();
        for (Enseignant membre : jury.getMembres()) {
            if (resultat.length() > 0) resultat.append(", ");
            resultat.append(membre.getNom()).append(" ").append(membre.getPrenom());
        }
        return resultat.length() > 0 ? resultat.toString() : "—";
    }
}
