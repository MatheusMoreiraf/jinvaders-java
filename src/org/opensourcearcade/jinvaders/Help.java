package org.opensourcearcade.jinvaders;

import org.opensourcearcade.jinvaders.entities.Entity;

import java.awt.*;

public class Help {
    public int getMostColumn(String direction) {
        int column = direction.equals("Right") ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int y = 0; y < Game.ALIENS.length; y++)
            for (int x = 0; x < Game.ALIENS[y].length; x++)
                if (Game.ALIENS[y][x].visible) {
                    column = direction.equals("Right") ? Math.max(column, x) : Math.min(column, x);
                }
        return column;
    }

    public float alinesMovement(float alienDelta, float alienY, float alienMaxY, boolean eixoX) {
        for (int y = 0; y < Game.ALIENS.length; y++) {
            for (int x = 0; x < Game.ALIENS[y].length; x++) {
                Entity alien = Game.ALIENS[y][x];
                if (eixoX) alien.x += alienDelta;
                else alien.y += alienDelta;
                alienY = alien.y + alien.image.getHeight(null);
                alienMaxY = (alienY > alienMaxY) ? alienY : alienMaxY;
            }
        }
        return alienMaxY;
    }

    public static void inputName(int width, int height, int charW, int strLen, int h, String tmpPlayerName, Graphics g, int inputCaretPos) {
        int x = width / 2 - charW * 4;
        int y = height / 2 - h;

        for (int i = 0; i < 8; i++) {
            g.drawLine(x + charW * i, y + 2, x + charW * (i + 1) - 2, y + 2); // underlines
            g.drawLine(x + charW * i, y + 3, x + charW * (i + 1) - 2, y + 3); // underlines

            if (i < strLen)
                g.drawString(tmpPlayerName.substring(i, i + 1), x + charW * i, y);
        }

        g.setColor(Color.red);
        g.drawLine(x + charW * inputCaretPos, y + 2, x + charW * (inputCaretPos + 1) - 2, y + 2); // underlines
        g.drawLine(x + charW * inputCaretPos, y + 3, x + charW * (inputCaretPos + 1) - 2, y + 3); // underlines
        g.setColor(Color.white);

    }


}
