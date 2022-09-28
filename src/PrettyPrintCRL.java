import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.tsunderebug.speedrun4j.game.Game;
import com.tsunderebug.speedrun4j.game.Leaderboard;
import com.tsunderebug.speedrun4j.game.categoryRunList;
import com.tsunderebug.speedrun4j.game.run.PlacedRun;
import com.tsunderebug.speedrun4j.game.run.Player;
import com.tsunderebug.speedrun4j.game.run.Run;
import com.tsunderebug.speedrun4j.platform.Platform;

public class PrettyPrintCRL {
       public static final String APPLICABLE_GAME = "rbce";
       public static final boolean REMOVE_GAME_NAME = true;
       public static void main(String[] args) throws InterruptedException, IOException {
    	   
              Game[] games = new Game[1];
              games[0] = Game.fromID(APPLICABLE_GAME);
              
              Gson gson = new Gson();
              
              JsonReader pfr = new JsonReader(new FileReader("playerNames.json"));
              JsonReader sfr = new JsonReader(new FileReader("platformNames.json"));
              
              HashMap<String,String> playerNames = gson.fromJson(pfr, HashMap.class);
              HashMap<String,String> platformNames = gson.fromJson(sfr, HashMap.class);
              
              DateFormat dform = new SimpleDateFormat("MM-dd-yy");
              String date = dform.format(new Date());

              for(Game sg : games) {
            	  	String fileName = "games/" + sg.getNames().get("international").replaceAll("[\\\\/:*?\"<>|]", "") + ".json";
            	  	 
            	  	File file = new File(fileName);

            	  	JsonReader reader = new JsonReader(new FileReader(file));
                    Game g = gson.fromJson(reader, Game.class);
                    ArrayList<categoryRunList> crls = g.getCategoryRunLists();
                    
                    for(categoryRunList crl : crls) {
                    	
                    	makeLeaderboardCSV(crl,playerNames,platformNames,date);  
                    	makeRunListCSV(crl,playerNames,platformNames,date);  
                    }
                    	
                    reader.close();    
             }  
              pfr.close();
              FileWriter pfw = new FileWriter("playerNames.json");
              pfw.write(gson.toJson(playerNames));
              pfw.close();
              
              FileWriter sfw = new FileWriter("platformNames.json");
              sfw.write(gson.toJson(platformNames));
              sfw.close(); 
       }
       
       public static void makeLeaderboardCSV(categoryRunList crl, HashMap<String,String> playerNames, HashMap<String,String> platformNames, String date) throws IOException, InterruptedException {
	    	String fileName = crl.getName().replaceAll("[\\\\/:*?\"<>|]", "") + " - Leaderboard.csv";
	    	if(REMOVE_GAME_NAME) {
	    		fileName = fileName.substring(fileName.indexOf(crl.getCategory().getName()));
	    	}
    	   FileWriter leaderboardcsv = new FileWriter("sheets/" + fileName);
	       	leaderboardcsv.append("Place,Player,Time,Date,Platform,Video Link,Comment,\n");
	       	Leaderboard l = crl.getLeaderboard();
	       	for(PlacedRun pr : l.getRuns()) {
	       		
				Run r = pr.getRun();
				Player p = r.getPlayers()[0];
				
				String platform = getPlatformName(r.getSystem().getPlatform(), platformNames);
				
	       		String video;
	       		try {
	       			video = r.getVideos().getLinks()[0].getUri();
	       		} catch(Exception e) {
	       			video = "none";
	       		}
	       		
	       		String comment = "";
	       		if(r.getComment() != null) {
	       			comment = r.getComment().replaceAll("[\r\n]+", " ").replaceAll("[,]+", "/");
	       		}
	       		leaderboardcsv.append(pr.getPlace() + "," + getPlayerName(p,playerNames) + "," + r.getFormattedPrimaryTime() + "," + 
	       				r.getDate() + "," + platform + "," + video + "," + comment + "\n");
	       	}
	       	leaderboardcsv.close();
       }
       
       public static void makeRunListCSV(categoryRunList crl, HashMap<String,String> playerNames, HashMap<String,String> platformNames, String date) throws IOException, InterruptedException {
    	   String fileName =  crl.getName().replaceAll("[\\\\/:*?\"<>|]", "") + " - Run List.csv";
	    	if(REMOVE_GAME_NAME) {
	    		fileName = fileName.substring(fileName.indexOf(crl.getCategory().getName()));
	    	}
    	   FileWriter runlistcsv = new FileWriter("sheets/" + fileName);
	       	runlistcsv.append("Player,Time,Date,Platform,Video Link,Comment,\n");
       		for(Run r : crl.getRuns()) {
				Player p = r.getPlayers()[0];	
				
				String platform = getPlatformName(r.getSystem().getPlatform(), platformNames);
				
	       		String video;
	       		try {
	       			video = r.getVideos().getLinks()[0].getUri();
	       		} catch(Exception e) {
	       			video = "none";
	       		}
	       		
	       		String comment = "";
	       		if(r.getComment() != null) {
	       			comment = r.getComment().replaceAll("[\r\n]+", " ").replaceAll("[,]+", "/");
	       		}
	       		runlistcsv.append(getPlayerName(p,playerNames) + "," + r.getFormattedPrimaryTime() + "," + 
	       				r.getDate() + "," + platform + "," + video + "," + comment + "\n");
		       	}
       		runlistcsv.close();
       }
       
       public static String getPlayerName(Player p, HashMap<String,String> playerNames) throws IOException, InterruptedException {
    	   String id = p.getID();
           String name;
           if(id != null && playerNames.get(id) == null) { //user with name not in database
                  TimeUnit.MILLISECONDS.sleep(600);
                  name = p.getName();
                  System.out.println("Adding: " + name);
                  playerNames.put(id, name);
           } else if(id == null) { //guest 
                  name = p.getName();
           } else { //user in database;
                  name = playerNames.get(id);
           }
           return name;	
       }
       
       public static String getPlatformName(String id, HashMap<String,String> platformNames) throws InterruptedException, IOException {
           String name;
           if(platformNames.get(id) == null) { //platform name not in database
                  TimeUnit.MILLISECONDS.sleep(600);
                  name = Platform.fromID(id).getName();
                  System.out.println("Adding Platform: " + name);
                  platformNames.put(id, name);
           } else { //platform in database;
                  name = platformNames.get(id);
           }
           return name;	
       }
}



