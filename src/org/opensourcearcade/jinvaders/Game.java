package org.opensourcearcade.jinvaders;

import org.opensourcearcade.jinvaders.Sound.SOUNDS;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * <p>
 * Title: JInvaders
 * </p>
 *
 * <p>
 * Description: Clone of the arcade game machine
 * </p>
 *
 * <p>
 * License: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 * </p>
 *
 * @author Michael Brandt
 */

public final class Game extends Applet implements Runnable {

    private static final long serialVersionUID = 1802938807824847849L;

    public static final String VERSION = "2.2";
    public static final int WIDTH = 449;
    public static final int HEIGHT = 480;
    public static final int FRAMES_PER_SECOND = 30;

    private GameStates gameState = GameStates.SPLASH_SCREEN;

    private static final int LIVES = 3;
    private static final int FRAMES_PER_IMAGE = 3;

    private static final int BOTTOM_LINE_POS = 462;
    private static final int PLAYER_Y_POS = 416;
    private static final int UFO_Y_POS = 75;
    private static final int ALIENS_X_POS = 68;
    private static final int ALIENS_Y_POS = 112;
    private static final int BUNKERS_Y_POS = 368;

    private static final NumberFormat NUM_FORMAT = new DecimalFormat("000000");

    private HighScores highScores;

    private Graphics2D g2d;

    private Entity player, ufo, playerShot, alienShot;
    private Entity[][] aliens = new Entity[5][11];
    private Entity[][] bunkers = new Entity[4][20];

    private boolean paused;

    private String playerName1, playerName2, tmpPlayerName;
    private int caretPos;

    private int score1, score2, highscore, lives1; // , lives2;
    private int alienCtr, soundCtr, ufoCntDown;

    private float alienSX;

    private long frameCtr, shootCtr, splashScreenTimer;
    private long lastShotTime, lastSoundTime;
    private long shot_freq; // per nanos

    private Font font;

    private Panel panel;

    private Thread gameLoopThread;
    private long lastUpdate;

    private static Texts TEXTS = new Texts();
    private static Speeds SPEEDS = new Speeds();
    private Imagens imagens = new Imagens();
    private Keyboard keyboard = new Keyboard();

    public static void main(String[] args) {

        try {

            // fix the JNLP desktop icon exec rights bug (Linux only)
            if (System.getProperty("os.name").toLowerCase().indexOf("linux") != -1) {
                java.io.File desktop = new java.io.File(System.getProperty("user.home") + "/Desktop");
                if (desktop.exists()) {
                    // TODO filter files for "jinvaders" and ".desktop" extension
                    java.io.File[] files = desktop.listFiles();
                    for (java.io.File file : files) {
                        if (file.getName().contains("jws_app_shortcut_")) {
                            file.setExecutable(true, false);
                        }
                    }
                }
            }

            Game game = new Game();
            String name = ToolBox.getPackageName();
            Image iconImg = ToolBox.loadImage(ToolBox.getURL(name + ".png"));

            Frame frame = new Frame(name);
            frame.setIconImage(iconImg);
            frame.setLayout(new BorderLayout());
            frame.add(game.getPanel(), BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            frame.setVisible(true);

            game.init();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    public Game() {
        System.out.println(System.getProperty("java.vm.name") + System.getProperty("java.vm.version"));
        System.out.println(ToolBox.getPackageName() + " v" + VERSION);

        panel = new Panel();
        panel.setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));
        panel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                keyboard.keyEvent(event, true);
            }

            public void keyReleased(KeyEvent event) {
                keyboard.keyEvent(event, false);
            }
        });

        panel.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                pause();
            }

            public void focusGained(FocusEvent arg0) {
                resume();
            }
        });

        imagens.setBackbuffer(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB));

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
    }

    @Override
    public void init() {

        setSize(WIDTH, HEIGHT);

        boolean isApplet = (null != System.getSecurityManager());
        highScores = isApplet ? new AppletHighScores() : new ApplicationHighScores();

        imagens.setBackbuffer(ToolBox.convertToCompatibleImage(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)));
        g2d = imagens.getBackbuffer().createGraphics();
        Color cordeFundo = new Color(28, 28, 28);
        g2d.setBackground(cordeFundo);

        playerName1 = "PLAYER1";
        playerName2 = "PLAYER2";
        tmpPlayerName = playerName1;
        caretPos = playerName1.length();

        try {
            imagens.setImagens();

            Sound.loadSound(SOUNDS.SHOT, ToolBox.getURL("sounds/PlyShot_44.wav"));
            Sound.loadSound(SOUNDS.PLY_HIT, ToolBox.getURL("sounds/PlyHit_44.wav"));
            Sound.loadSound(SOUNDS.INV_HIT, ToolBox.getURL("sounds/InvHit_44.wav"));
            Sound.loadSound(SOUNDS.BASE_HIT, ToolBox.getURL("sounds/BaseHit_44.wav"));
            Sound.loadSound(SOUNDS.UFO, ToolBox.getURL("sounds/Ufo.wav"));
            Sound.loadSound(SOUNDS.UFO_HIT, ToolBox.getURL("sounds/UfoHit.wav"));
            Sound.loadSound(SOUNDS.WALK1, ToolBox.getURL("sounds/Walk1.wav"));
            Sound.loadSound(SOUNDS.WALK2, ToolBox.getURL("sounds/Walk2.wav"));
            Sound.loadSound(SOUNDS.WALK3, ToolBox.getURL("sounds/Walk3.wav"));
            Sound.loadSound(SOUNDS.WALK4, ToolBox.getURL("sounds/Walk4.wav"));
            Sound.setEnabled(true);

            font = ToolBox.loadFont(ToolBox.getURL("ARCADEPI.TTF"));
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }

        panel.requestFocus();

        resetGame();

        lastUpdate = System.nanoTime();
        gameLoopThread = new Thread(this);
        gameLoopThread.start();
    }

    public void updateGame(long time) {
        // state-dependent updates

        switch (gameState) {
            case SPLASH_SCREEN:
                updateSplashScreen(time);
                break;
            case HELP_SCREEN:
                updateHelpScreen(time);
                break;
            case HIGH_SCORE_SCREEN:
                updateHighScoreScreen(time);
                break;
            case IN_GAME_SCREEN:
                updateInGameScreen(time);
                break;
            case INPUT_NAME_SCREEN:
                updateInputNameScreen(time);
                break;
            case GAME_OVER_SCREEN:
                if (keyboard.isEnterKey()) {
                    keyboard.setEnterKey(false);
                    if (score1 > 0)
                        gameState = GameStates.INPUT_NAME_SCREEN;
                    else
                        gameState = GameStates.SPLASH_SCREEN;
                }
                break;
            default:
                break;
        }

        // state-independent updates

        if (keyboard.getLastKey() == KeyEvent.VK_S) {
            keyboard.setLastKey(0);
            boolean enabled = !Sound.isEnabled();
            Sound.setEnabled(enabled);
            if (enabled && ufo.visible && ufo.frame == 0)
                Sound.loop(SOUNDS.UFO);
        }
    }

    private void updateSplashScreen(long time) {
        if (keyboard.isEnterKey()) {
            keyboard.setEnterKey(false);
            resetGame();
            gameState = GameStates.IN_GAME_SCREEN;
            return;
        }

        splashScreenTimer -= time;
        if (splashScreenTimer <= 0) {
            highscore = highScores.getHighScore();
            splashScreenTimer = 5000000000L;
            gameState = GameStates.HIGH_SCORE_SCREEN;
        }
    }

    private void updateHelpScreen(long time) {
        if (keyboard.isEnterKey()) {
            keyboard.setEnterKey(false);
            resetGame();
            gameState = GameStates.IN_GAME_SCREEN;
            return;
        }

        splashScreenTimer -= time;
        if (splashScreenTimer <= 0) {
            highscore = highScores.getHighScore();
            splashScreenTimer = 5000000000L;
            gameState = GameStates.SPLASH_SCREEN;
        }
    }

    private void updateHighScoreScreen(long time) {
        if (keyboard.isEnterKey()) {
            keyboard.setEnterKey(false);
            resetGame();
            gameState = GameStates.IN_GAME_SCREEN;
            return;
        }

        Object[] scores = highScores.getHighScores();
        highscore = (scores.length > 0) ? Integer.parseInt((String) scores[1]) : 0;

        splashScreenTimer -= time;
        if (splashScreenTimer <= 0) {
            splashScreenTimer = 4000000000L;
            gameState = GameStates.HELP_SCREEN;
        }
    }

    private void updateInGameScreen(long time) {
        if (keyboard.isEnterKey()) {
            keyboard.setEnterKey(false);
            resetGame();
            gameState = GameStates.SPLASH_SCREEN;
            return;
        }

        if (!paused) {
            updateShooting(time);

            updatePositions(time);

            updateCollisions(time);

            updateExplosions(time);

            playWalkingSound(time);

            // no more aliens ?
            if (alienCtr == 0) {
                // create new wave
                int dx = imagens.getE1Img().getWidth() / 3 + 4;
                for (int y = 0; y < aliens.length; y++)
                    for (int x = 0; x < aliens[y].length; x++) {
                        Entity alien = aliens[y][x];
                        alien.x = ALIENS_X_POS + x * dx + (dx / 2 - alien.w / 2);
                        alien.y = ALIENS_Y_POS + y * alien.h * 2;
                        alien.frame = 0;
                        alien.visible = true;
                    }

                // reset alien data
                alienCtr = aliens.length * aliens[0].length;
                --alienSX;
                shot_freq = (long) (0.9f * (float) shot_freq);
            }
        }
    }

    private void updateInputNameScreen(long time) {
        if (keyboard.isEnterKey()) {
            keyboard.setEnterKey(false);
            if (tmpPlayerName.isEmpty())
                tmpPlayerName = playerName1;

            playerName1 = tmpPlayerName;
            caretPos = playerName1.length();
            highScores.postHighScore(playerName1, score1);

            gameState = GameStates.HIGH_SCORE_SCREEN;
        } else if (keyboard.isBackKey()) {
            if (caretPos > 0) {
                caretPos--;
                if (caretPos > 0)
                    tmpPlayerName = tmpPlayerName.substring(0, caretPos);
                else
                    tmpPlayerName = "";
            }
            keyboard.setBackKey(false);
        } else if (keyboard.getLastKey() != 0) {
            if (caretPos < 8) {
                int strLen = tmpPlayerName.length();
                String s1 = (caretPos > 0) ? tmpPlayerName.substring(0, caretPos) : "";
                String s2 = (caretPos < strLen) ? tmpPlayerName.substring(caretPos + 1, strLen - 1) : "";
                tmpPlayerName = s1 + KeyEvent.getKeyText(keyboard.getLastKey()) + s2;
                caretPos++;
            }
            keyboard.setLastKey(0);
        }
    }

    private void updateShooting(long time) {

        Entity shot;
        if (keyboard.isSpaceKey() && keyboard.isSpaceKeyReleased() && !playerShot.visible && player.frame == 0) {
            keyboard.setSpaceKeyReleased(false);
            long t = System.nanoTime();
            if (t - lastShotTime > 300000) {
                lastShotTime = t;
                Sound.play(SOUNDS.SHOT);
                playerShot = new Entity();
                playerShot.x = player.x + player.w / 2 - 1;
                playerShot.y = player.y - 10;
                playerShot.w = 2;
                playerShot.h = 8;
                playerShot.sy = -(Math.round(10.0f * (float) SPEEDS.getPlayerShotSpeed() / (float) FRAMES_PER_SECOND)) / 10.0f;
            }
        }

        shootCtr += 1000 / FRAMES_PER_SECOND;
        if (shootCtr > shot_freq) {
            Entity shooter = null;
            while (shooter == null) {
                int x = (int) (Math.random() * aliens[0].length);
                for (int y = aliens.length - 1; y >= 0; y--)
                    if (aliens[y][x].visible) {
                        shooter = aliens[y][x];
                        break;
                    }
            }

            shot = new Entity();
            shot.x = shooter.x + shooter.w / 2 - 1;
            shot.y = shooter.y + shooter.h;
            shot.w = 2;
            shot.h = 8;
            shot.sy = (Math.round(10.0f * (float) SPEEDS.getAlienShotSpeed() / (float) FRAMES_PER_SECOND)) / 10.0f;

            if (alienShot == null)
                alienShot = shot;
            else {
                shot.prev = alienShot;
                alienShot = shot;
            }

            shootCtr = 0;
        }
    }

    private void updateExplosions(long time) {
        // player exploding ?
        if (player.frame != 0) {
            player.cntDown -= 1000 / FRAMES_PER_SECOND;
            if (player.cntDown < 0) {
                player.cntDown = 0;
                player.frame = 0;
            }
        }

        // ufo exploding ?
        if (ufo.visible && ufo.frame != 0) {
            ufo.cntDown -= 1000 / FRAMES_PER_SECOND;
            if (ufo.cntDown < 0) {
                if (ufo.frame == 1) {
                    ufo.cntDown = 1000;
                    ufo.frame = 2;
                } else {
                    ufo.visible = false;
                    ufo.frame = 0;
                    score1 += (int) (Math.random() * 10) * 100;
                }
            }
        }
    }

    private void updateCollisions(long time) {
        for (int y = 0; y < aliens.length && gameState == GameStates.IN_GAME_SCREEN; y++)
            for (int x = 0; x < aliens[y].length && gameState == GameStates.IN_GAME_SCREEN; x++) {
                Entity alien = aliens[y][x];
                if (alien.visible && alien.frame < 2) {
                    // alien ./. playershot
                    if (playerShot.visible && ToolBox.checkCollision(playerShot, alien)) {
                        Sound.play(SOUNDS.INV_HIT);
                        --alienCtr;
                        alien.frame = FRAMES_PER_IMAGE - 1;
                        playerShot.visible = false;

                        if (alien.image == imagens.getE1Img())
                            score1 += 10;
                        else if (alien.image == imagens.getE2Img())
                            score1 += 20;
                        else
                            score1 += 30;
                    }
                    // player ./. alien
                    else if (player.frame == 0 && player.visible && ToolBox.checkCollision(player, alien)) {
                        Sound.play(SOUNDS.PLY_HIT);
                        Sound.play(SOUNDS.INV_HIT);
                        --alienCtr;
                        alien.frame = FRAMES_PER_IMAGE - 1;

                        player.dx = 0;
                        player.frame = 1;
                        player.cntDown = 2000;
                        if (--lives1 == 0) {
                            gameState = GameStates.GAME_OVER_SCREEN;
                            if (ufo.visible) {
                                Sound.stop(SOUNDS.UFO);
                                ufo.visible = false;
                            }
                        }
                        continue;
                    } else {
                        // alien ./. bunker
                        for (int b = 0; b < bunkers.length; b++)
                            for (int yy = 0; yy < 4; yy++)
                                for (int xx = 0; xx < 5; xx++) {
                                    Entity bk = bunkers[b][yy * 5 + xx];
                                    if (bk.visible && ToolBox.checkCollision(alien, bk)) {
                                        bk.visible = false;
                                        Sound.play(SOUNDS.BASE_HIT);
                                    }
                                }
                    }
                }
            }

        // ufo ./. playershot
        if (ufo.frame < 2 && ufo.visible && playerShot.visible && ToolBox.checkCollision(playerShot, ufo)) {
            ufo.frame = 1;
            ufo.cntDown = 1000;
            playerShot.visible = false;
            Sound.stop(SOUNDS.UFO);
            Sound.play(SOUNDS.UFO_HIT);
        }

        Entity shot = alienShot;
        while (shot != null) {
            // alienShot ./. player
            if (player.frame == 0 && shot.visible && ToolBox.checkCollision(shot, player)) {
                shot.y = HEIGHT + shot.h;

                Sound.play(SOUNDS.PLY_HIT);
                player.dx = 0;
                player.frame = 1;
                player.cntDown = 2000;
                if (--lives1 == 0) {
                    gameState = GameStates.GAME_OVER_SCREEN;
                    if (ufo.visible) {
                        Sound.stop(SOUNDS.UFO);
                        ufo.visible = false;
                    }
                }
                break;
            }

            // bunker collision checks
            boolean bnkHit = false;
            for (int b = 0; b < bunkers.length && !bnkHit; b++)
                for (int yy = 0; yy < 4 && !bnkHit; yy++)
                    for (int xx = 0; xx < 5 && !bnkHit; xx++) {
                        Entity bnk = bunkers[b][yy * 5 + xx];
                        if (bnk.visible) {
                            // alienShot ./. bunker
                            if (shot.visible && ToolBox.checkCollision(shot, bnk)) {
                                shot.y = HEIGHT + shot.h;
                                ++bnk.frame;
                                if (bnk.frame > 2)
                                    bnk.visible = false;
                                bnkHit = true;
                            }
                            // playerShot ./. bunker
                            else if (playerShot.visible && ToolBox.checkCollision(playerShot, bnk)) {
                                playerShot.visible = false;
                                ++bnk.frame;
                                if (bnk.frame > 2)
                                    bnk.visible = false;
                                bnkHit = true;
                            }

                            if (bnkHit)
                                Sound.play(SOUNDS.BASE_HIT);
                        }
                    }

            shot = shot.prev;
        }
    }

    private void updatePositions(long time) {
        // --- player ---
        if (player.frame == 0) {
            float delta = player.sx * (time / 1000000000.0f);
            if (keyboard.isLeftKey())
                player.dx = -delta;
            else if (keyboard.isRightKey())
                player.dx = delta;
            else
                player.dx = 0;
        }

        player.x += player.dx;
        if (player.x < 0 || player.x > WIDTH - player.w)
            player.x -= player.dx;

        // --- ufo ---
        if (ufo.visible) {
            if (ufo.frame == 0) {
                float delta = ufo.sx * (time / 1000000000.0f);
                if (ufo.x > WIDTH) {
                    ufo.visible = false;
                    Sound.stop(SOUNDS.UFO);
                } else
                    ufo.x += delta;
            }
        } else {
            ufoCntDown -= 1000 / FRAMES_PER_SECOND;
            if (ufoCntDown < 0) {
                ufo.x = -ufo.image.getWidth(null);
                ufo.visible = true;
                ufoCntDown = 15000 + (2000 - (int) (Math.random() * 4000));
                Sound.loop(SOUNDS.UFO);
            }
        }

        // --- aliens ---
        float alienDelta = 2 * (float) alienSX * (time / 1000000000.0f); // pixels
        // to
        // move
        alienDelta /= (float) (alienCtr + 4); // speed modifier for killed
        // aliens

        boolean bounce = checkAlienBounce(alienDelta);
        if (!bounce) // not bouncing, move aliens sidewards
        {
            float alienMaxY = 0, alienY = 0;
            for (int y = 0; y < aliens.length; y++)
                for (int x = 0; x < aliens[y].length; x++) {
                    Entity alien = aliens[y][x]; // do *not* check for
                    // visibility (bouncing
                    // test!)
                    alien.x += alienDelta;
                    alienY = alien.y + alien.image.getHeight(null);
                    alienMaxY = (alienY > alienMaxY) ? alienY : alienMaxY;
                }
        } else // bouncing, move aliens downwards
        {
            float alienMaxY = 0, alienY = 0;
            alienSX = (bounce) ? -alienSX : alienSX;

            for (int y = 0; y < aliens.length; y++)
                for (int x = 0; x < aliens[y].length; x++) {
                    Entity alien = aliens[y][x]; // do *not* check for
                    // visibility (bouncing
                    // test!)
                    alien.y += 10;
                    alienY = alien.y + alien.image.getHeight(null);
                    alienMaxY = (alienY > alienMaxY) ? alienY : alienMaxY;
                }

            // aliens hit ground ?
            if (alienMaxY >= BOTTOM_LINE_POS - 1) {
                // game over
                lives1 = 0; // lives2 = 0;
                Sound.play(SOUNDS.PLY_HIT);
                player.dx = 0;
                player.frame = 1;
                player.cntDown = 2000;
                gameState = GameStates.GAME_OVER_SCREEN;
                if (ufo.visible) {
                    Sound.stop(SOUNDS.UFO);
                    ufo.visible = false;
                }
            }
        }

        if (playerShot != null) {
            playerShot.y += playerShot.sy;
            if (playerShot.y < -10)
                playerShot.visible = false;
        }

        Entity shot = alienShot;
        while (null != shot) {
            shot.y += shot.sy;
            if (shot.prev != null && shot.prev.y > HEIGHT)
                shot.prev = null;
            shot = shot.prev;
        }
    }

    private boolean checkAlienBounce(float alienDelta) {
        // get most right (or most left) alien for bouncing check
        // (broadest alien is always in last alien row)
        Entity alienToCheck = null;
        if (alienSX > 0)
            alienToCheck = aliens[aliens.length - 1][getMostRightColumn()];
        else
            alienToCheck = aliens[aliens.length - 1][getMostLeftColumn()];

        // check if updated position bounces screen
        float newXpos = alienToCheck.x + alienDelta;

        return ((int) newXpos < 0 || (int) newXpos > WIDTH - alienToCheck.w);
    }

    private int getMostLeftColumn() {
        // get the most left alien (which is alive)
        int column = Integer.MAX_VALUE;
        for (int y = 0; y < aliens.length; y++)
            for (int x = 0; x < aliens[y].length; x++)
                if (aliens[y][x].visible)
                    column = Math.min(column, x);
        return column;
    }

    private int getMostRightColumn() {
        // get the most right alien (which is alive)
        int column = Integer.MIN_VALUE;
        for (int y = 0; y < aliens.length; y++)
            for (int x = 0; x < aliens[y].length; x++)
                if (aliens[y][x].visible)
                    column = Math.max(column, x);
        return column;
    }

    public void paint() {
        frameCtr += 1000 / FRAMES_PER_SECOND;
        if (frameCtr > 300)
            frameCtr = 0;

        g2d.setColor(Color.black);
        g2d.clearRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(Color.white);
        g2d.setFont(font.deriveFont(20f));

        final FontMetrics fm = g2d.getFontMetrics(g2d.getFont());

        int fontHeight = fm.getHeight();

        int names_height = (int) (fontHeight * 1.5);
        ToolBox.drawText(g2d, playerName1, WIDTH / 6, names_height, Color.white);
        ToolBox.drawText(g2d, TEXTS.getStrHiscore(), names_height, Color.white);
        ToolBox.drawText(g2d, playerName2, WIDTH / 6 * 5, names_height, Color.white);

        int score_height = fontHeight * 3;
        ToolBox.drawText(g2d, NUM_FORMAT.format(score1), WIDTH / 6, score_height, Color.white);
        ToolBox.drawText(g2d, NUM_FORMAT.format(highscore), score_height, Color.white);
        ToolBox.drawText(g2d, NUM_FORMAT.format(score2), WIDTH / 6 * 5, score_height, Color.white);

        switch (gameState) {
            case SPLASH_SCREEN:
                drawSplashScreen(g2d, fontHeight);
                break;
            case HELP_SCREEN:
                drawHelpScreen(g2d, fontHeight);
                break;
            case HIGH_SCORE_SCREEN:
                drawHighScoreScreen(g2d, fontHeight);
                break;
            case IN_GAME_SCREEN:
                drawIngameScreen(g2d);
                break;
            case GAME_OVER_SCREEN:
                drawGameOverScreen(g2d, fontHeight);
                break;
            case INPUT_NAME_SCREEN:
                drawInputNameScreen(g2d, fontHeight, fm.stringWidth("M") + 1);
                break;
        }

        if (paused)
            drawClickToContinue(g2d, names_height);
        else if (gameState != GameStates.IN_GAME_SCREEN)
            drawPressEnter(g2d, names_height);

        if (!Sound.isEnabled())
            g2d.drawImage(imagens.getSndOffImg(), WIDTH - imagens.getSndOffImg().getWidth() - 1, BOTTOM_LINE_POS + 2, null);

        panel.getGraphics().drawImage(imagens.getBackbuffer(), 0, 0, null);
    }

    private void drawSplashScreen(Graphics g, int height) {
        if (frameCtr < 250) {
            ToolBox.drawText(g, TEXTS.getPlayInvaders()[0], 6 * height, Color.white);
            ToolBox.drawText(g, TEXTS.getPlayInvaders()[1], 8 * height, Color.white);
            ToolBox.drawText(g, TEXTS.getPlayInvaders()[2], 10 * height, Color.white);
        }

        final int X = 125;

        ToolBox.drawText(g, TEXTS.getSplashScoreTable()[0], WIDTH / 2, 12 * height, Color.white);

        ToolBox.drawText(g, TEXTS.getSplashScoreTable()[1], WIDTH / 2, (int) (14 * height), Color.white);
        ToolBox.drawImageCentered(g, imagens.getUfoImg(), X, 13 * height, 0);

        ToolBox.drawText(g, TEXTS.getSplashScoreTable()[2], WIDTH / 2, (int) (15.5 * height), Color.white);
        ToolBox.drawImageCentered(g, imagens.getE3Img(), X, (int) (14.5 * height), 0);

        ToolBox.drawText(g, TEXTS.getSplashScoreTable()[3], WIDTH / 2, (int) (17 * height), Color.white);
        ToolBox.drawImageCentered(g, imagens.getE2Img(), X, 16 * height, 0);

        ToolBox.drawText(g, TEXTS.getSplashScoreTable()[4], WIDTH / 2, (int) (18.5 * height), Color.green);
        ToolBox.drawImageCentered(g, imagens.getE1Img(), X, (int) (17.5 * height), 0);
    }

    private void drawHelpScreen(Graphics g, int fontHeight) {
        for (int i = 0; i < TEXTS.getStrHelp().length; i++)
            ToolBox.drawText(g, TEXTS.getStrHelp()[i], fontHeight * 2 * (i + 5), Color.white);
    }

    private void drawHighScoreScreen(Graphics g, int fontHeight) {
        ToolBox.drawText(g, TEXTS.getStrHighscoreList(), fontHeight * 5, Color.white);

        Object[] scores = highScores.getHighScores();
        // only first 8 scores
        for (int i = 0; i + 1 < scores.length && i < 16; i += 2) {
            int y = (int) (fontHeight * (7 + i / 1.5f));
            Color color = (i < 2) ? Color.red : Color.white;
            ToolBox.drawText(g, (String) scores[i], WIDTH / 4, y, color);
            ToolBox.drawText(g, (String) scores[i + 1], 3 * WIDTH / 4, y, color);
        }
    }

    private void drawIngameScreen(Graphics g) {
        g.setColor(Color.white);
        g.drawLine(0, BOTTOM_LINE_POS, WIDTH, BOTTOM_LINE_POS);

        // draw remaining lifes
        g.drawString("" + lives1, 19, HEIGHT - 1);
        for (int i = 0; i < lives1 - 1; i++)
            ToolBox.drawImage(g, player.image, 40 + i * (player.w + 3), 465, 0);

        // draw aliens
        for (int y = 0; y < aliens.length; y++)
            for (int x = 0; x < aliens[y].length; x++) {
                Entity alien = aliens[y][x];
                if (alien.visible) {
                    alien.draw(g);
                    if (frameCtr == 0) {
                        if (alien.frame == FRAMES_PER_IMAGE - 1)
                            alien.visible = false;
                        else
                            alien.frame = 1 - alien.frame;
                    }
                }
            }

        // draw bunkers
        for (int b = 0; b < bunkers.length; b++)
            for (int y = 0; y < 4; y++)
                for (int x = 0; x < 5; x++)
                    if (bunkers[b][y * 5 + x].visible)
                        bunkers[b][y * 5 + x].draw(g);

        // draw player
        if (player.visible)
            player.draw(g);
        if (player.frame != 0 && frameCtr == 0)
            player.frame = 3 - player.frame;

        // draw ufo
        if (ufo.visible)
            ufo.draw(g);

        // draw player shot
        if (playerShot.visible)
            g.fillRect((int) playerShot.x, (int) playerShot.y, playerShot.w, playerShot.h);

        // draw all alien shots
        Entity shot = alienShot;
        while (null != shot) {
            g.fillRect((int) shot.x, (int) shot.y, 2, 8);
            shot = shot.prev;
        }
    }

    private void drawGameOverScreen(Graphics g, int fontHeight) {
        drawIngameScreen(g);

        if (frameCtr < 250)
            ToolBox.drawText(g, TEXTS.getStrGameOver(), (int) (4.5 * fontHeight), Color.red);
    }

    private void drawInputNameScreen(Graphics g, int fontHeight, int charWidth) {
        ToolBox.drawText(g, TEXTS.getStrInputname(), fontHeight * 7, Color.white);

        int strLen = tmpPlayerName.length();
        int x = WIDTH / 2 - charWidth * 4;
        int y = HEIGHT / 2 - fontHeight;
        for (int i = 0; i < 8; i++) {
            g.drawLine(x + charWidth * i, y + 2, x + charWidth * (i + 1) - 2, y + 2); // underlines
            g.drawLine(x + charWidth * i, y + 3, x + charWidth * (i + 1) - 2, y + 3); // underlines

            if (i < strLen)
                g.drawString(tmpPlayerName.substring(i, i + 1), x + charWidth * i, y);
        }

        g.setColor(Color.red);
        g.drawLine(x + charWidth * caretPos, y + 2, x + charWidth * (caretPos + 1) - 2, y + 2); // underlines
        g.drawLine(x + charWidth * caretPos, y + 3, x + charWidth * (caretPos + 1) - 2, y + 3); // underlines
        g.setColor(Color.white);
    }

    private void drawPressEnter(Graphics g, int fontHeight) {
        if (frameCtr < 250) {
            if (panel.hasFocus())
                ToolBox.drawText(g, TEXTS.getStrPressEnter(), HEIGHT - fontHeight / 2, Color.red);
            else
                ToolBox.drawText(g, TEXTS.getStrClickToStart(), HEIGHT - fontHeight / 2, Color.red);
        }
    }

    private void drawClickToContinue(Graphics g, int height) {
        if (frameCtr < 250)
            ToolBox.drawText(g, TEXTS.getStrPaused(), HEIGHT - height / 2, Color.red);
    }

    private void playWalkingSound(long time) {
        int maxAliens = 0;
        for (int i = 0; i < aliens.length; i++)
            maxAliens += aliens[i].length;

        float percent = 1.0f - ((float) alienCtr / (float) maxAliens);
        long should = 1000000000L - (long) (percent * 700000000);

        if (System.nanoTime() - lastSoundTime > should) {
            Sound.play(SOUNDS.WALK1.ordinal() + soundCtr++);
            if (soundCtr > 3)
                soundCtr = 0;
            lastSoundTime = System.nanoTime();
        }
    }

    private void resetGame() {
        lastShotTime = lastSoundTime = System.nanoTime();
        score1 = score2 = 0;
        highscore = highScores.getHighScore();
        splashScreenTimer = 4000000000L;
        soundCtr = 0;
        lives1 = /* lives2 = */LIVES;
        ufoCntDown = 15000 + (2000 - (int) (Math.random() * 4000));
        shot_freq = SPEEDS.getAlienShotFreq();

        // --- ufo ---

        if (ufo == null) {
            ufo = new Entity();
            ufo.setImage(imagens.getUfoImg(), 3);
            ufo.y = UFO_Y_POS;
        }
        ufo.sx = SPEEDS.getUfoSpeed();
        ufo.cntDown = 0;
        ufo.frame = 0;
        ufo.visible = false;

        // --- player ---

        if (player == null) {
            player = new Entity();
            player.setImage(imagens.getPlyrImg(), 3);
            player.y = PLAYER_Y_POS;
        }
        player.sx = SPEEDS.getPlayerSpeed();
        player.cntDown = 0;
        player.frame = 0;
        player.visible = true;
        player.x = WIDTH / 2 - imagens.getPlyrImg().getWidth() / 2;

        // --- player shot ---

        if (playerShot == null) {
            playerShot = new Entity();
            playerShot.w = 2;
            playerShot.h = 8;
        }
        playerShot.x = player.x + player.w / 2 - 1;
        playerShot.y = player.y - 10;

        // --- aliens ---

        int dx = imagens.getE1Img().getWidth() / 3 + 4;

        for (int y = 0; y < aliens.length; y++) {
            BufferedImage image;
            if (y == 0)
                image = imagens.getE3Img();
            else if (y < 3)
                image = imagens.getE2Img();
            else
                image = imagens.getE1Img();

            for (int x = 0; x < aliens[y].length; x++) {
                Entity alien = aliens[y][x];
                if (alien == null) {
                    alien = new Entity();
                    alien.setImage(image, 3);
                    aliens[y][x] = alien;
                }
                alien.x = ALIENS_X_POS + x * dx + (dx / 2 - alien.w / 2);
                alien.y = ALIENS_Y_POS + y * alien.h * 2;
                alien.frame = 0;
                alien.visible = true;
            }
        }

        alienCtr = aliens.length * aliens[0].length;
        alienSX = SPEEDS.getAlienSpeed();

        // --- bunkers ---

        final int BUNKER_X = WIDTH / 2 - (7 * (5 * imagens.getLlBnkImg().getWidth() / 3 + 2)) / 2;

        for (int b = 0; b < bunkers.length; b++)
            for (int y = 0; y < 4; y++)
                for (int x = 0; x < 5; x++) {
                    BufferedImage img = imagens.getMmBnkImg();
                    if (y == 0 && x == 0)
                        img = imagens.getUlBnkImg();
                    else if (y == 0 && x == 4)
                        img = imagens.getUrBnkImg();
                    else if (y == 3 && x == 1)
                        img = imagens.getLlBnkImg();
                    else if (y == 3 && x == 3)
                        img = imagens.getLrBnkImg();

                    Entity e = bunkers[b][y * 5 + x];
                    if (e == null) {
                        e = new Entity();
                        e.setImage(img, 3);
                        bunkers[b][y * 5 + x] = e;
                    }
                    e.x = BUNKER_X + b * 2 * (e.w * 5) + x * e.w;
                    e.y = BUNKERS_Y_POS + y * e.h;
                    e.frame = 0;
                    e.visible = (y == 3 && x == 2) ? false : true;
                }

        // --- ufo ---

        ufo.visible = false;
        Sound.stop(SOUNDS.UFO);

        // --- shots ---

        playerShot.visible = false;

        while (alienShot != null) {
            Entity shot = alienShot;
            alienShot = shot.prev;
            shot.prev = null;
        }
    }



    public void resume() {
        paused = false;
        if (gameState == GameStates.IN_GAME_SCREEN && ufo.visible && ufo.frame == 0)
            Sound.loop(SOUNDS.UFO);
    }

    public void pause() {
        paused = true;
        if (gameState == GameStates.IN_GAME_SCREEN && ufo.visible && ufo.frame == 0)
            Sound.stop(SOUNDS.UFO);
    }

    @Override
    public void stop() {
        Sound.setEnabled(false);
        gameLoopThread = null;
        System.out.println("Game stopped.");
    }

    public Panel getPanel() {
        return panel;
    }

    public void run() {
        Thread t = Thread.currentThread();
        while (t == gameLoopThread) {
            long update = System.nanoTime();

            updateGame(update - lastUpdate);
            paint();

            lastUpdate = update;

            Thread.yield();
            try {
                Thread.sleep(1000 / Game.FRAMES_PER_SECOND);
            } catch (InterruptedException e) {
            }
        }
    }
}
