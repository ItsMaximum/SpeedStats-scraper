package com.tsunderebug.speedrun4j.game;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.tsunderebug.speedrun4j.game.run.PlacedRun;
import com.tsunderebug.speedrun4j.game.run.Player;
import com.tsunderebug.speedrun4j.game.run.Run;

public class categoryRunList {
	
	private Game game;
	private Category category;
	private List<Subcategory> subcatlist;
	private LinkedHashMap<String, String> subCatValues;
	private String name;
	private Leaderboard leaderboard;
	private int numWrs;
	private int numRuns;
	private int numRunners;
	private Level level;
	private ArrayList<Run> runs;
	
	
	//categoryRunList for category with no subcategories or levels
	public categoryRunList(Game game, Category category, ArrayList<Run> runs) throws IOException {
		this.name = (game.getNames().get("international") + ": " + category.getName()).replaceAll(",", ".");
		this.category = category;
		if(runs.size() >= 10000) {
			this.leaderboard = createLeaderboard(runs); 
		} else {
			this.leaderboard = Leaderboard.forCategory(game, category);
		}
		this.numWrs = getNumWrs(runs);
		this.numRuns = runs.size();
		this.numRunners = this.leaderboard.getRuns().length;
		//this.runs = runs;
		this.updateRunValues();
	}
	
	//categoryRunList for category with level no subcategories
	public categoryRunList(Game game, Category category, ArrayList<Run> runs, Level level) throws IOException {
		this.name = (game.getNames().get("international") + ": " + category.getName() + 
				" - " + level.getName()).replaceAll(",", ".");
		this.category = category;
		if(runs.size() >= 10000) {
			this.leaderboard = createLeaderboard(runs); 
		} else {
			this.leaderboard = Leaderboard.forCategoryAndLevel(game, category, level);
		}
		this.numWrs = getNumWrs(runs);
		this.numRuns = runs.size();
		this.numRunners = this.leaderboard.getRuns().length;
		this.level = level;
		//this.runs = runs;
		this.updateRunValues();
	}
	
	//categoryRunList for subcategory with no level
	public categoryRunList(Game game, Category category, List<Subcategory> subcatlist, LinkedHashMap<String, String> subCatValues, ArrayList<Run> runs) throws IOException {
		this.name = (game.getNames().get("international") + ": " + category.getName() + " - " + 
				getSubcatNames(subcatlist, subCatValues)).replaceAll(",", ".");
		this.category = category;
		if(runs.size() >= 10000) {
			this.leaderboard = createLeaderboard(runs); 
		} else {
			this.leaderboard = Leaderboard.forCatAndSubCat(game, category, subCatValues);
		}
		this.numWrs = getNumWrs(runs);
		this.numRuns = runs.size();
		this.numRunners = this.leaderboard.getRuns().length;
		//this.runs = runs;
		this.updateRunValues();
	}
	
	//categoryRunList for subcategory with level
	public categoryRunList(Game game, Category category, List<Subcategory> subcatlist, LinkedHashMap<String, String> subCatValues, ArrayList<Run> runs, Level level) throws IOException {
		this.name = (game.getNames().get("international") + ": " + category.getName() + " - " + 
				level.getName() + " - " + getSubcatNames(subcatlist, subCatValues)).replaceAll(",", ".");
		this.category = category;
		if(runs.size() >= 10000) {
			this.leaderboard = createLeaderboard(runs); 
		} else {
			this.leaderboard = Leaderboard.forCatAndSubCatAndLevel(game, category, subCatValues, level);
		}
		this.numWrs = getNumWrs(runs);
		this.numRuns = runs.size();
		this.numRunners = this.leaderboard.getRuns().length;
		this.level = level;
		//this.runs = runs;
		this.updateRunValues();
	}
	
	public int getNumRunners() {
		return numRunners;
	}

	public void setNumRunners(int numRunners) {
		this.numRunners = numRunners;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public double getRunValue(int place) {
		if(place == 0 || numRuns == 0 || place == numRunners)
			return 0.0;
		
		int matchPlace = -1;
		for(PlacedRun pr : this.leaderboard.getRuns()) {
			if(pr.getPlace() == place)
				matchPlace++;
		}
		place += matchPlace;
		double placeWeight = Math.log(this.numRuns) / Math.log(place + 0.5);
		//double numWrsWeight = (1.0 * numWrs/(numRunners*numRunners)) * Math.pow(place - numRunners - 1, 2);
		double numWrsWeight = (-1.0 * numWrs/numRunners) * (place-1) + numWrs;

		if(this.level != null) {
			numWrsWeight *= 0.5;
		}
		return placeWeight * numWrsWeight;
	}
	
	public void updateRunValues() {
		for(PlacedRun pr : this.leaderboard.getRuns()) {
			pr.setRunValue(this.getRunValue(pr.getPlace()));
		}
	}
	
	
	public Game getGame() {
		return game;
	}
	
	public ArrayList<Run> getRuns() {
		return runs;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public List<Subcategory> getSubcatlist() {
		return subcatlist;
	}

	public void setSubcatlist(List<Subcategory> subcatlist) {
		this.subcatlist = subcatlist;
	}

	public LinkedHashMap<String, String> getSubCatValues() {
		return subCatValues;
	}

	public void setSubCatValues(LinkedHashMap<String, String> subCatValues) {
		this.subCatValues = subCatValues;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLeaderboard(Leaderboard leaderboard) {
		this.leaderboard = leaderboard;
	}


	public String getName() {
		return name;
	}
	public Leaderboard getLeaderboard() {
		return leaderboard;
	}
	public int getNumWrs() {
		return numWrs;
	}

	public void setNumWrs(int numWrs) {
		this.numWrs = numWrs;
	}

	public int getNumRuns() {
		return numRuns;
	}

	public void setNumRuns(int numRuns) {
		this.numRuns = numRuns;
	}
	
	//Creates a string containing the names of every subcategory value supplied
	public static String getSubcatNames(List<Subcategory> subcatlist, LinkedHashMap<String,String> subCatValues) {
		String subcats = "";
		LinkedHashMap<String,String> subcatNames = new LinkedHashMap<String,String>();
		for(Subcategory s: subcatlist)
			for(String id : s.getValues().keySet())
				subcatNames.put(id, s.getValues().get(id));
		
		//Add subcategory names together
		for(String value : subCatValues.values()) {
			subcats+=subcatNames.get(value) + " ";
		}
		return subcats;
	}
	
	public static Leaderboard createLeaderboard(ArrayList<Run> r) {
		ArrayList<Run> runs = new ArrayList<Run>(r);

		ArrayList<Player[]> runPlayers = new ArrayList<Player[]>();
		//put runs in order from highest time to lowest (since we loop through them backwards)
		runs.sort((r1, r2) -> Double.valueOf(r2.getImportantTime()).compareTo(Double.valueOf(r1.getImportantTime())));
		
		//remove obseleted runs
		for(int i = runs.size() - 1; i >= 0; i--) {
			Player[] players = runs.get(i).getPlayers();
			boolean obselete = false;
			
			for(Player[] pl : runPlayers) {
				
				if(Arrays.equals(players, pl)) {
					obselete = true;
					break;
				}
			}
			
			if(obselete) {
				runs.remove(i);
			} else {
				runPlayers.add(players);
			}
		}
		//put not obseleted runs in order from lowest time to highest time
		Collections.reverse(runs);
		
		PlacedRun[] prl = new PlacedRun[runs.size()];
		
		for(int i=0; i<runs.size(); i++) {
			PlacedRun p = new PlacedRun();
			if(i>0 && runs.get(i).getImportantTime() == runs.get(i-1).getImportantTime()) {
				p.setPlace(prl[i-1].getPlace());
			} else {
				p.setPlace(i+1);
			}
			p.setRun(runs.get(i));
			prl[i] = p;
		}
		
		Leaderboard l = new Leaderboard();
		l.setRuns(prl);
		return l;
	}

	
	public static int getNumWrs(ArrayList<Run> r) {
		ArrayList<Run> wrs = new ArrayList<Run>();
		ArrayList<Run> runs = new ArrayList<Run>(r);
		double currentWR = 100000000;
		for(int i=0; i<runs.size(); i++) {
			double time = runs.get(i).getImportantTime();
			
			if(runs.get(i).getDate() != null && time < currentWR) {
				currentWR = time;
				wrs.add(runs.get(i));
			}
		}
		return wrs.size();
	}
}
