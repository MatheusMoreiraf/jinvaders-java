package org.opensourcearcade.jinvaders;

import org.opensourcearcade.jinvaders.Sound.SOUNDS;
import org.opensourcearcade.jinvaders.entities.Entity;
import org.opensourcearcade.jinvaders.entities.Player;
import org.opensourcearcade.jinvaders.entities.Ufo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
    public static final Entity[][] ALIENS = new Entity[5][11];
    public static final Entity[][] BUNKERS = new Entity[4][20];
    public static final int LIVES = 3;
    public static final int FRAMES_PER_IMAGE = 3;

    private GameStates gameState = GameStates.SPLASH_SCREEN;

    private static final NumberFormat NUM_FORMAT = new DecimalFormat("000000");

    private HighScores highScores;

    private Graphics2D g2d;

    private Entity playerShot, alienShot;
    private Player player;
    private Ufo ufo;

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

    private final Panel panel;

    private Thread gameLoopThread;
    private long lastUpdate;

    private final Imagens imagens = new Imagens();
    private final Keyboard keyboard = new Keyboard();
    private final Help help = new Help();

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
            Sound.init();
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

    public void updateScreenUtil(GameStates gs, long time) {
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
            gameState = gs;
        }
    }

    private void updateSplashScreen(long time) {
        updateScreenUtil(GameStates.HIGH_SCORE_SCREEN, time);
    }

    private void updateHelpScreen(long time) {
        updateScreenUtil(GameStates.SPLASH_SCREEN, time);
    }

    private void updateHighScoreScreen(long time) {
        updateScreenUtil(GameStates.HELP_SCREEN, time);
        if (!keyboard.isEnterKey()) {
            Object[] scores = highScores.getHighScores();
            highscore = (scores.length > 0) ? Integer.parseInt((String) scores[1]) : 0;
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
                imagens.createNewWaveAliens(imagens);
                // reset alien data
                alienCtr = ALIENS.length * ALIENS[0].length;
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
                playerShot.sy = -(Math.round(10.0f * (float) Speeds.getPlayerShotSpeed() / (float) FRAMES_PER_SECOND)) / 10.0f;
            }
        }

        shootCtr += 1000 / FRAMES_PER_SECOND;
        if (shootCtr > shot_freq) {
            Entity shooter = null;
            while (shooter == null) {
                int x = (int) (Math.random() * ALIENS[0].length);
                for (int y = ALIENS.length - 1; y >= 0; y--)
                    if (ALIENS[y][x].visible) {
                        shooter = ALIENS[y][x];
                        break;
                    }
            }

            shot = new Entity();
            shot.x = shooter.x + shooter.w / 2 - 1;
            shot.y = shooter.y + shooter.h;
            shot.w = 2;
            shot.h = 8;
            shot.sy = (Math.round(10.0f * (float) Speeds.getAlienShotSpeed() / (float) FRAMES_PER_SECOND)) / 10.0f;

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
            player.explosions(player);
        }

        // ufo exploding ?
        if (ufo.visible && ufo.frame != 0) {
            ufo.explosions(ufo, score1);
        }
    }

    private void updateCollisions(long time) {
        for (int y = 0; y < ALIENS.length && gameState == GameStates.IN_GAME_SCREEN; y++) {
            for (int x = 0; x < ALIENS[y].length && gameState == GameStates.IN_GAME_SCREEN; x++) {
                Entity alien = ALIENS[y][x];
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
                        imagens.updateBunkers(alien);
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
            imagens.collisionBunkers(shot, playerShot);

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
        ufo.updatePosition(ufo, time, ufoCntDown);

        // --- aliens ---
        float alienDelta = 2 * alienSX * (time / 1000000000.0f); // pixels
        // to
        // move
        alienDelta /= (float) (alienCtr + 4); // speed modifier for killed
        // aliens

        boolean bounce = checkAlienBounce(alienDelta);
        if (!bounce) // not bouncing, move aliens sidewards
        {
            float alienMaxY = 0, alienY = 0;

            help.alinesMovement(alienDelta, alienY, alienMaxY, true);
        } else // bouncing, move aliens downwards
        {
            float alienMaxY = 0, alienY = 0;
            alienSX = (bounce) ? -alienSX : alienSX;

            help.alinesMovement(10, alienY, alienMaxY, false);

            // aliens hit ground ?
            if (alienMaxY >= Pos.BOTTOM_LINE_POS - 1) {
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
            alienToCheck = ALIENS[ALIENS.length - 1][help.getMostColumn("Right")];
        else
            alienToCheck = ALIENS[ALIENS.length - 1][help.getMostColumn("Left")];

        // check if updated position bounces screen
        float newXpos = alienToCheck.x + alienDelta;

        return ((int) newXpos < 0 || (int) newXpos > WIDTH - alienToCheck.w);
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
        ToolBox.drawText(g2d, Texts.getStrHiscore(), names_height, Color.white);
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
            g2d.drawImage(imagens.getSndOffImg(), WIDTH - imagens.getSndOffImg().getWidth() - 1, Pos.BOTTOM_LINE_POS + 2, null);

        panel.getGraphics().drawImage(imagens.getBackbuffer(), 0, 0, null);
    }

    private void drawSplashScreen(Graphics g, int height) {
        if (frameCtr < 250) {
            ToolBox.drawText(g, Texts.getPlayInvaders()[0], 6 * height, Color.white);
            ToolBox.drawText(g, Texts.getPlayInvaders()[1], 8 * height, Color.white);
            ToolBox.drawText(g, Texts.getPlayInvaders()[2], 10 * height, Color.white);
        }

        final int X = 125;

        ToolBox.drawText(g, Texts.getSplashScoreTable()[0], WIDTH / 2, 12 * height, Color.white);

        ToolBox.drawText(g, Texts.getSplashScoreTable()[1], WIDTH / 2, 14 * height, Color.white);
        ToolBox.drawImageCentered(g, imagens.getUfoImg(), X, 13 * height, 0);

        ToolBox.drawText(g, Texts.getSplashScoreTable()[2], WIDTH / 2, (int) (15.5 * height), Color.white);
        ToolBox.drawImageCentered(g, imagens.getE3Img(), X, (int) (14.5 * height), 0);

        ToolBox.drawText(g, Texts.getSplashScoreTable()[3], WIDTH / 2, 17 * height, Color.white);
        ToolBox.drawImageCentered(g, imagens.getE2Img(), X, 16 * height, 0);

        ToolBox.drawText(g, Texts.getSplashScoreTable()[4], WIDTH / 2, (int) (18.5 * height), Color.green);
        ToolBox.drawImageCentered(g, imagens.getE1Img(), X, (int) (17.5 * height), 0);
    }

    private void drawHelpScreen(Graphics g, int fontHeight) {
        for (int i = 0; i < Texts.getStrHelp().length; i++)
            ToolBox.drawText(g, Texts.getStrHelp()[i], fontHeight * 2 * (i + 5), Color.white);
    }

    private void drawHighScoreScreen(Graphics g, int fontHeight) {
        ToolBox.drawText(g, Texts.getStrHighscoreList(), fontHeight * 5, Color.white);

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
        g.drawLine(0, Pos.BOTTOM_LINE_POS, WIDTH, Pos.BOTTOM_LINE_POS);

        // draw remaining lifes
        g.drawString("" + lives1, 19, HEIGHT - 1);
        for (int i = 0; i < lives1 - 1; i++)
            ToolBox.drawImage(g, player.image, 40 + i * (player.w + 3), 465, 0);

        // draw aliens
        imagens.drawAliens(frameCtr, g);

        // draw BUNKERS
        imagens.drawBunkers(g);

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
            ToolBox.drawText(g, Texts.getStrGameOver(), (int) (4.5 * fontHeight), Color.red);
    }

    private void drawInputNameScreen(Graphics g, int fontHeight, int charWidth) {
        ToolBox.drawText(g, Texts.getStrInputname(), fontHeight * 7, Color.white);

        int strLen = tmpPlayerName.length();

        Help.inputName(WIDTH, HEIGHT, charWidth, strLen, fontHeight, tmpPlayerName, g, caretPos);
    }

    private void drawPressEnter(Graphics g, int fontHeight) {
        if (frameCtr < 250) {
            if (panel.hasFocus())
                ToolBox.drawText(g, Texts.getStrPressEnter(), HEIGHT - fontHeight / 2, Color.red);
            else
                ToolBox.drawText(g, Texts.getStrClickToStart(), HEIGHT - fontHeight / 2, Color.red);
        }
    }

    private void drawClickToContinue(Graphics g, int height) {
        if (frameCtr < 250)
            ToolBox.drawText(g, Texts.getStrPaused(), HEIGHT - height / 2, Color.red);
    }

    private void playWalkingSound(long time) {
        int maxAliens = 0;
        for (int i = 0; i < ALIENS.length; i++)
            maxAliens += ALIENS[i].length;

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
        shot_freq = Speeds.getAlienShotFreq();

        // --- ufo ---

        if (ufo == null) {
            ufo = new Ufo();
            ufo.setImage(imagens.getUfoImg(), 3);
            ufo.y = Pos.UFO_Y_POS;
        }
        ufo.sx = Speeds.getUfoSpeed();
        ufo.cntDown = 0;
        ufo.frame = 0;
        ufo.visible = false;

        // --- player ---

        if (player == null) {
            player = new Player();
            player.setImage(imagens.getPlyrImg(), 3);
            player.y = Pos.PLAYER_Y_POS;
        }
        player.sx = Speeds.getPlayerSpeed();
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

        // --- Aliens ---
        imagens.resetAliens(imagens);

        alienCtr = ALIENS.length * ALIENS[0].length;
        alienSX = Speeds.getAlienSpeed();

        // --- Bunkers ---
        imagens.resetBunkers(imagens);

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
