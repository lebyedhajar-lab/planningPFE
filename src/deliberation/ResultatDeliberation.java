package deliberation;

import model.Etudiant;
import model.FicheNotation;


public class ResultatDeliberation {
    private Etudiant etudiant;
    private FicheNotation fiche;
    private DecisionJury decision;    // enum : ADMIS, RATTRAPAGE, AJOURNÉ
    private Mention mention;          // AB, Bien, TB,EXCELLENT
    
    
    public ResultatDeliberation(Etudiant etudiant, FicheNotation fiche) {
        if (fiche.getNote() == null)
            throw new IllegalStateException(
                "La fiche de " + etudiant.getNom() + " n'est pas encore remplie.");
        this.etudiant = etudiant;
        this.fiche    = fiche;
        this.decision = calculerDecision();
        this.mention  = calculerMention();
    }
    
    
    public Mention calculerMention() {
        double note = this.fiche.getNote();
        if (note >= 18) return Mention.EXCELLENT;
        else if (note >= 16) return Mention.TRES_BIEN;
        else if (note >= 14) return Mention.BIEN;
        else if (note >= 12) return Mention.ASSEZ_BIEN;
        else                 return Mention.AUCUNE;
    }
    	    
    public DecisionJury calculerDecision() {
    	double note =this.fiche.getNote();
    	if(note>=10) return DecisionJury.ADMIS;
    	else if (note >=8)  return DecisionJury.RATTRAPAGE;
    	else return DecisionJury.AJOURNE;
    }
    
    public Etudiant getEtudiant()       { return etudiant; }
    public FicheNotation getFiche()     { return fiche; }
    public DecisionJury getDecision()   { return decision; }
    public Mention getMention()         { return mention; }
    public Double getNote()             { return fiche.getNote(); }
    public boolean estAdmis()           { return decision == DecisionJury.ADMIS; }
    
    public String toString() {
        return "ResultatDeliberation{"
            + "etudiant=" + etudiant.getNom()
            + ", note=" + fiche.getNote()
            + ", mention=" + mention
            + ", decision=" + decision + "}";
    }
}
