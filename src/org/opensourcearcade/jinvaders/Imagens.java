package org.opensourcearcade.jinvaders;

import org.opensourcearcade.jinvaders.entities.Entity;
import org.opensourcearcade.jinvaders.entities.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Imagens {
    private BufferedImage plyrImg, e1Img, e2Img, e3Img, ufoImg, llBnkImg, lrBnkImg, ulBnkImg, urBnkImg, mmBnkImg, sndOffImg, backbuffer;

    public Imagens() {
    }

    public void setImagens() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getPlyrImg() {
        return plyrImg;
    }

    public BufferedImage getE1Img() {
        return e1Img;
    }

    public BufferedImage getE2Img() {
        return e2Img;
    }

    public BufferedImage getE3Img() {
        return e3Img;
    }

    public BufferedImage getUfoImg() {
        return ufoImg;
    }

    public BufferedImage getLlBnkImg() {
        return llBnkImg;
    }

    public BufferedImage getLrBnkImg() {
        return lrBnkImg;
    }

    public BufferedImage getUlBnkImg() {
        return ulBnkImg;
    }

    public BufferedImage getUrBnkImg() {
        return urBnkImg;
    }

    public BufferedImage getMmBnkImg() {
        return mmBnkImg;
    }

    public BufferedImage getSndOffImg() {
        return sndOffImg;
    }

    public void setBackbuffer(BufferedImage backbuffer) {
        this.backbuffer = backbuffer;
    }

    public BufferedImage getBackbuffer() {
        return backbuffer;
    }

    // --- BUNKERS ---
    public void resetBunkers(Imagens imagens) {
        final int BUNKER_X = Game.WIDTH / 2 - (7 * (5 * imagens.getLlBnkImg().getWidth() / 3 + 2)) / 2;

        for (int b = 0; b < Game.BUNKERS.length; b++) {
            for (int y = 0; y < 4; y++) {
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

                    Entity e = Game.BUNKERS[b][y * 5 + x];
                    if (e == null) {
                        e = new Entity();
                        e.setImage(img, 3);
                        Game.BUNKERS[b][y * 5 + x] = e;
                    }
                    e.x = BUNKER_X + b * 2 * (e.w * 5) + x * e.w;
                    e.y = Pos.BUNKERS_Y_POS + y * e.h;
                    e.frame = 0;
                    e.visible = y != 3 || x != 2;
                }
            }
        }
    }

    public void updateBunkers(Entity alien) {
        for (int b = 0; b < Game.BUNKERS.length; b++) {
            for (int yy = 0; yy < 4; yy++) {
                for (int xx = 0; xx < 5; xx++) {
                    Entity bk = Game.BUNKERS[b][yy * 5 + xx];
                    if (bk.visible && ToolBox.checkCollision(alien, bk)) {
                        bk.visible = false;
                        Sound.play(Sound.SOUNDS.BASE_HIT);
                    }
                }
            }
        }
    }

    public void drawBunkers(Graphics g) {
        for (int b = 0; b < Game.BUNKERS.length; b++) {
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 5; x++) {
                    if (Game.BUNKERS[b][y * 5 + x].visible) {
                        Game.BUNKERS[b][y * 5 + x].draw(g);
                    }
                }
            }
        }
    }

    public void collisionBunkers(Entity shot, Player player) {
        boolean bnkHit = false;
        for (int b = 0; b < Game.BUNKERS.length && !bnkHit; b++) {
            for (int yy = 0; yy < 4 && !bnkHit; yy++) {
                for (int xx = 0; xx < 5 && !bnkHit; xx++) {
                    Entity bnk = Game.BUNKERS[b][yy * 5 + xx];
                    if (bnk.visible) {
                        // alienShot ./. bunker
                        if (shot.visible && ToolBox.checkCollision(shot, bnk)) {
                            shot.y = Game.HEIGHT + shot.h;
                            ++bnk.frame;
                            if (bnk.frame > 2)
                                bnk.visible = false;
                            bnkHit = true;
                        }
                        // playerShot ./. bunker
                        else if (player.getPlayerShot().visible && ToolBox.checkCollision(player.getPlayerShot(), bnk)) {
                            player.getPlayerShot().visible = false;
                            ++bnk.frame;
                            if (bnk.frame > 2)
                                bnk.visible = false;
                            bnkHit = true;
                        }

                        if (bnkHit)
                            Sound.play(Sound.SOUNDS.BASE_HIT);
                    }
                }
            }
        }
    }

    // --- ALIENS ---
    public void drawAliens(long frameCtr, Graphics g) {
        for (int y = 0; y < Game.ALIENS.length; y++) {
            for (int x = 0; x < Game.ALIENS[y].length; x++) {
                Entity alien = Game.ALIENS[y][x];
                if (alien.visible) {
                    alien.draw(g);
                    if (frameCtr == 0) {
                        if (alien.frame == Game.FRAMES_PER_IMAGE - 1)
                            alien.visible = false;
                        else
                            alien.frame = 1 - alien.frame;
                    }
                }
            }
        }
    }

    public void createNewWaveAliens(Imagens imagens) {
        int dx = imagens.getE1Img().getWidth() / 3 + 4;
        for (int y = 0; y < Game.ALIENS.length; y++) {
            for (int x = 0; x < Game.ALIENS[y].length; x++) {
                Entity alien = Game.ALIENS[y][x];
                alien.x = Pos.ALIENS_X_POS + x * dx + (dx / 2 - alien.w / 2);
                alien.y = Pos.ALIENS_Y_POS + y * alien.h * 2;
                alien.frame = 0;
                alien.visible = true;
            }
        }
    }

    public void resetAliens(Imagens imagens) {
        int dx = imagens.getE1Img().getWidth() / 3 + 4;
        for (int y = 0; y < Game.ALIENS.length; y++) {
            BufferedImage image;
            if (y == 0)
                image = imagens.getE3Img();
            else if (y < 3)
                image = imagens.getE2Img();
            else
                image = imagens.getE1Img();

            for (int x = 0; x < Game.ALIENS[y].length; x++) {
                Entity alien = Game.ALIENS[y][x];
                if (alien == null) {
                    alien = new Entity();
                    alien.setImage(image, 3);
                    Game.ALIENS[y][x] = alien;
                }
                alien.x = Pos.ALIENS_X_POS + x * dx + (dx / 2 - alien.w / 2);
                alien.y = Pos.ALIENS_Y_POS + y * alien.h * 2;
                alien.frame = 0;
                alien.visible = true;
            }
        }
    }
}
