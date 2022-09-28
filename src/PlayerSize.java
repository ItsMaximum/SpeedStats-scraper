import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class PlayerSize {
	public static void main(String[] args) throws IOException {
		Gson gson = new Gson();
		JsonReader pfr = new JsonReader(new FileReader("playerNames.json",StandardCharsets.UTF_8));
		HashMap<String, String> playerNames = gson.fromJson(pfr, HashMap.class);
		System.out.println(playerNames.keySet().size());
	}
}
