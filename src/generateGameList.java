import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.tsunderebug.speedrun4j.game.Game;
import com.tsunderebug.speedrun4j.game.GameList;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

public class generateGameList {
	public static int rateLimit = 560;
	public static RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
			  .handle(IOException.class)
			  .withDelay(Duration.ofSeconds(10))
			  .onFailedAttempt(e -> System.out.println("Connection attempt failed" + e.getLastFailure()))
			  .withMaxRetries(10)
			  .build();
	
	public static void main(String[] args) throws IOException, InterruptedException {
		int count = 0;
		int offset = 0;
		Gson gson = new Gson();
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
			if(count < 1000)
				break;
			offset+=count;
		}
		System.out.println(games.size());
		Collections.shuffle(games);
		FileWriter fw = new FileWriter("games.json",StandardCharsets.UTF_8);
		fw.write(gson.toJson(games));
		fw.close();
	}
	
}
