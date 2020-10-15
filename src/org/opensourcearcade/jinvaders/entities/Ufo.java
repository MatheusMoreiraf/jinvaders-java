package org.opensourcearcade.jinvaders.entities;

import org.opensourcearcade.jinvaders.Game;
import org.opensourcearcade.jinvaders.Sound;

public class Ufo extends Entity {
    public void explosions(Entity ufo, int score1) {
        ufo.cntDown -= 1000 / Game.FRAMES_PER_SECOND;
        if (ufo.cntDown < 0) {
            if (ufo.frame == 1) {
                ufo.cntDown = 1000;
                ufo.frame = 2;
            } else {
                ufo.visible = false;
                ufo.frame = 0;
                score1 += (int) (Math.random() * 10) * 100;
            }
        }
    }

    public void updatePosition(Ufo ufo, long time) {
        if (ufo.frame == 0) {
            float delta = ufo.sx * (time / 1000000000.0f);
            if (ufo.x > Game.WIDTH) {
                ufo.visible = false;
                Sound.stop(Sound.SOUNDS.UFO);
            } else {
                ufo.x += delta;
            }
        }
    }
}