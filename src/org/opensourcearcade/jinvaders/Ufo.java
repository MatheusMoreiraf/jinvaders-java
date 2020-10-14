package org.opensourcearcade.jinvaders;

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
}
