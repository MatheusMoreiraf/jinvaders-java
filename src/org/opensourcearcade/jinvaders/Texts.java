package org.opensourcearcade.jinvaders;

public class Texts {
    public static final String VERSION = "2.2";

    private static final String STR_GAME_OVER = "GAME OVER";
    private static final String STR_CLICK_TO_START = "CLICK HERE TO BEGIN";
    private static final String STR_PRESS_ENTER = "PRESS ENTER TO PLAY";
    private static final String STR_HISCORE = "HISCORE";
    private static final String STR_HIGHSCORE_LIST = "HIGHSCORES";
    private static final String STR_INPUTNAME = "INPUT YOUR NAME";
    private static final String STR_PAUSED = "PAUSED, CLICK HERE";
    private static final String[] STR_HELP = {"ENTER = START GAME", "SPACE = FIRE", "MOVE = CURSOR LEFT/RIGHT", "ESC = LEAVE GAME"};
    private static final String[] SPLASH_SCORE_TABLE = {"*SCORE ADVANCE TABLE*", "=? MYSTERY", "=30 POINTS", "=20 POINTS", "=10 POINTS"};
    private static final String[] PLAY_INVADERS = {"PLAY", "JINVADERS", "V" + VERSION};

    public Texts() {
    }

    public static String getStrGameOver() {
        return STR_GAME_OVER;
    }

    public static String getStrClickToStart() {
        return STR_CLICK_TO_START;
    }

    public static String getStrPressEnter() {
        return STR_PRESS_ENTER;
    }

    public static String getStrHiscore() {
        return STR_HISCORE;
    }

    public static String getStrHighscoreList() {
        return STR_HIGHSCORE_LIST;
    }

    public static String getStrInputname() {
        return STR_INPUTNAME;
    }

    public static String getStrPaused() {
        return STR_PAUSED;
    }

    public static String[] getStrHelp() {
        return STR_HELP;
    }

    public static String[] getSplashScoreTable() {
        return SPLASH_SCORE_TABLE;
    }

    public static String[] getPlayInvaders() {
        return PLAY_INVADERS;
    }
}
