package org.opensourcearcade.jinvaders.entities;

import org.opensourcearcade.jinvaders.Game;
import org.opensourcearcade.jinvaders.Sound;

public class Player extends Entity {
    public void explosions(Player player) {
        player.cntDown -= 1000 / Game.FRAMES_PER_SECOND;
        if (player.cntDown < 0) {
            player.cntDown = 0;
            player.frame = 0;
        }
    }

    public void collisionAlien(Entity alien, Player player, int alienCtr) {
        Sound.play(Sound.SOUNDS.PLY_HIT);
        Sound.play(Sound.SOUNDS.INV_HIT);
        --alienCtr;
        alien.frame = Game.FRAMES_PER_IMAGE - 1;

        player.dx = 0;
        player.frame = 1;
        player.cntDown = 2000;
    }
}
