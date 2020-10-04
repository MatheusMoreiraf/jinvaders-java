package org.opensourcearcade.jinvaders;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.opensourcearcade.jinvaders.Sound.SOUNDS;

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

	public static enum GameStates {
		SPLASH_SCREEN, HELP_SCREEN, HIGH_SCORE_SCREEN, GAME_OVER_SCREEN, IN_GAME_SCREEN, INPUT_NAME_SCREEN
	};

	private GameStates gameState = GameStates.SPLASH_SCREEN;

	private static final int LIVES = 3;
	private static final int FRAMES_PER_IMAGE = 3;

	// speed in pixels per sec
	private static final int PLAYER_SPEED = 200;
	private static final int PLAYER_SHOT_SPEED = 300;
	private static final int ALIEN_SPEED = 400;
	private static final int UFO_SPEED = 75;
	private static final int ALIEN_SHOT_SPEED = 150;
	private static final int ALIEN_SHOT_FREQ = 1000;

	private static final int BOTTOM_LINE_POS = 462;
	private static final int PLAYER_Y_POS = 416;
	private static final int UFO_Y_POS = 75;
	private static final int ALIENS_X_POS = 68;
	private static final int ALIENS_Y_POS = 112;
	private static final int BUNKERS_Y_POS = 368;

	private static final String STR_GAME_OVER = "GAME OVER";
	private static final String STR_CLICK_TO_START = "CLICK HERE TO BEGIN";
	private static final String STR_PRESS_ENTER = "PRESS ENTER TO PLAY";
	private static final String STR_HISCORE = "HISCORE";
	private static final String STR_HIGHSCORE_LIST = "HIGHSCORES";
	private static final String STR_INPUTNAME = "INPUT YOUR NAME";
	private static final String STR_PAUSED = "PAUSED, CLICK HERE";
	private static final String[] STR_HELP = { "ENTER = START GAME", "SPACE = FIRE", "MOVE = CURSOR LEFT/RIGHT", "ESC = LEAVE GAME" };
	private static final String[] SPLASH_SCORE_TABLE = { "*SCORE ADVANCE TABLE*", "=? MYSTERY", "=30 POINTS", "=20 POINTS", "=10 POINTS" };
	private static final String[] PLAY_INVADERS = { "PLAY", "JINVADERS", "V"+VERSION };

	private static final NumberFormat NUM_FORMAT = new DecimalFormat("000000");

	private HighScores highScores;

	private BufferedImage backbuffer;
	private Graphics2D g2d;

	private BufferedImage plyrImg, e1Img, e2Img, e3Img, ufoImg, llBnkImg, lrBnkImg, ulBnkImg, urBnkImg, mmBnkImg, sndOffImg;

	private Entity player, ufo, playerShot, alienShot;
	private Entity[][] aliens = new Entity[5][11];
	private Entity[][] bunkers = new Entity[4][20];

	private boolean paused;
	private boolean leftKey, rightKey, spaceKey, escKey, enterKey, backKey;
	private boolean spaceKeyReleased = true;

	private String playerName1, playerName2, tmpPlayerName;
	private int caretPos;

	private int score1, score2, highscore, lives1; // , lives2;
	private int alienCtr, soundCtr, ufoCntDown;

	private float alienSX;

	private long frameCtr, shootCtr, splashScreenTimer;
	private long lastShotTime, lastSoundTime;
	private long shot_freq; // per nanos

	private Font font;

	private int lastKey;

	private Panel panel;

	private Thread gameLoopThread;
	private long lastUpdate;

	public static void main(String[] args) {

		try {

			// fix the JNLP desktop icon exec rights bug (Linux only)
			if(System.getProperty("os.name").toLowerCase().indexOf("linux")!=-1)
			{
		        java.io.File desktop = new java.io.File(System.getProperty("user.home") + "/Desktop");
		        if(desktop.exists())
		        {
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
			String name=ToolBox.getPackageName();
			Image iconImg=ToolBox.loadImage(ToolBox.getURL(name+".png"));

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
		}
		catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	public Game() {
		System.out.println(System.getProperty("java.vm.name")+System.getProperty("java.vm.version"));
		System.out.println(ToolBox.getPackageName()+" v"+VERSION);

		panel = new Panel();
		panel.setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));
		panel.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				keyEvent(event, true);
			}

			public void keyReleased(KeyEvent event) {
				keyEvent(event, false);
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

		backbuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}

	@Override
	public void init() {

		setSize(WIDTH, HEIGHT);

		boolean isApplet=(null != System.getSecurityManager());
		highScores = isApplet?new AppletHighScores(): new ApplicationHighScores();

		backbuffer = ToolBox.convertToCompatibleImage(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB));
		g2d = backbuffer.createGraphics();
		Color cordeFundo = new Color(28,28,28);
		g2d.setBackground(cordeFundo);

		playerName1 = "PLAYER1";
		playerName2 = "PLAYER2";
		tmpPlayerName = playerName1;
		caretPos = playerName1.length();

		try {
			plyrImg = ToolBox.loadImage(ToolBox.getURL("player.png"));
			e1Img = ToolBox.loadImage(ToolBox.getURL("e1.png"));
			e2Img = ToolBox.loadImage(ToolBox.getURL("e2.png"));
			e3Img = ToolBox.loadImage(ToolBox.getURL("e3.png"));
			llBnkImg = ToolBox.loadImage(ToolBox.getURL("ll.png"));
			lrBnkImg = ToolBox.loadImage(ToolBox.getURL("lr.png"));
			ulBnkImg = ToolBox.loadImage(ToolBox.getURL("ul.png"));
			urBnkImg = ToolBox.loadImage(ToolBox.getURL("ur.png"));
			mmBnkImg = ToolBox.loadImage(ToolBox.getURL("mm.png"));
			ufoImg = ToolBox.loadImage(ToolBox.getURL("ufo.png"));
			sndOffImg = ToolBox.loadImage(ToolBox.getURL("sndOff.png"));

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
		}
		catch (Exception e) {
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
			if (enterKey) {
				enterKey = false;
				if (score1>0)
					gameState = GameStates.INPUT_NAME_SCREEN;
				else
					gameState = GameStates.SPLASH_SCREEN;
			}
			break;
		default:
			break;
		}

		// state-independent updates

		if (lastKey==KeyEvent.VK_S) {
			lastKey = 0;
			boolean enabled = !Sound.isEnabled();
			Sound.setEnabled(enabled);
			if (enabled&&ufo.visible&&ufo.frame==0)
				Sound.loop(SOUNDS.UFO);
		}
	}

	private void updateSplashScreen(long time) {
		if (enterKey) {
			enterKey = false;
			resetGame();
			gameState = GameStates.IN_GAME_SCREEN;
			return;
		}

		splashScreenTimer -= time;
		if (splashScreenTimer<=0) {
			highscore = highScores.getHighScore();
			splashScreenTimer = 5000000000L;
			gameState = GameStates.HIGH_SCORE_SCREEN;
		}
	}

	private void updateHelpScreen(long time) {
		if (enterKey) {
			enterKey = false;
			resetGame();
			gameState = GameStates.IN_GAME_SCREEN;
			return;
		}

		splashScreenTimer -= time;
		if (splashScreenTimer<=0) {
			highscore = highScores.getHighScore();
			splashScreenTimer = 5000000000L;
			gameState = GameStates.SPLASH_SCREEN;
		}
	}

	private void updateHighScoreScreen(long time) {
		if (enterKey) {
			enterKey = false;
			resetGame();
			gameState = GameStates.IN_GAME_SCREEN;
			return;
		}

		Object[] scores = highScores.getHighScores();
		highscore = (scores.length>0) ? Integer.parseInt((String) scores[1]) : 0;

		splashScreenTimer -= time;
		if (splashScreenTimer<=0) {
			splashScreenTimer = 4000000000L;
			gameState = GameStates.HELP_SCREEN;
		}
	}

	private void updateInGameScreen(long time) {
		if (escKey) {
			escKey = false;
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
			if (alienCtr==0) {
				// create new wave
				int dx = e1Img.getWidth()/3+4;
				for (int y = 0; y<aliens.length; y++)
					for (int x = 0; x<aliens[y].length; x++) {
						Entity alien = aliens[y][x];
						alien.x = ALIENS_X_POS+x*dx+(dx/2-alien.w/2);
						alien.y = ALIENS_Y_POS+y*alien.h*2;
						alien.frame = 0;
						alien.visible = true;
					}

				// reset alien data
				alienCtr = aliens.length*aliens[0].length;
				--alienSX;
				shot_freq = (long) (0.9f*(float) shot_freq);
			}
		}
	}

	private void updateInputNameScreen(long time) {
		if (enterKey) {
			enterKey = false;
			if (tmpPlayerName.isEmpty())
				tmpPlayerName = playerName1;

			playerName1 = tmpPlayerName;
			caretPos = playerName1.length();
			highScores.postHighScore(playerName1, score1);

			gameState = GameStates.HIGH_SCORE_SCREEN;
		}
		else if (backKey) {
			if (caretPos>0) {
				caretPos--;
				if (caretPos>0)
					tmpPlayerName = tmpPlayerName.substring(0, caretPos);
				else
					tmpPlayerName = "";
			}
			backKey = false;
		}
		else if (lastKey!=0) {
			if (caretPos<8) {
				int strLen = tmpPlayerName.length();
				String s1 = (caretPos>0) ? tmpPlayerName.substring(0, caretPos) : "";
				String s2 = (caretPos<strLen) ? tmpPlayerName.substring(caretPos+1, strLen-1) : "";
				tmpPlayerName = s1+KeyEvent.getKeyText(lastKey)+s2;
				caretPos++;
			}
			lastKey = 0;
		}
	}

	private void updateShooting(long time) {

		Entity shot;
		if (spaceKey&&spaceKeyReleased&&!playerShot.visible&&player.frame==0) {
			spaceKeyReleased = false;
			long t = System.nanoTime();
			if (t-lastShotTime>300000) {
				lastShotTime = t;
				Sound.play(SOUNDS.SHOT);
				playerShot = new Entity();
				playerShot.x = player.x+player.w/2-1;
				playerShot.y = player.y-10;
				playerShot.w = 2;
				playerShot.h = 8;
				playerShot.sy = -(Math.round(10.0f*(float) PLAYER_SHOT_SPEED/(float) FRAMES_PER_SECOND))/10.0f;
			}
		}

		shootCtr += 1000/FRAMES_PER_SECOND;
		if (shootCtr>shot_freq) {
			Entity shooter = null;
			while (shooter==null) {
				int x = (int) (Math.random()*aliens[0].length);
				for (int y = aliens.length-1; y>=0; y--)
					if (aliens[y][x].visible) {
						shooter = aliens[y][x];
						break;
					}
			}

			shot = new Entity();
			shot.x = shooter.x+shooter.w/2-1;
			shot.y = shooter.y+shooter.h;
			shot.w = 2;
			shot.h = 8;
			shot.sy = (Math.round(10.0f*(float) ALIEN_SHOT_SPEED/(float) FRAMES_PER_SECOND))/10.0f;

			if (alienShot==null)
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
		if (player.frame!=0) {
			player.cntDown -= 1000/FRAMES_PER_SECOND;
			if (player.cntDown<0) {
				player.cntDown = 0;
				player.frame = 0;
			}
		}

		// ufo exploding ?
		if (ufo.visible&&ufo.frame!=0) {
			ufo.cntDown -= 1000/FRAMES_PER_SECOND;
			if (ufo.cntDown<0) {
				if (ufo.frame==1) {
					ufo.cntDown = 1000;
					ufo.frame = 2;
				}
				else {
					ufo.visible = false;
					ufo.frame = 0;
					score1 += (int) (Math.random()*10)*100;
				}
			}
		}
	}

	private void updateCollisions(long time) {
		for (int y = 0; y<aliens.length&&gameState==GameStates.IN_GAME_SCREEN; y++)
			for (int x = 0; x<aliens[y].length&&gameState==GameStates.IN_GAME_SCREEN; x++) {
				Entity alien = aliens[y][x];
				if (alien.visible&&alien.frame<2) {
					// alien ./. playershot
					if (playerShot.visible&&ToolBox.checkCollision(playerShot, alien)) {
						Sound.play(SOUNDS.INV_HIT);
						--alienCtr;
						alien.frame = FRAMES_PER_IMAGE-1;
						playerShot.visible = false;

						if (alien.image==e1Img)
							score1 += 10;
						else if (alien.image==e2Img)
							score1 += 20;
						else
							score1 += 30;
					}
					// player ./. alien
					else if (player.frame==0&&player.visible&&ToolBox.checkCollision(player, alien)) {
						Sound.play(SOUNDS.PLY_HIT);
						Sound.play(SOUNDS.INV_HIT);
						--alienCtr;
						alien.frame = FRAMES_PER_IMAGE-1;

						player.dx = 0;
						player.frame = 1;
						player.cntDown = 2000;
						if (--lives1==0) {
							gameState = GameStates.GAME_OVER_SCREEN;
							if (ufo.visible) {
								Sound.stop(SOUNDS.UFO);
								ufo.visible = false;
							}
						}
						continue;
					}
					else {
						// alien ./. bunker
						for (int b = 0; b<bunkers.length; b++)
							for (int yy = 0; yy<4; yy++)
								for (int xx = 0; xx<5; xx++) {
									Entity bk = bunkers[b][yy*5+xx];
									if (bk.visible&&ToolBox.checkCollision(alien, bk)) {
										bk.visible = false;
										Sound.play(SOUNDS.BASE_HIT);
									}
								}
					}
				}
			}

		// ufo ./. playershot
		if (ufo.frame<2&&ufo.visible&&playerShot.visible&&ToolBox.checkCollision(playerShot, ufo)) {
			ufo.frame = 1;
			ufo.cntDown = 1000;
			playerShot.visible = false;
			Sound.stop(SOUNDS.UFO);
			Sound.play(SOUNDS.UFO_HIT);
		}

		Entity shot = alienShot;
		while (shot!=null) {
			// alienShot ./. player
			if (player.frame==0&&shot.visible&&ToolBox.checkCollision(shot, player)) {
				shot.y = HEIGHT+shot.h;

				Sound.play(SOUNDS.PLY_HIT);
				player.dx = 0;
				player.frame = 1;
				player.cntDown = 2000;
				if (--lives1==0) {
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
			for (int b = 0; b<bunkers.length&&!bnkHit; b++)
				for (int yy = 0; yy<4&&!bnkHit; yy++)
					for (int xx = 0; xx<5&&!bnkHit; xx++) {
						Entity bnk = bunkers[b][yy*5+xx];
						if (bnk.visible) {
							// alienShot ./. bunker
							if (shot.visible&&ToolBox.checkCollision(shot, bnk)) {
								shot.y = HEIGHT+shot.h;
								++bnk.frame;
								if (bnk.frame>2)
									bnk.visible = false;
								bnkHit = true;
							}
							// playerShot ./. bunker
							else if (playerShot.visible&&ToolBox.checkCollision(playerShot, bnk)) {
								playerShot.visible = false;
								++bnk.frame;
								if (bnk.frame>2)
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
		if (player.frame==0) {
			float delta = player.sx*(time/1000000000.0f);
			if (leftKey)
				player.dx = -delta;
			else if (rightKey)
				player.dx = delta;
			else
				player.dx = 0;
		}

		player.x += player.dx;
		if (player.x<0||player.x>WIDTH-player.w)
			player.x -= player.dx;

		// --- ufo ---
		if (ufo.visible) {
			if (ufo.frame==0) {
				float delta = ufo.sx*(time/1000000000.0f);
				if (ufo.x>WIDTH) {
					ufo.visible = false;
					Sound.stop(SOUNDS.UFO);
				}
				else
					ufo.x += delta;
			}
		}
		else {
			ufoCntDown -= 1000/FRAMES_PER_SECOND;
			if (ufoCntDown<0) {
				ufo.x = -ufo.image.getWidth(null);
				ufo.visible = true;
				ufoCntDown = 15000+(2000-(int) (Math.random()*4000));
				Sound.loop(SOUNDS.UFO);
			}
		}

		// --- aliens ---
		float alienDelta = 2*(float) alienSX*(time/1000000000.0f); // pixels
		// to
		// move
		alienDelta /= (float) (alienCtr+4); // speed modifier for killed
		// aliens

		boolean bounce = checkAlienBounce(alienDelta);
		if (!bounce) // not bouncing, move aliens sidewards
		{
			float alienMaxY = 0, alienY = 0;
			for (int y = 0; y<aliens.length; y++)
				for (int x = 0; x<aliens[y].length; x++) {
					Entity alien = aliens[y][x]; // do *not* check for
					// visibility (bouncing
					// test!)
					alien.x += alienDelta;
					alienY = alien.y+alien.image.getHeight(null);
					alienMaxY = (alienY>alienMaxY) ? alienY : alienMaxY;
				}
		}
		else // bouncing, move aliens downwards
		{
			float alienMaxY = 0, alienY = 0;
			alienSX = (bounce) ? -alienSX : alienSX;

			for (int y = 0; y<aliens.length; y++)
				for (int x = 0; x<aliens[y].length; x++) {
					Entity alien = aliens[y][x]; // do *not* check for
					// visibility (bouncing
					// test!)
					alien.y += 10;
					alienY = alien.y+alien.image.getHeight(null);
					alienMaxY = (alienY>alienMaxY) ? alienY : alienMaxY;
				}

			// aliens hit ground ?
			if (alienMaxY>=BOTTOM_LINE_POS-1) {
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

		if (playerShot!=null) {
			playerShot.y += playerShot.sy;
			if (playerShot.y<-10)
				playerShot.visible = false;
		}

		Entity shot = alienShot;
		while (null!=shot) {
			shot.y += shot.sy;
			if (shot.prev!=null&&shot.prev.y>HEIGHT)
				shot.prev = null;
			shot = shot.prev;
		}
	}

	private boolean checkAlienBounce(float alienDelta) {
		// get most right (or most left) alien for bouncing check
		// (broadest alien is always in last alien row)
		Entity alienToCheck = null;
		if (alienSX>0)
			alienToCheck = aliens[aliens.length-1][getMostRightColumn()];
		else
			alienToCheck = aliens[aliens.length-1][getMostLeftColumn()];

		// check if updated position bounces screen
		float newXpos = alienToCheck.x+alienDelta;

		return ((int) newXpos<0||(int) newXpos>WIDTH-alienToCheck.w);
	}

	private int getMostLeftColumn() {
		// get the most left alien (which is alive)
		int column = Integer.MAX_VALUE;
		for (int y = 0; y<aliens.length; y++)
			for (int x = 0; x<aliens[y].length; x++)
				if (aliens[y][x].visible)
					column = Math.min(column, x);
		return column;
	}

	private int getMostRightColumn() {
		// get the most right alien (which is alive)
		int column = Integer.MIN_VALUE;
		for (int y = 0; y<aliens.length; y++)
			for (int x = 0; x<aliens[y].length; x++)
				if (aliens[y][x].visible)
					column = Math.max(column, x);
		return column;
	}

	public void paint() {
		frameCtr += 1000/FRAMES_PER_SECOND;
		if (frameCtr>300)
			frameCtr = 0;

		g2d.setColor(Color.black);
		g2d.clearRect(0, 0, WIDTH, HEIGHT);

		g2d.setColor(Color.white);
		g2d.setFont(font.deriveFont(20f));

		final FontMetrics fm = g2d.getFontMetrics(g2d.getFont());

		int fontHeight = fm.getHeight();

		int names_height = (int) (fontHeight*1.5);
		ToolBox.drawText(g2d, playerName1, WIDTH/6, names_height, Color.white);
		ToolBox.drawText(g2d, STR_HISCORE, names_height, Color.white);
		ToolBox.drawText(g2d, playerName2, WIDTH/6*5, names_height, Color.white);

		int score_height = fontHeight*3;
		ToolBox.drawText(g2d, NUM_FORMAT.format(score1), WIDTH/6, score_height, Color.white);
		ToolBox.drawText(g2d, NUM_FORMAT.format(highscore), score_height, Color.white);
		ToolBox.drawText(g2d, NUM_FORMAT.format(score2), WIDTH/6*5, score_height, Color.white);

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
			drawInputNameScreen(g2d, fontHeight, fm.stringWidth("M")+1);
			break;
		}

		if (paused)
			drawClickToContinue(g2d, names_height);
		else if (gameState!=GameStates.IN_GAME_SCREEN)
			drawPressEnter(g2d, names_height);

		if (!Sound.isEnabled())
			g2d.drawImage(sndOffImg, WIDTH-sndOffImg.getWidth()-1, BOTTOM_LINE_POS+2, null);

		panel.getGraphics().drawImage(backbuffer, 0, 0, null);
	}

	private void drawSplashScreen(Graphics g, int height) {
		if (frameCtr<250) {
			ToolBox.drawText(g, PLAY_INVADERS[0], 6*height, Color.white);
			ToolBox.drawText(g, PLAY_INVADERS[1], 8*height, Color.white);
			ToolBox.drawText(g, PLAY_INVADERS[2], 10*height, Color.white);
		}

		final int X = 125;

		ToolBox.drawText(g, SPLASH_SCORE_TABLE[0], WIDTH/2, 12*height, Color.white);

		ToolBox.drawText(g, SPLASH_SCORE_TABLE[1], WIDTH/2, (int) (14*height), Color.white);
		ToolBox.drawImageCentered(g, ufoImg, X, 13*height, 0);

		ToolBox.drawText(g, SPLASH_SCORE_TABLE[2], WIDTH/2, (int) (15.5*height), Color.white);
		ToolBox.drawImageCentered(g, e3Img, X, (int) (14.5*height), 0);

		ToolBox.drawText(g, SPLASH_SCORE_TABLE[3], WIDTH/2, (int) (17*height), Color.white);
		ToolBox.drawImageCentered(g, e2Img, X, 16*height, 0);

		ToolBox.drawText(g, SPLASH_SCORE_TABLE[4], WIDTH/2, (int) (18.5*height), Color.green);
		ToolBox.drawImageCentered(g, e1Img, X, (int) (17.5*height), 0);
	}

	private void drawHelpScreen(Graphics g, int fontHeight) {
		for (int i = 0; i<STR_HELP.length; i++)
			ToolBox.drawText(g, STR_HELP[i], fontHeight*2*(i+5), Color.white);
	}

	private void drawHighScoreScreen(Graphics g, int fontHeight) {
		ToolBox.drawText(g, STR_HIGHSCORE_LIST, fontHeight*5, Color.white);

		Object[] scores = highScores.getHighScores();
		// only first 8 scores
		for (int i = 0; i+1<scores.length&&i<16; i += 2) {
			int y = (int) (fontHeight*(7+i/1.5f));
			Color color = (i<2) ? Color.red : Color.white;
			ToolBox.drawText(g, (String) scores[i], WIDTH/4, y, color);
			ToolBox.drawText(g, (String) scores[i+1], 3*WIDTH/4, y, color);
		}
	}

	private void drawIngameScreen(Graphics g) {
		g.setColor(Color.white);
		g.drawLine(0, BOTTOM_LINE_POS, WIDTH, BOTTOM_LINE_POS);

		// draw remaining lifes
		g.drawString(""+lives1, 19, HEIGHT-1);
		for (int i = 0; i<lives1-1; i++)
			ToolBox.drawImage(g, player.image, 40+i*(player.w+3), 465, 0);

		// draw aliens
		for (int y = 0; y<aliens.length; y++)
			for (int x = 0; x<aliens[y].length; x++) {
				Entity alien = aliens[y][x];
				if (alien.visible) {
					alien.draw(g);
					if (frameCtr==0) {
						if (alien.frame==FRAMES_PER_IMAGE-1)
							alien.visible = false;
						else
							alien.frame = 1-alien.frame;
					}
				}
			}

		// draw bunkers
		for (int b = 0; b<bunkers.length; b++)
			for (int y = 0; y<4; y++)
				for (int x = 0; x<5; x++)
					if (bunkers[b][y*5+x].visible)
						bunkers[b][y*5+x].draw(g);

		// draw player
		if (player.visible)
			player.draw(g);
		if (player.frame!=0&&frameCtr==0)
			player.frame = 3-player.frame;

		// draw ufo
		if (ufo.visible)
			ufo.draw(g);

		// draw player shot
		if (playerShot.visible)
			g.fillRect((int) playerShot.x, (int) playerShot.y, playerShot.w, playerShot.h);

		// draw all alien shots
		Entity shot = alienShot;
		while (null!=shot) {
			g.fillRect((int) shot.x, (int) shot.y, 2, 8);
			shot = shot.prev;
		}
	}

	private void drawGameOverScreen(Graphics g, int fontHeight) {
		drawIngameScreen(g);

		if (frameCtr<250)
			ToolBox.drawText(g, STR_GAME_OVER, (int) (4.5*fontHeight), Color.red);
	}

	private void drawInputNameScreen(Graphics g, int fontHeight, int charWidth) {
		ToolBox.drawText(g, STR_INPUTNAME, fontHeight*7, Color.white);

		int strLen = tmpPlayerName.length();
		int x = WIDTH/2-charWidth*4;
		int y = HEIGHT/2-fontHeight;
		for (int i = 0; i<8; i++) {
			g.drawLine(x+charWidth*i, y+2, x+charWidth*(i+1)-2, y+2); // underlines
			g.drawLine(x+charWidth*i, y+3, x+charWidth*(i+1)-2, y+3); // underlines

			if (i<strLen)
				g.drawString(tmpPlayerName.substring(i, i+1), x+charWidth*i, y);
		}

		g.setColor(Color.red);
		g.drawLine(x+charWidth*caretPos, y+2, x+charWidth*(caretPos+1)-2, y+2); // underlines
		g.drawLine(x+charWidth*caretPos, y+3, x+charWidth*(caretPos+1)-2, y+3); // underlines
		g.setColor(Color.white);
	}

	private void drawPressEnter(Graphics g, int fontHeight) {
		if (frameCtr<250) {
			if (panel.hasFocus())
				ToolBox.drawText(g, STR_PRESS_ENTER, HEIGHT-fontHeight/2, Color.red);
			else
				ToolBox.drawText(g, STR_CLICK_TO_START, HEIGHT-fontHeight/2, Color.red);
		}
	}

	private void drawClickToContinue(Graphics g, int height) {
		if (frameCtr<250)
			ToolBox.drawText(g, STR_PAUSED, HEIGHT-height/2, Color.red);
	}

	private void playWalkingSound(long time) {
		int maxAliens = 0;
		for (int i = 0; i<aliens.length; i++)
			maxAliens += aliens[i].length;

		float percent = 1.0f-((float) alienCtr/(float) maxAliens);
		long should = 1000000000L-(long) (percent*700000000);

		if (System.nanoTime()-lastSoundTime>should) {
			Sound.play(SOUNDS.WALK1.ordinal()+soundCtr++);
			if (soundCtr>3)
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
		ufoCntDown = 15000+(2000-(int) (Math.random()*4000));
		shot_freq = ALIEN_SHOT_FREQ;

		// --- ufo ---

		if (ufo==null) {
			ufo = new Entity();
			ufo.setImage(ufoImg, 3);
			ufo.y = UFO_Y_POS;
		}
		ufo.sx = UFO_SPEED;
		ufo.cntDown = 0;
		ufo.frame = 0;
		ufo.visible = false;

		// --- player ---

		if (player==null) {
			player = new Entity();
			player.setImage(plyrImg, 3);
			player.y = PLAYER_Y_POS;
		}
		player.sx = PLAYER_SPEED;
		player.cntDown = 0;
		player.frame = 0;
		player.visible = true;
		player.x = WIDTH/2-plyrImg.getWidth()/2;

		// --- player shot ---

		if (playerShot==null) {
			playerShot = new Entity();
			playerShot.w = 2;
			playerShot.h = 8;
		}
		playerShot.x = player.x+player.w/2-1;
		playerShot.y = player.y-10;

		// --- aliens ---

		int dx = e1Img.getWidth()/3+4;

		for (int y = 0; y<aliens.length; y++) {
			BufferedImage image;
			if (y==0)
				image = e3Img;
			else if (y<3)
				image = e2Img;
			else
				image = e1Img;

			for (int x = 0; x<aliens[y].length; x++) {
				Entity alien = aliens[y][x];
				if (alien==null) {
					alien = new Entity();
					alien.setImage(image, 3);
					aliens[y][x] = alien;
				}
				alien.x = ALIENS_X_POS+x*dx+(dx/2-alien.w/2);
				alien.y = ALIENS_Y_POS+y*alien.h*2;
				alien.frame = 0;
				alien.visible = true;
			}
		}

		alienCtr = aliens.length*aliens[0].length;
		alienSX = ALIEN_SPEED;

		// --- bunkers ---

		final int BUNKER_X = WIDTH/2-(7*(5*llBnkImg.getWidth()/3+2))/2;

		for (int b = 0; b<bunkers.length; b++)
			for (int y = 0; y<4; y++)
				for (int x = 0; x<5; x++) {
					BufferedImage img = mmBnkImg;
					if (y==0&&x==0)
						img = ulBnkImg;
					else if (y==0&&x==4)
						img = urBnkImg;
					else if (y==3&&x==1)
						img = llBnkImg;
					else if (y==3&&x==3)
						img = lrBnkImg;

					Entity e = bunkers[b][y*5+x];
					if (e==null) {
						e = new Entity();
						e.setImage(img, 3);
						bunkers[b][y*5+x] = e;
					}
					e.x = BUNKER_X+b*2*(e.w*5)+x*e.w;
					e.y = BUNKERS_Y_POS+y*e.h;
					e.frame = 0;
					e.visible = (y==3&&x==2) ? false : true;
				}

		// --- ufo ---

		ufo.visible = false;
		Sound.stop(SOUNDS.UFO);

		// --- shots ---

		playerShot.visible = false;

		while (alienShot!=null) {
			Entity shot = alienShot;
			alienShot = shot.prev;
			shot.prev = null;
		}
	}

	public void keyEvent(KeyEvent event, boolean pressed) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.VK_LEFT:
			leftKey = pressed;
			break;
		case KeyEvent.VK_RIGHT:
			rightKey = pressed;
			break;
		case KeyEvent.VK_SPACE:
			spaceKey = pressed;
			if (!pressed)
				spaceKeyReleased = true;
			break;
		case KeyEvent.VK_ESCAPE:
			escKey = pressed;
			break;
		case KeyEvent.VK_ENTER:
			enterKey = pressed;
			break;
		case KeyEvent.VK_BACK_SPACE:
			backKey = pressed;
			break;
		default:
			char c = event.getKeyChar();
			if (pressed&&lastKey!=keyCode)
				if ((c>47&&c<91)||(c>96&&c<123))
					lastKey = keyCode;
			break;
		}
	}

	public void resume() {
		paused = false;
		if (gameState==GameStates.IN_GAME_SCREEN&&ufo.visible&&ufo.frame==0)
			Sound.loop(SOUNDS.UFO);
	}

	public void pause() {
		paused = true;
		if (gameState==GameStates.IN_GAME_SCREEN&&ufo.visible&&ufo.frame==0)
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
		while (t==gameLoopThread) {
			long update = System.nanoTime();

			updateGame(update-lastUpdate);
			paint();

			lastUpdate = update;

			Thread.yield();
			try {
				Thread.sleep(1000/Game.FRAMES_PER_SECOND);
			}
			catch (InterruptedException e) {}
		}
	}
}
