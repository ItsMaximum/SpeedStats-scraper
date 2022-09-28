import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.tsunderebug.speedrun4j.Speedrun4J;
import com.tsunderebug.speedrun4j.game.Category;
import com.tsunderebug.speedrun4j.game.Game;
import com.tsunderebug.speedrun4j.game.GameList;
import com.tsunderebug.speedrun4j.game.Level;
import com.tsunderebug.speedrun4j.game.LevelList;
import com.tsunderebug.speedrun4j.game.Subcategory;
import com.tsunderebug.speedrun4j.game.VariableList;
import com.tsunderebug.speedrun4j.game.categoryRunList;
import com.tsunderebug.speedrun4j.game.run.Run;
import com.tsunderebug.speedrun4j.game.run.RunList;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

public class GenerateCategoryRunLists {
	
	
	
	public static LinkedHashMap <String,Double> playerPoints = new LinkedHashMap <String,Double>();
	public static LinkedHashMap <String,String> playerNames = new LinkedHashMap <String,String>();
	public static int rateLimit = 560;
	public static RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
			  .handle(IOException.class)
			  .withDelay(Duration.ofSeconds(10))
			  .onFailedAttempt(e -> System.out.println("Connection attempt failed" + e.getLastFailure()))
			  .withMaxRetries(10)
			  .build();
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Gson gson = new Gson();
		
		//default values
		Boolean SKIP_EXISTING = false;
		int offset = 0;
		int total = 2000;
		
		if(args.length != 0) {
			Speedrun4J.proxyNum = Integer.parseInt(args[0]);
			offset = Integer.parseInt(args[1]);
			total = Integer.parseInt(args[2]);
			SKIP_EXISTING = args[3].equals("skip");
		}
		
		JsonReader gfr = new JsonReader(new FileReader("games.json",StandardCharsets.UTF_8));
		Game[] games = gson.fromJson(gfr, Game[].class);
		System.out.println(games.length);
		
		
		//List<Game> games = new ArrayList<Game>();
		//games.add(Game.fromID("redball1"));
		
		//Game[] games = GameList.fromSeriesId("redball").getGames();
		//Game[] games = GameList.fromOffset(Integer.parseInt(args[0])).getGames(); 
		
		//Collections.shuffle(games);
		//loop through each game
		for(int i=offset; i<total+offset; i++) {
			if(i>games.length) {
				break;
			}
			Game g = games[i];
			System.out.println(g.getNames().get("international"));
			String fileName = "games/" + g.getNames().get("international").replaceAll("[\\\\/:*?\"<>|]", "") + ".json";
			
			File file = new File(fileName);
			
			if(SKIP_EXISTING && file.exists()) {
				System.out.println(fileName + " already exists. Skipping");
			} else {
				
				
				ArrayList<categoryRunList> categoryRunLists = new ArrayList<categoryRunList>();
				
				try {
					TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
					Category[] categories = Failsafe.with(retryPolicy).get(() -> g.getCategories().getCategories());
					TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
					Level[] levelList = Failsafe.with(retryPolicy).get(() -> LevelList.forGame(g).getLevels());
					
					//loop through each category
					for(Category c : categories) {
						
						TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
						List<Subcategory> subcatlist = Failsafe.with(retryPolicy).get(() -> VariableList.fromCategory(c).getSubcategories());
						
						if(c.getType().equals("per-level")) {
							for(Level lev : levelList) { // Adds a categoryRunList for each level (per-level category)
								
								ArrayList<Subcategory> applicableSubcats = new ArrayList<Subcategory>();
								for(Subcategory s : subcatlist) {
									Map<String,String> scope = s.getScope();
									String type = scope.get("type");
									//Only adds subcategories that are applicable to the given level	
									if((type.equals("all-levels") || type.equals("global")) || (type.equals("single-level") && scope.get("level").equals(lev.getId())))
										applicableSubcats.add(s);
								}
								applicableRuns(g,c,categoryRunLists,lev,applicableSubcats);
							}
						} else { // Adds a categoryRunList for each level (per-game category)
							applicableRuns(g,c,categoryRunLists,null,subcatlist);
						}
					}
					g.setCategoryRunLists(categoryRunLists);
					
					FileWriter fw = new FileWriter(fileName);
					fw.write(gson.toJson(g));
					fw.close();
				} catch(Exception e) {
					System.out.println("Game no longer exists, or some other fatal error occurred. Skipping.");
				}
			}
		}
	}
	
	//Gets a list of games from a provided offset and intended total
	public static List<Game> getGamesFromOffset(int offset, int total) throws InterruptedException {
		int count = 0;
		List<Game> games = new ArrayList<Game>();
		
		//get a complete run list using offsets
		while(true) {
			count = 0;
			int currOffset = offset;
			for(Game g: Failsafe.with(retryPolicy).get(() -> GameList.fromOffset(currOffset).getGames())) {
				games.add(g);
				count++;
			}
			TimeUnit.MILLISECONDS.sleep(rateLimit);
			if(games.size() >= total || count < 1000)
				break;
			offset+=count;
		}
		return games;
	}
	
	
	
	// Calls respective methods for getting runs based on whether a category has subcategories or not
	public static void applicableRuns(Game g, Category c, ArrayList<categoryRunList> categoryRunLists, Level lev, List<Subcategory> subcatlist) throws InterruptedException, IOException {
		
		ArrayList<Run> categoryRuns = getRuns(c,lev);
		if(subcatlist.size() > 0) { //has subcategories
			LinkedHashMap<String,String> subCatValues = new LinkedHashMap<String,String>();
			possSubcategories(0, subcatlist, subCatValues, g, c, categoryRunLists, lev, categoryRuns);
		} else { //does not have subcategories
			getCatRuns(g,c,categoryRunLists, lev, categoryRuns);
		}
	}
	
	//Gets all runs from a specific category and level. Level may be null (Dealt with on the API call side)
	public static ArrayList<Run> getRuns(Category c, Level l) throws InterruptedException {
		ArrayList<Run> runs = new ArrayList<Run>();
		int count = 0;
		int offset = 0;
		//ArrayList<String> runIds = new ArrayList<String>();
		//int duped = 0;
		while(true) {
			count = 0;
			int ascOffset = offset;
			TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
			for(Run r: Failsafe.with(retryPolicy).get(() -> RunList.forCatAndOffsetAndLevel(c,ascOffset,l).getRuns())) {
				
				/*
				String id = r.getId();
				if(!runIds.contains(id)) {
					runIds.add(id);
				} else {
					duped++;
				}
				*/
				
				runs.add(r);
				count++;
			}
			if(count<200) {
				//System.out.println(duped);
				break;
			}
			offset+=count;
			if(offset >= 10000) { //start getting runs by reverse date order if a category has more than 10000 runs
				
				String run10000id = runs.get(runs.size()-1).getId(); //get the last run in the runs list (pivot point)
				ArrayList<Run> past10000Runs = new ArrayList<Run>();
				offset = 0;
				while(true) {
					count = 0;
					int descOffset = offset;
					
					TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
					for(Run r: Failsafe.with(retryPolicy).get(() -> RunList.forCatAndOffsetAndLevelDesc(c,descOffset,l).getRuns())) {
						if(!r.getId().equals(run10000id)) {
							past10000Runs.add(r);
							count++;
						} else {
							Collections.reverse(past10000Runs); //Reverse runs past 10000 so proper ordering is maintained
							runs.addAll(past10000Runs);
							return runs;
						}
					}
					offset+=count;
				}
			}
		}
		return runs;
	}
	
	//Creates a categoryRunList for a combination of subcategories and level (level may be null)
	public static void getSubcatRuns(Game g, Category c, List<Subcategory> subcatlist, LinkedHashMap<String,String> subCatValues, ArrayList<categoryRunList> crls, Level lev, ArrayList<Run> categoryRuns) throws IOException, InterruptedException {
		
		//get a complete run list using offsets
		ArrayList<Run> runs = new ArrayList<Run>(categoryRuns);
		
		if(runs.size() == 0)
			return;
		
		//remove runs that do not have the specified subcategories
		for(int i = runs.size()-1; i >= 0; i--) {
			Map<String,String> values = runs.get(i).getValues();
			for(String key : subCatValues.keySet()) {
				if(values.get(key) == null || !values.get(key).equals(subCatValues.get(key))) {
					runs.remove(i);
					break;
				}
			}
		}
		
		if(runs.size() == 0)
			return;
		categoryRunList crl;
		
		if(lev == null) { // Not a level category
			TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
			crl = Failsafe.with(retryPolicy).get(() -> new categoryRunList(g,c,subcatlist,subCatValues,runs));
		} else { // Level category
			TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
			crl = Failsafe.with(retryPolicy).get(() -> new categoryRunList(g,c,subcatlist,subCatValues,runs,lev));
		}

		System.out.println("Added: " + crl.getName() + " with " + runs.size() + " runs.");
		crls.add(crl);
	}
	
	//Creates a categoryRunList for a category and level (level may be null)
	public static void getCatRuns(Game g, Category c, ArrayList<categoryRunList> crls, Level lev, ArrayList<Run> categoryRuns) throws IOException, InterruptedException {
		ArrayList<Run> runs = categoryRuns;
		
		if(runs.size() == 0)
			return;
		
		categoryRunList crl;
		
		if(lev == null) { // Not a level category
			TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
			crl = Failsafe.with(retryPolicy).get(() -> new categoryRunList(g,c,runs));
		} else { // Level category
			TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
			crl = Failsafe.with(retryPolicy).get(() -> new categoryRunList(g,c,runs,lev));
		}
		System.out.println("Added: " + crl.getName() + " with " + runs.size() + " runs.");
		//System.out.println(crl.getNumRunners());
		crls.add(crl);
	}
	
	//recursively finds every possible combination of subcategories
	public static void possSubcategories(int x, List<Subcategory> subcatlist, LinkedHashMap<String,String> subCatValues, Game g, Category c, ArrayList<categoryRunList> crls, Level lev, ArrayList<Run> categoryRuns) throws IOException, InterruptedException {
		
		for(int i=0; i<subcatlist.get(x).getValueIds().length; i++) {
			subCatValues.put(subcatlist.get(x).getId(),subcatlist.get(x).getValueIds()[i]);
			if(x >= subcatlist.size()-1) {
				getSubcatRuns(g, c, subcatlist, subCatValues, crls, lev, categoryRuns);
			} else {
				possSubcategories(x+1, subcatlist, subCatValues, g, c, crls, lev, categoryRuns);
			}

		}
	}
}
