import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.tsunderebug.speedrun4j.game.Game;
import com.tsunderebug.speedrun4j.game.GameList;
import com.tsunderebug.speedrun4j.game.Leaderboard;
import com.tsunderebug.speedrun4j.game.categoryRunList;
import com.tsunderebug.speedrun4j.game.run.PlacedRun;
import com.tsunderebug.speedrun4j.game.run.Player;
import com.tsunderebug.speedrun4j.game.run.Run;
import com.tsunderebug.speedrun4j.platform.Platform;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

public class GenerateRunListCSV {
	public static final String APPLICABLE_SERIES = "redball";
	
	public static RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
			  .handle(IOException.class)
			  .withDelay(Duration.ofSeconds(10))
			  .onFailedAttempt(e -> System.out.println("Connection attempt failed" + e.getLastFailure()))
			  .withMaxRetries(4)
			  .build();
	
	public static int rateLimit = 560;
	
	public static void main(String[] args) throws InterruptedException, IOException {

		Game[] games = GameList.fromSeriesId(APPLICABLE_SERIES).getGames();
		// Game[] games = new Game[1];

		// games[0] = Game.fromID("bfbb");

		//int count = 0;
		//int offset = 0;
		// List<Game> games = new ArrayList<Game>();
		// games.add(Game.fromID("sm63"));
		// get a complete run list using offsets
		/*
		 * while(true) { count = 0; for(Game g:
		 * GameList.fromPlatformAndOffset(APPLICABLE_SERIES,offset).getGames()) {
		 * games.add(g); count++; } TimeUnit.MILLISECONDS.sleep(1000); if(count<1000)
		 * break; offset+=count; }
		 */
		Gson gson = new Gson();

		JsonReader pfr = new JsonReader(new FileReader("playerNames.json",StandardCharsets.UTF_8));
		JsonReader plfr = new JsonReader(new FileReader("platformNames.json",StandardCharsets.UTF_8));

		HashMap<String, String> playerNames = gson.fromJson(pfr, HashMap.class);
		HashMap<String, String> platformNames = gson.fromJson(plfr, HashMap.class);

		ArrayList<categoryRunList> categoryRunLists = new ArrayList<categoryRunList>();
		ArrayList<String> ignoredIds = new ArrayList<String>();

		FileWriter runValues = new FileWriter("runValues.csv",StandardCharsets.UTF_8);
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		File file = new File("games/");
        File[] files = file.listFiles();
       
           /*
		for (Game sg : games) {
			String gameName = sg.getNames().get("international");
			String fileName = "games/" + gameName.replaceAll("[\\\\/:*?\"<>|]", "") + ".json";

			File file = new File(fileName);
			*/
        for(File f: files){
            System.out.println(f.getName());
			JsonReader reader = new JsonReader(new FileReader(f,StandardCharsets.UTF_8));
			Game g = gson.fromJson(reader, Game.class);
			ArrayList<categoryRunList> crls = g.getCategoryRunLists();

			for (categoryRunList crl : crls) {
				categoryRunLists.add(crl);
				Leaderboard l = crl.getLeaderboard();
				ArrayList<String> idsCredited = new ArrayList<String>();

				for (PlacedRun pr : l.getRuns()) {
					Run r = pr.getRun();
					Player[] pl = r.getPlayers();

					for (Player p : pl) {
						try {
							int numPlayers = pl.length;
							String id = p.getID();
							if(ignoredIds.contains(id)) // Player deleted their account
								id = null;
							if (id != null && playerNames.get(id) == null) { // user with name not in database
								TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
								String name = Failsafe.with(retryPolicy).get(() -> p.getName());
								System.out.println("Adding Player: " + name);
								playerNames.put(id, name);
							} 
							
							String platformId = r.getSystem().getPlatform();
							if (platformId != null && platformNames.get(platformId) == null) { // platform name not in database
								TimeUnit.MILLISECONDS.sleep(rateLimit); // Rate limit API Call
								String name = Failsafe.with(retryPolicy).get(() -> Platform.fromID(platformId).getName());
								System.out.println("Adding Platform: " + name);
								platformNames.put(platformId, name);
							} 
							
							if (id != null && !idsCredited.contains(id)) { // disregard runs from guest users
								double runValue = pr.getRunValue() / numPlayers;
								String gameName = g.getNames().get("international").replace(",", ".");
								String platform = platformId == null ? "\\N" : platformNames.get(platformId);
								String date = r.getDate() == null ? "\\N" : r.getDate();
								runValues.append((gameName + "," + crl.getName().replace("\\","\\\\") + "," + playerNames.get(id) + "," + platform + "," + 
										pr.getPlace() + "," + r.getTimes().getPrimaryT() + "," + df.format(runValue) + "," + date + "\n"));
								idsCredited.add(id);
							}
						} catch(Exception e) {
							System.out.println("Error occured while adding run. Ignoring future runs from player.");
							ignoredIds.add(p.getID());
						}
					}
				}
				reader.close();
			}
		}
		runValues.close();
		pfr.close();
		plfr.close();
		
		FileWriter pfw = new FileWriter("playerNames.json",StandardCharsets.UTF_8);
		pfw.write(gson.toJson(playerNames));
		pfw.close();
		
		FileWriter plfw = new FileWriter("platformNames.json",StandardCharsets.UTF_8);
		plfw.write(gson.toJson(platformNames));
		plfw.close();
		
		
	}
}
