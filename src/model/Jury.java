package model;

import java.util.List;

public class Jury {
	
	private int id;
	private Enseignant encadrant;
	private List<Enseignant> membres;
	
	public Jury(int id, Enseignant encadrant, List<Enseignant> membres) {
        this.id = id;
        this.encadrant = encadrant;
        this.membres = membres;
    }
	
	public int getId() {
		return id;
	}
	
	public Enseignant getEncadrant() {
		return encadrant;
	}
	public List<Enseignant> getMembres() {
	    return membres;
	}
	
	// Est-ce que cet enseignant fait partie de ce jury ?

	public boolean contientEnseignant(Enseignant e) {
        if (encadrant.getId() == e.getId()) return true;
        for (Enseignant m : membres) {
            if (m.getId() == e.getId()) return true;
        }
        return false;
    }
	public void setEncadrant(Enseignant encadrant) {
	    this.encadrant = encadrant;
	}

	public void setMembres(List<Enseignant> membres) {
	    this.membres = membres;
	}
}
