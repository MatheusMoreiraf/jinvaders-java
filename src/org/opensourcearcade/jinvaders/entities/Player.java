package org.opensourcearcade.jinvaders.entities;

import org.opensourcearcade.jinvaders.Game;

public class Player extends Entity {
    public void explosions(Player player) {
        player.cntDown -= 1000 / Game.FRAMES_PER_SECOND;
        if (player.cntDown < 0) {
            player.cntDown = 0;
            player.frame = 0;
        }
    }
}
