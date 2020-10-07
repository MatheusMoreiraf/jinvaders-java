package org.opensourcearcade.jinvaders;

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
}
