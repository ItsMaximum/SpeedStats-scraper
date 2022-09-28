package com.tsunderebug.speedrun4j;

import com.tsunderebug.speedrun4j.user.User;

import java.io.IOException;

public class UserTest {

	public static void main(String[] args) throws IOException {
		User u = User.fromApiKey(Speedrun4J.API_KEY);
		System.out.println(u.getNames().get("international"));
	}

}
