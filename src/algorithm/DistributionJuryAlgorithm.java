package algorithm;

import Config.ConfigPlanning;
import model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DistributionJuryAlgorithm {

    private List<Soutenance> soutenances = new ArrayList<>();
    private final int nbMembres;
    private final int ecartMin;

    public DistributionJuryAlgorithm(ConfigPlanning config) {
        this.nbMembres = config.getNbMembresJury() - 1; // 2
        this.ecartMin  = config.getEcartMinEntreSoutenances();
    }

    public int calculerCharge(Enseignant enseignant) {
        int compteur = 0;
        for (Soutenance s : soutenances) {
            if (s.getJury().contientEnseignant(enseignant))
                compteur++;
        }
        return compteur;
    }

    public Enseignant enseignantLePlusDispo(
            List<Enseignant> candidats) {
        if (candidats == null || candidats.isEmpty()) return null;

        int minCharge = Integer.MAX_VALUE;
        for (Enseignant e : candidats) {
            minCharge = Math.min(minCharge, calculerCharge(e));
        }

        List<Enseignant> moinsCharges = new ArrayList<>();
        for (Enseignant e : candidats) {
            if (calculerCharge(e) == minCharge) {
                moinsCharges.add(e);
            }
        }

        if (moinsCharges.size() == 1) {
            return moinsCharges.get(0);
        }

        Collections.shuffle(moinsCharges);
        return moinsCharges.get(0);
    }
    private boolean dejaDansJuryMemeHoraire(Enseignant e, Creneau c) {
        for (Soutenance s : soutenances) {
            if (s.getCreneau().getDateJour().equals(c.getDateJour())
                && s.getCreneau().getHeureDebut().equals(c.getHeureDebut())
                && s.getJury().contientEnseignant(e)) {
                return true;
            }
        }
        return false;
    }

    /** Écart minimum entre deux débuts de soutenance (même logique que EcranVerification). */
    private boolean respecteEcartMinimum(Enseignant e, Creneau c) {
        return respecteEcartMinimum(e, c, null);
    }

    private boolean respecteEcartMinimum(Enseignant e, Creneau c,Soutenance exclure) {
    	for (Soutenance s : soutenances) {
    	if (exclure != null && s == exclure) continue;
    	if (!s.getCreneau().getDateJour().equals(c.getDateJour())) continue;
    	if (!s.getJury().contientEnseignant(e)) continue;

    	// Écart entre fin existante -> début nouvelle
    	long ecartApres = (c.getHeureDebut().toSecondOfDay() -
    	s.getCreneau().getHeureFin().toSecondOfDay()) / 60;

    	// Écart entre fin nouvelle -> début existante
    	long ecartAvant = (s.getCreneau().getHeureDebut().toSecondOfDay() -
    	c.getHeureFin().toSecondOfDay()) / 60;

    	if (ecartApres >= 0 && ecartApres < ecartMin) return false;
    	if (ecartAvant >= 0 && ecartAvant < ecartMin) return false;
    	}
    	return true;
    }

    private boolean salleOccupee(Salle s, Creneau c) {
        for (Soutenance sout : soutenances) {
            if (sout.getSalle().getId() == s.getId()
                && sout.getCreneau().getDateJour().equals(c.getDateJour())
                && sout.getCreneau().getHeureDebut().equals(c.getHeureDebut())) {
                return true;
            }
        }
        return false;
    }

    public Jury formerJury(Etudiant e,
                           List<Enseignant> enseignants,
                           String langue,
                           Creneau creneau,
                           ContrainteValidator validator) {

        Enseignant encadrant = e.getEncadrant();

        // Filtrer : exclure encadrant + indisponibles
        List<Enseignant> candidats = new ArrayList<>();
        for (Enseignant ens : enseignants) {
            if (ens.getId() == encadrant.getId()) continue;
            if (!validator.enseignantDisponible(ens, creneau))
                continue;
            if (dejaDansJuryMemeHoraire(ens, creneau)) continue;
            if (!respecteEcartMinimum(ens, creneau)) continue;
            candidats.add(ens);
        }

        // Séparer informaticiens et tous les autres
        List<Enseignant> informaticiens = new ArrayList<>();
        List<Enseignant> tousLesAutres  = new ArrayList<>();
        for (Enseignant ens : candidats) {
            if (ens.getSpecialite()
                   .equalsIgnoreCase("informatique"))
                informaticiens.add(ens);
            else
                tousLesAutres.add(ens);
        }

        // Vérifier au moins 1 informaticien disponible
        if (informaticiens.isEmpty())
            throw new IllegalStateException(
                "Pas de prof informatique pour : "
                + e.getNom() + " " + e.getPrenom());

        List<Enseignant> membres = new ArrayList<>();

        // ── Membre 1 : informaticien le moins chargé ─────────
        Enseignant info1 = enseignantLePlusDispo(informaticiens);
        membres.add(info1);
        informaticiens.remove(info1);
        candidats.remove(info1);

        // ── Membre 2 ──────────────────────────────────────────
        Enseignant membre2;

        if (langue != null
                && langue.equalsIgnoreCase("anglais")) {
            // Soutenance anglais → anglophone obligatoire
            List<Enseignant> anglophones = new ArrayList<>();
            for (Enseignant ens : candidats) {
                if (ens.isAnglophone()) anglophones.add(ens);
            }
            if (anglophones.isEmpty())
                throw new IllegalStateException(
                    "Pas de prof anglophone pour : "
                    + e.getNom() + " " + e.getPrenom());
            membre2 = enseignantLePlusDispo(anglophones);
        } else {
            // Soutenance français →
            // le moins chargé parmi TOUS les candidats restants
            // (informaticiens + maths + gestion + anglais)
            membre2 = enseignantLePlusDispo(candidats);
        }

        if (membre2 == null)
            throw new IllegalStateException(
                "Membre 2 null pour : "
                + e.getNom() + " " + e.getPrenom());

        membres.add(membre2);

        // Incrémenter les charges
        encadrant.incrementerSoutenances();
        for (Enseignant m : membres)
            m.incrementerSoutenances();

        return new Jury(soutenances.size(), encadrant, membres);
    }

    public List<Soutenance> distribuer(
            List<Etudiant>      etudiants,
            List<Enseignant>    enseignants,
            List<Creneau>       creneaux,
            List<Salle>         salles,
            int                 dureeMin,
            ContrainteValidator validator) {

        if (salles.isEmpty())
            throw new IllegalStateException(
                "Aucune salle disponible.");

        // Filtrer étudiants sans encadrant
        List<Etudiant> etudiantsValides = new ArrayList<>();
        for (Etudiant et : etudiants) {
            if (et.getEncadrant() == null) {
                System.out.println(
                    " Ignoré (encadrant null) : "
                    + et.getNom() + " " + et.getPrenom());
            } else {
                etudiantsValides.add(et);
            }
        }

        if (etudiantsValides.isEmpty())
            throw new IllegalStateException(
                "Aucun étudiant avec encadrant. "
                + "Vérifiez EncadrantAffectationService.");

        int capaciteTotale = creneaux.size() * salles.size();
        if (etudiantsValides.size() > capaciteTotale)
            throw new IllegalStateException(
                "Créneaux insuffisants : " + capaciteTotale
                + " pour " + etudiantsValides.size()
                + " étudiants.");

        trierEtudiantsPourEquilibre(etudiantsValides);

        // Distribuer
        int i = soutenances.size()+1;
        for (Etudiant et : etudiantsValides) {
        	Creneau creneau = null;
        	Salle salle = null;
        	Jury jury = null;

        	for (int c = 0; c < creneaux.size() && creneau == null; c++) {
        	    Creneau candidatCreneau = creneaux.get(c);
        	    if (dejaDansJuryMemeHoraire(et.getEncadrant(), candidatCreneau)
        	        || !respecteEcartMinimum(et.getEncadrant(), candidatCreneau)) {
        	        continue;
        	    }
        	    for (int s2 = 0; s2 < salles.size(); s2++) {
        	        if (salleOccupee(salles.get(s2), candidatCreneau)) continue;
        	        try {
        	            jury = formerJury(
        	                et, enseignants, et.getLangue(),
        	                candidatCreneau, validator);
        	            creneau = candidatCreneau;
        	            salle = salles.get(s2);
        	            break;
        	        } catch (IllegalStateException ignored) {
        	            // créneau suivant si jury impossible (contraintes)
        	        }
        	    }
        	}

        	if (creneau == null || jury == null)
        	    throw new IllegalStateException(
        	        "Pas de créneau disponible pour : "
        	        + et.getNom() + " " + et.getPrenom()
        	        + " (écart minimum " + ecartMin + " min requis)");

            String langue = et.getLangue();

            Soutenance s = new Soutenance(
                i, langue, dureeMin,
                et, salle, creneau, jury);

            soutenances.add(s);
            i++;
        }

        reequilibrerMembresJury(enseignants, validator);
        corrigerViolationsEcart(enseignants, validator);

        // Rapport
        System.out.println("\n=== Rapport Jury ===");
        System.out.println("Total : " + soutenances.size());
        for (Enseignant ens : enseignants) {
            int charge = calculerCharge(ens);
            if (charge > 0)
                System.out.println("  "
                    + ens.getNom() + " "
                    + ens.getPrenom()
                    + " [" + ens.getSpecialite() + "]"
                    + " -> " + charge + " jury(s)");
        }
        System.out.println("====================");

        return soutenances;
    }

    /**
     * Traite d'abord les encadrants les plus chargés, puis les soutenances
     * en anglais en dernier pour ne pas surcharger les anglophones.
     */
    private void trierEtudiantsPourEquilibre1(List<Etudiant> etudiants) {
        etudiants.sort(Comparator
            .comparingInt((Etudiant e) ->
                compterEtudiantsParEncadrant(e.getEncadrant(), etudiants))
            .reversed()
            .thenComparing(e ->
                "anglais".equalsIgnoreCase(e.getLangue())));
    }
    private void trierEtudiantsPourEquilibre(List<Etudiant> etudiants) {
        // Précalculer les charges AVANT le tri pour éviter l'incohérence
        java.util.Map<Integer, Integer> chargesParEncadrant = new java.util.HashMap<>();
        for (Etudiant e : etudiants) {
            if (e.getEncadrant() != null) {
                int id = e.getEncadrant().getId();
                if (!chargesParEncadrant.containsKey(id)) {
                    chargesParEncadrant.put(id, compterEtudiantsParEncadrant(e.getEncadrant(), etudiants));
                }
            }
        }

        etudiants.sort((a, b) -> {
            int chargeA = a.getEncadrant() != null
                ? chargesParEncadrant.getOrDefault(a.getEncadrant().getId(), 0) : 0;
            int chargeB = b.getEncadrant() != null
                ? chargesParEncadrant.getOrDefault(b.getEncadrant().getId(), 0) : 0;

            int cmp = Integer.compare(chargeB, chargeA); // reversed
            if (cmp != 0) return cmp;

            int langueA = "anglais".equalsIgnoreCase(a.getLangue()) ? 1 : 0;
            int langueB = "anglais".equalsIgnoreCase(b.getLangue()) ? 1 : 0;
            return Integer.compare(langueA, langueB);
        });
    }
    private int compterEtudiantsParEncadrant(Enseignant encadrant,
                                              List<Etudiant> etudiants) {
        int count = 0;
        for (Etudiant e : etudiants) {
            if (e.getEncadrant() != null
                && e.getEncadrant().getId() == encadrant.getId()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Échange des membres de jury entre soutenances pour réduire l'écart
     * min/max lorsque les contraintes le permettent.
     */
    private void reequilibrerMembresJury(List<Enseignant> enseignants,
                                          ContrainteValidator validator) {
        int maxIterations = soutenances.size() * 3;
        for (int iter = 0; iter < maxIterations; iter++) {
            Enseignant surcharge = trouverEnseignantExtreme(enseignants, true);
            if (surcharge == null) break;

            List<Enseignant> triesParCharge = new ArrayList<>(enseignants);
            triesParCharge.sort((a, b) -> Integer.compare(calculerCharge(a), calculerCharge(b)));

            boolean echange = false;
            for (Enseignant sousCharge : triesParCharge) {
                if (sousCharge.getId() == surcharge.getId()) continue;
                if (calculerCharge(surcharge) - calculerCharge(sousCharge) <= 1) break;
                if (essayerEchangerMembre(surcharge, sousCharge, validator)) {
                    echange = true;
                    break;
                }
            }
            if (!echange) break;
        }
    }

    private Enseignant trouverEnseignantExtreme(List<Enseignant> enseignants,
                                                 boolean max) {
        Enseignant extreme = null;
        int valeur = max ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Enseignant e : enseignants) {
            int charge = calculerCharge(e);
            if (charge == 0) continue;
            if (max ? charge > valeur : charge < valeur) {
                valeur = charge;
                extreme = e;
            }
        }
        return extreme;
    }

    private boolean essayerEchangerMembre(Enseignant surcharge,
                                           Enseignant sousCharge,
                                           ContrainteValidator validator) {
        for (Soutenance s : soutenances) {
            if (s.getJury().getEncadrant().getId() == surcharge.getId()) continue;

            List<Enseignant> membres = s.getJury().getMembres();
            for (int i = 0; i < membres.size(); i++) {
                if (membres.get(i).getId() != surcharge.getId()) continue;
                if (peutRemplacerMembre(s, i, sousCharge, validator)) {
                    membres.set(i, sousCharge);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean peutRemplacerMembre(Soutenance s, int indexMembre,
                                         Enseignant remplacant,
                                         ContrainteValidator validator) {
        Jury jury = s.getJury();
        Enseignant encadrant = jury.getEncadrant();
        Creneau creneau = s.getCreneau();
        String langue = s.getLangue();

        if (remplacant.getId() == encadrant.getId()) return false;
        if (jury.contientEnseignant(remplacant)) return false;
        if (!validator.enseignantDisponible(remplacant, creneau)) return false;
        if (dejaDansJuryMemeHoraire(remplacant, creneau)) return false;
        if (!respecteEcartMinimum(remplacant, creneau, s)) return false;

        if (indexMembre == 0
            && !remplacant.getSpecialite().equalsIgnoreCase("informatique")) {
            return false;
        }

        if (langue != null && langue.equalsIgnoreCase("anglais")) {
            boolean anglophonePresent = remplacant.isAnglophone();
            if (!anglophonePresent) {
                for (int i = 0; i < jury.getMembres().size(); i++) {
                    if (i == indexMembre) continue;
                    if (jury.getMembres().get(i).isAnglophone()) {
                        anglophonePresent = true;
                        break;
                    }
                }
            }
            if (!anglophonePresent && !encadrant.isAnglophone()) {
                return false;
            }
        }

        List<Enseignant> membresApres = new ArrayList<>(jury.getMembres());
        membresApres.set(indexMembre, remplacant);
        boolean aInformatique = encadrant.getSpecialite()
            .equalsIgnoreCase("informatique");
        for (Enseignant m : membresApres) {
            if (m.getSpecialite().equalsIgnoreCase("informatique")) {
                aInformatique = true;
                break;
            }
        }
        return aInformatique;
    }

    /** Corrige les membres de jury qui violent encore l'écart minimum. */
    private void corrigerViolationsEcart(List<Enseignant> enseignants,
                                          ContrainteValidator validator) {
        int maxIter = soutenances.size() * enseignants.size();
        for (int n = 0; n < maxIter; n++) {
            boolean corrige = false;
            for (Soutenance s : soutenances) {
                List<Enseignant> membres = s.getJury().getMembres();
                for (int i = 0; i < membres.size(); i++) {
                    Enseignant m = membres.get(i);
                    if (respecteEcartMinimum(m, s.getCreneau(), s)) continue;

                    for (Enseignant candidat : enseignants) {
                        if (candidat.getId() == m.getId()) continue;
                        if (peutRemplacerMembre(s, i, candidat, validator)) {
                            membres.set(i, candidat);
                            corrige = true;
                            break;
                        }
                    }
                }
            }
            if (!corrige) break;
        }
    }
}