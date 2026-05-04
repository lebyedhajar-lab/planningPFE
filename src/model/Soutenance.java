package model;

public class Soutenance {
	
	private int id;
	private String langue;
	private int dureeMin;
	private Etudiant etudiant;
	private Salle salle;
	private Creneau creneau;
	private Jury jury;
	
	public Soutenance(int id, String langue, int dureeMin, Etudiant etudiant, Salle salle, Creneau creneau, Jury jury) {
		this.id = id;
		this.langue = langue;
		this.dureeMin = dureeMin;
		this.etudiant = etudiant;
		this.salle = salle;
		this.creneau = creneau;
		this.jury = jury;
	}
	
	public int getId() {return id;}
	public Etudiant getEtudiant() {return etudiant;}
	public Salle getSalle() {return salle;}
	public Creneau getCreneau() {return creneau;}
	public Jury getJury() {return jury;}
	public String getLangue() {return langue;}
	public int getDureeMin() {return dureeMin;}
}
