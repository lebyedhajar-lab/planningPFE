package model;

import java.time.LocalDate;

public class FicheNotation {
	
	
	private int id;
	private Jury jury;
	private double note;
	private String appreciation;
	private LocalDate dateRemise;
	
	public FicheNotation(int id, Jury jury, double note, String appreciation, LocalDate dateRemise) {
		this.id = id;
		this.jury = jury;
		this.note = note;
		this.appreciation = appreciation;
		this.dateRemise = dateRemise;
	}
	
	public int getId() {
		return id;
	}
	
	public double getNote() {
		return note;
	}
	
	public String getAppreciation() {
		return appreciation;
	}
	
	public void setNote(double n) {
		this.note = n;
	}

}