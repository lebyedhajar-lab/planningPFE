package algorithm;

import model.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PlanningVerificateur{

    public List<String> verifier(List<Soutenance> soutenances, List<Enseignant> enseignants) {
        List<String> anomalies = new ArrayList<>();

        anomalies.addAll(verifierChevauchementSalles(soutenances));
        anomalies.addAll(verifierProfDoubleHoraire(soutenances, enseignants));
        anomalies.addAll(verifierEcartMinimum(soutenances));
        anomalies.addAll(verifierRepartitionEquitable(enseignants));
        anomalies.addAll(verifierLangueAnglaise(soutenances));

        return anomalies;
    }
    
    private List<String> verifierChevauchementSalles(List<Soutenance> soutenances) {
        List<String> erreurs = new ArrayList<>();
        for (int i = 0; i < soutenances.size(); i++) {
            for (int j = i + 1; j < soutenances.size(); j++) {
                Soutenance s1 = soutenances.get(i);
                Soutenance s2 = soutenances.get(j);
                if (s1.getSalle().getId() == s2.getSalle().getId()) {
                    if (isTimeOverlapping(s1, s2)) {
                        erreurs.add("CONFLIT SALLE : La salle " + s1.getSalle().getNom() + 
                                   " est réservée en double pour " + s1.getEtudiant().getNom() + 
                                   " et " + s2.getEtudiant().getNom());
                    }
                }
            }
        }
        return erreurs;
    }

    private List<String> verifierProfDoubleHoraire(List<Soutenance> soutenances, List<Enseignant> enseignants) {
        List<String> erreurs = new ArrayList<>();
        for (int i = 0; i < soutenances.size(); i++) {
            for (int j = i + 1; j < soutenances.size(); j++) {
                Soutenance s1 = soutenances.get(i);
                Soutenance s2 = soutenances.get(j);

                if (isTimeOverlapping(s1, s2)) {
                    for (Enseignant e : enseignants) {
                        if (s1.getJury().contientEnseignant(e) && s2.getJury().contientEnseignant(e)) {
                            erreurs.add("CONFLIT ENSEIGNANT : Pr. " + e.getNom() + 
                                       " est convoqué sur deux soutenances simultanées : " + 
                                       s1.getEtudiant().getNom() + " et " + s2.getEtudiant().getNom());
                        }
                    }
                }
            }
        }
        return erreurs;
    }

    private List<String> verifierEcartMinimum(List<Soutenance> soutenances) {
        List<String> erreurs = new ArrayList<>();
        for (Soutenance s1 : soutenances) {
            for (Soutenance s2 : soutenances) {
                if (s1.getId() == s2.getId()) continue;

                if (s1.getSalle().getId() == s2.getSalle().getId() && 
                    s1.getCreneau().getDateJour().equals(s2.getCreneau().getDateJour())) {
                    
                    if (s2.getCreneau().getHeureDebut().isAfter(s1.getCreneau().getHeureFin())) {
                        long gap = Duration.between(s1.getCreneau().getHeureFin(), s2.getCreneau().getHeureDebut()).toMinutes();
                        if (gap < 15) {
                            erreurs.add("ÉCART INSUFFISANT : Seulement " + gap + " min de pause en salle " + 
                                       s1.getSalle().getNom() + " entre " + s1.getEtudiant().getNom() + 
                                       " et " + s2.getEtudiant().getNom());
                        }
                    }
                }
            }
        }
        return erreurs;
    }

    private List<String> verifierRepartitionEquitable(List<Enseignant> enseignants) {
        List<String> erreurs = new ArrayList<>();
        if (enseignants.isEmpty()) return erreurs;

        int total = 0;
        for (Enseignant e : enseignants) {
            total += e.getNbSoutenance();
        }
        double moyenne = (double) total / enseignants.size();
        
        double seuilSurcharge = moyenne + 3.0;

        for (Enseignant e : enseignants) {
            if (e.getNbSoutenance() > seuilSurcharge) {
                erreurs.add("ÉQUITÉ : Pr. " + e.getNom() + " a une charge trop élevée (" + 
                           e.getNbSoutenance() + " soutenances) par rapport à la moyenne de " + 
                           String.format("%.1f", moyenne));
            }
        }
        return erreurs;
    }

    private List<String> verifierLangueAnglaise(List<Soutenance> soutenances) {
        List<String> erreurs = new ArrayList<>();
        for (Soutenance s : soutenances) {
            if ("Anglais".equalsIgnoreCase(s.getLangue())) {
                boolean expertPresent = false;

                if ("Anglais".equalsIgnoreCase(s.getJury().getEncadrant().getSpecialite())) {
                    expertPresent = true;
                }
                
                for (Enseignant m : s.getJury().getMembres()) {
                    if ("Anglais".equalsIgnoreCase(m.getSpecialite())) {
                        expertPresent = true;
                        break;
                    }
                }

                if (!expertPresent) {
                    erreurs.add("LANGUE : La soutenance de " + s.getEtudiant().getNom() + 
                               " est en Anglais, mais aucun membre du jury n'est spécialiste en Anglais.");
                }
            }
        }
        return erreurs;
    }

    private boolean isTimeOverlapping(Soutenance s1, Soutenance s2) {
        Creneau c1 = s1.getCreneau();
        Creneau c2 = s2.getCreneau();

        if (!c1.getDateJour().equals(c2.getDateJour())) {
            return false;
        }

        return c1.getHeureDebut().isBefore(c2.getHeureFin()) && 
               c1.getHeureFin().isAfter(c2.getHeureDebut());
    }
}