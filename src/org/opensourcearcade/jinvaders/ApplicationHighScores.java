package org.opensourcearcade.jinvaders;

public class ApplicationHighScores extends HighScores {

	protected boolean accessAllowed;

	public ApplicationHighScores() {
		accessAllowed = ToolBox.createHomeDirectory();
		loadScores();
	}

	protected boolean loadScores() {

		if (!accessAllowed) return false;
		String scores = ToolBox.loadGame();
		return deserializeScores(scores);
	}

	protected void saveScores(String playerName, int score) {

		if (!accessAllowed) return;
		String serialized=serializeScores(highScores);
		ToolBox.saveGame(serialized);
	}
}
