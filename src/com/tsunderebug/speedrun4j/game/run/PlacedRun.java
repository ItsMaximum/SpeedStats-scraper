package com.tsunderebug.speedrun4j.game.run;

public class PlacedRun {

	private int place;

	private Run run;
	
	private double runValue;
	
	public void setRunValue(double runValue) {
		this.runValue = runValue;
	}
	
	public double getRunValue() {
		return this.runValue;
	}

	public int getPlace() {
		return place;
	}

	public Run getRun() {
		return run;
	}

	public void setPlace(int place) {
		this.place = place;
	}

	public void setRun(Run run) {
		this.run = run;
	}
	
	
}
