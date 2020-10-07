package org.opensourcearcade.jinvaders;

public class Speeds {
    // speed in pixels per sec
    private static final int PLAYER_SPEED = 200;
    private static final int PLAYER_SHOT_SPEED = 300;
    private static final int ALIEN_SPEED = 400;
    private static final int UFO_SPEED = 75;
    private static final int ALIEN_SHOT_SPEED = 150;
    private static final int ALIEN_SHOT_FREQ = 1000;

    public Speeds() {
    }

    public static int getPlayerSpeed() {
        return PLAYER_SPEED;
    }

    public static int getPlayerShotSpeed() {
        return PLAYER_SHOT_SPEED;
    }

    public static int getAlienSpeed() {
        return ALIEN_SPEED;
    }

    public static int getUfoSpeed() {
        return UFO_SPEED;
    }

    public static int getAlienShotSpeed() {
        return ALIEN_SHOT_SPEED;
    }

    public static int getAlienShotFreq() {
        return ALIEN_SHOT_FREQ;
    }
}
