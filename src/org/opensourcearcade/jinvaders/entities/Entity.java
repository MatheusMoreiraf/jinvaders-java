package org.opensourcearcade.jinvaders.entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Entity
{
	public BufferedImage image;

	public float x, y, dx, dy, sx, sy;
	public boolean visible = true;
	public int frame, w, h;
	public long cntDown;
	public Entity prev;

	public void setImage(BufferedImage image, int frames)
	{
		this.image = image;
		w = image.getWidth(null) / frames;
		h = image.getHeight(null);
	}

	public void draw(Graphics g)
	{
		g.drawImage(image, (int) x, (int) y, (int) x + w, (int) y + h, frame * w, 0, (frame + 1) * w, (int) h, null);
	}
}
