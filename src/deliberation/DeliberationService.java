package deliberation;

import model.Etudiant;
import model.FicheNotation;
import repository.FicheNotationRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DeliberationService {

    private FicheNotationRepository ficheRepo;
    private List<ResultatDeliberation> resultats;

    public DeliberationService(FicheNotationRepository ficheRepo) {
        this.ficheRepo  = ficheRepo;
        this.resultats  = new ArrayList<>();
    }

    // Saisir la note dans une fiche ───────────────────────────
    // Appelé par le responsable PFE après délibération
    public void saisirNote(FicheNotation fiche, double note,
                           String appreciation) {
        fiche.saisirNote(note, appreciation, LocalDate.now());
        ficheRepo.sauvegarder(fiche);
    }

    // ─── Générer le résultat d'un étudiant ───────────────────────
    public ResultatDeliberation genererResultat(Etudiant etudiant,
                                                FicheNotation fiche) {
        ResultatDeliberation resultat = 
            new ResultatDeliberation(etudiant, fiche);
        resultats.add(resultat);
        return resultat;
    }

    // ─── Générer tous les résultats 
    public List<ResultatDeliberation> genererTousLesResultats(
                                      List<FicheNotation> fiches) {
        resultats.clear();
        for (FicheNotation fiche : fiches) {
            // vérifier que la fiche est remplie avant de générer
        	if (fiche.isEstRemplie()) {                ResultatDeliberation resultat = new ResultatDeliberation(
                    fiche.getEtudiant(), fiche);
                resultats.add(resultat);
            }
        }
        return new ArrayList<>(resultats);
    }

    // ─── Filtrer les admis ───────────────────────────────────────
    public List<ResultatDeliberation> getAdmis() {
        List<ResultatDeliberation> admis = new ArrayList<>();
        for (ResultatDeliberation r : resultats) {
            if (r.estAdmis()) {
                admis.add(r);
            }
        }
        return admis;
    }

    // ─── Filtrer les rattrapages ─────────────────────────────────
    public List<ResultatDeliberation> getRattrapage() {
        List<ResultatDeliberation> rattrapage = new ArrayList<>();
        for (ResultatDeliberation r : resultats) {
            if (r.getDecision() == DecisionJury.RATTRAPAGE) {
                rattrapage.add(r);
            }
        }
        return rattrapage;
    }

    // ─── Filtrer les ajournés ────────────────────────────────────
    public List<ResultatDeliberation> getAjournes() {
        List<ResultatDeliberation> ajournes = new ArrayList<>();
        for (ResultatDeliberation r : resultats) {
            if (r.getDecision() == DecisionJury.AJOURNE) {
                ajournes.add(r);
            }
        }
        return ajournes;
    }

    // ─── Vérifier si toutes les fiches sont remises ──────────────
    // Utilisé avant de générer les résultats
    public boolean toutesLesFichesRemises() {
        List<FicheNotation> fiches = ficheRepo.chargerTous();
        for (FicheNotation f : fiches) {
        	if (!f.isEstRemplie()) return false;  
        }
        return true;
    }
    // ─── Getter résultats ────────────────────────────────────────
    public List<ResultatDeliberation> getResultats() {
        return new ArrayList<>(resultats);
    }
}