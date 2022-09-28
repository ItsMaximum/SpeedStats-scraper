package com.tsunderebug.speedrun4j.game;

import com.google.gson.Gson;
import com.tsunderebug.speedrun4j.Speedrun4J;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GameList {

	private Game[] data;

	public static GameList withName(String name) throws IOException {
		Gson g = new Gson();
		URL u = new URL(Speedrun4J.getAPI_ROOT() + "games?name=" + name.replaceAll(" ", "_"));
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.addRequestProperty("User-Agent", Speedrun4J.USER_AGENT);
		InputStreamReader r = new InputStreamReader(c.getInputStream());
		GameList l = g.fromJson(r, GameList.class);
		r.close();
		return l;
	}
	public static GameList fromPlatform(String name) throws IOException {
		Gson g = new Gson();
		URL u = new URL(Speedrun4J.getAPI_ROOT() + "games?platform=" + name.replaceAll(" ", "_") + "&_bulk=yes&max=1000");
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.addRequestProperty("User-Agent", Speedrun4J.USER_AGENT);
		InputStreamReader r = new InputStreamReader(c.getInputStream());
		GameList l = g.fromJson(r, GameList.class);
		r.close();
		return l;
	}
	public static GameList fromPlatformAndOffset(String name, int offset) throws IOException {
		Gson g = new Gson();
		URL u = new URL(Speedrun4J.getAPI_ROOT() + "games?platform=" + name.replaceAll(" ", "_") + "&_bulk=yes&max=1000" + "&offset=" + offset);
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.addRequestProperty("User-Agent", Speedrun4J.USER_AGENT);
		InputStreamReader r = new InputStreamReader(c.getInputStream());
		GameList l = g.fromJson(r, GameList.class);
		r.close();
		return l;
	}
	public static GameList fromSeriesId(String id) throws IOException {
		Gson g = new Gson();
		URL u = new URL(Speedrun4J.getAPI_ROOT() + "series/" + id + "/games?_bulk=yes&max=1000");
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.addRequestProperty("User-Agent", Speedrun4J.USER_AGENT);
		InputStreamReader r = new InputStreamReader(c.getInputStream());
		GameList l = g.fromJson(r, GameList.class);
		r.close();
		return l;
	}
	public static GameList fromOffset(int offset) throws IOException {
		Gson g = new Gson();
		URL u = new URL(Speedrun4J.getAPI_ROOT() + "games?_bulk=yes&max=1000" + "&offset=" + offset);
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.addRequestProperty("User-Agent", Speedrun4J.USER_AGENT);
		InputStreamReader r = new InputStreamReader(c.getInputStream());
		GameList l = g.fromJson(r, GameList.class);
		r.close();
		return l;
	}

	public Game[] getGames() {
		return data;
	}

}
