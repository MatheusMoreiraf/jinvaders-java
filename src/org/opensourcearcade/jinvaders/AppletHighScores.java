package org.opensourcearcade.jinvaders;

public class AppletHighScores extends HighScores {

	protected final static String INET_URL = "http://www.opensourcearcade.org/";
	protected String game, computer, user;

	public AppletHighScores() {
		highScores.add("PETER");
		highScores.add("1000");
		highScores.add("PAUL");
		highScores.add("500");
	}
	
	protected boolean loadScores() {
		// TODO
		return false;
	}

	protected void saveScores(String playerName1, int score1) {
		// TODO
	}
}
