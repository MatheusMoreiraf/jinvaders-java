package org.opensourcearcade.jinvaders;

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

    public void alinesMovement(float alienDelta, float alienY, float alienMaxY, boolean eixoX) {
        for (int y = 0; y < Game.ALIENS.length; y++) {
            for (int x = 0; x < Game.ALIENS[y].length; x++) {
                Entity alien = Game.ALIENS[y][x];
                if (eixoX) alien.x += alienDelta;
                else alien.y += alienDelta;
                alienY = alien.y + alien.image.getHeight(null);
                alienMaxY = (alienY > alienMaxY) ? alienY : alienMaxY;
            }
        }
    }
}
