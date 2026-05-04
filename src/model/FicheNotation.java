package model;

import java.time.LocalDate;

public class FicheNotation {

    private int id;
    private Jury jury;
    private Soutenance soutenance;
    private String appreciation;
    private LocalDate dateRemise;
    private Etudiant etudiant;
    private Double note;   
    private boolean estRemplie; 

    
// Constructeur 1 — fiche vide générée avant la soutenance
    public FicheNotation(int id, Jury jury,String appreciation, LocalDate dateRemise) {
        this.id = id;
        this.jury = jury;
        this.appreciation = appreciation;
        this.dateRemise = dateRemise;
        this.note=null;
    }

// Constructeur 2 — fiche remplie après délibération
    public FicheNotation(int id, Soutenance soutenance,Etudiant etudiant, Jury jury,double note, String appreciation,LocalDate dateRemise) {
    	this.id   = id;
    	this.soutenance   = soutenance;
    	this.etudiant     = etudiant;
    	this.jury         = jury;
    	this.note         = note;
    	this.appreciation = appreciation;
    	this.dateRemise   = dateRemise;
    	this.estRemplie   = true;
    }
    
    
    public int        getId() {return id;}
    public Double getNote()          { return note; }
    public Soutenance getSoutenance(){ return soutenance; }
    public Jury       getJury() {return jury;}
    public String     getAppreciation() {return appreciation;}
    public LocalDate  getDateRemise() {return dateRemise; }
    public Etudiant   getEtudiant() {return etudiant; }
    
    public void saisirNote(double note, String appreciation,LocalDate dateRemise) {
    	this.note         = note;
    	this.appreciation = appreciation;
    	this.dateRemise   = dateRemise;
    	this.estRemplie   = true; 
    }
}