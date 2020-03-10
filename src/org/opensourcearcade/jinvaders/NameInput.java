package org.opensourcearcade.jinvaders;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class NameInput
{
	private static final String STR_INPUTNAME="INPUT YOUR NAME";
	
	public static void draw(Graphics g,FontMetrics metrics, int width, int height, String tmpPlayerName, int inputCaretPos)
	{
		int h=metrics.getHeight();
		int strW=metrics.stringWidth(STR_INPUTNAME);
		g.drawString(STR_INPUTNAME,width / 2 - strW / 2,h * 7);

		int strLen=tmpPlayerName.length();
		int charW=metrics.stringWidth("M") + 1;
		int x=width / 2 - charW * 4;
		int y=height / 2 - h;

		for (int i=0; i < 8; i++)
		{
			g.drawLine(x + charW * i,y + 2,x + charW * (i + 1) - 2,y + 2); // underlines
			g.drawLine(x + charW * i,y + 3,x + charW * (i + 1) - 2,y + 3); // underlines

			if (i < strLen)
				g.drawString(tmpPlayerName.substring(i,i + 1),x + charW * i,y);
		}

		g.setColor(Color.red);
		g.drawLine(x + charW * inputCaretPos,y + 2,x + charW * (inputCaretPos + 1) - 2,y + 2); // underlines
		g.drawLine(x + charW * inputCaretPos,y + 3,x + charW * (inputCaretPos + 1) - 2,y + 3); // underlines
		g.setColor(Color.white);
	}
}
