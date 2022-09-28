import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

import dev.failsafe.Failsafe;

public class GeneratePlayerNames {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
		
		File folder = new File("games");
		File[] listOfFiles = folder.listFiles();
		
		Gson gson = new Gson();
		
		JsonReader pfr = new JsonReader(new FileReader("playerNames.json"));
		
		HashMap<String,String> playerNames = gson.fromJson(pfr, HashMap.class);
		
		//HashMap<String,String> playerNames = new HashMap<String,String>();
		
		
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	JsonReader reader = new JsonReader(new FileReader(file));
		        Game g = gson.fromJson(reader, Game.class);
		        ArrayList<categoryRunList> crls = g.getCategoryRunLists();
		        
		        for(categoryRunList crl : crls) {
		        	Leaderboard l = crl.getLeaderboard();
		        	
		        	for(PlacedRun pr : l.getRuns()) {
		        		Run r = pr.getRun();
		        		Player[] pl = r.getPlayers();
		        		
		        		for(Player p : pl) {
		        			String id = p.getID();
		        			if(id != null && playerNames.get(id) == null) {
		        				TimeUnit.MILLISECONDS.sleep(600);
		        				String name = Failsafe.with(GenerateCategoryRunLists.retryPolicy).get(() -> p.getName());
		        				System.out.println("Adding: " + name);
		        				playerNames.put(id, p.getName());
		        			}
		        		}
		        	}
		        }
		        reader.close();
		    }
		}
		
		pfr.close();
		FileWriter pfw = new FileWriter("playerNames.json");
		pfw.write(gson.toJson(playerNames));
		pfw.close();
	}
}
