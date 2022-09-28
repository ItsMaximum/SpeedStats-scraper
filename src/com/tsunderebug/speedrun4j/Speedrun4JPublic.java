package com.tsunderebug.speedrun4j;

public class Speedrun4JPublic {

	private Speedrun4JPublic(){}

	public static int proxyNum = 0;
	public static final String USER_AGENT = "Speedrun4j/1.0";
	public static final String API_KEY = "";
	
	public static String getAPI_ROOT() {
		return "https://www.speedrun.com/api/v1/";
	}
}
