package org.opensourcearcade.jinvaders;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Entity
{
	protected BufferedImage image;

	protected float x, y, dx, dy, sx, sy;
	protected boolean visible = true;
	protected int frame, w, h;
	protected long cntDown;
	protected Entity prev;

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