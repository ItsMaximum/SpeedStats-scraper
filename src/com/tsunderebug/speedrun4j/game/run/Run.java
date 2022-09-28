package com.tsunderebug.speedrun4j.game.run;

import com.tsunderebug.speedrun4j.data.Link;
import com.tsunderebug.speedrun4j.data.Videos;
import com.tsunderebug.speedrun4j.game.Category;
import com.tsunderebug.speedrun4j.platform.System;

import java.io.IOException;
import java.util.Map;

public class Run {

	private String id;
	private String weblink;
	private String game;
	private String level;
	private String category;
	private Videos videos;
	private String comment;
	private Status status;
	private Player[] players;
	private String date;
	private String submitted;
	private Timeset times;
	private System system;
	private Map<String, String> values;
	
	public double getImportantTime() {
		return this.getTimes().getPrimaryT();
	}

	
	public String getFormattedPrimaryTime() {
		String pt = this.getTimes().getPrimary();
		
		//has hours but has "00" for minutes
		if(pt.indexOf("H") != -1 && pt.indexOf("M") == -1)
			pt = pt.substring(0,pt.indexOf("H")+1) + "00M" + pt.substring(pt.indexOf("H")+1);
		
		//has "00" for seconds
		if(pt.indexOf("S") == -1)
			pt += "00S";
		
		
		pt = pt.replaceAll("[PTS]","");
		String[] pieces = pt.split("[HM]");
		//java.lang.System.out.println(pieces[0]);
		String formatted = pieces[0];
		for(int i=1; i<pieces.length; i++) {
			if(pieces[i].length() < 2) {
				pieces[i] = "0" + pieces[i];
			}
			formatted += ":" + pieces[i];
		}
		return formatted;
	}
	
	
	
	public String getGame() {
		return game;
	}

	public void setGame(String game) {
		this.game = game;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setWeblink(String weblink) {
		this.weblink = weblink;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setVideos(Videos videos) {
		this.videos = videos;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setPlayers(Player[] players) {
		this.players = players;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setSubmitted(String submitted) {
		this.submitted = submitted;
	}

	public void setTimes(Timeset times) {
		this.times = times;
	}

	public void setSystem(System system) {
		this.system = system;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public void setLinks(Link[] links) {
		this.links = links;
	}

	public Map<String,String> getValues() {
		return values;
	}
	
	public String getLevel() {
		return level;
	}

	public String getId() {
		return id;
	}

	public String getWeblink() {
		return weblink;
	}

	public Videos getVideos() {
		return videos;
	}

	public String getComment() {
		return comment;
	}

	public Status getStatus() {
		return status;
	}

	public Player[] getPlayers() {
		return players;
	}

	public String getDate() {
		return date;
	}

	public String getSubmitted() {
		return submitted;
	}

	public Timeset getTimes() {
		return times;
	}

	public System getSystem() {
		return system;
	}

	public Link[] getLinks() {
		return links;
	}

	public Category getCategory() throws IOException {
		return Category.fromID(category);
	}

	private Link[] links;

}
