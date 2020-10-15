package org.opensourcearcade.jinvaders.entities;

import org.opensourcearcade.jinvaders.Game;
import org.opensourcearcade.jinvaders.Sound;
import org.opensourcearcade.jinvaders.Speeds;

public class Ufo extends Entity {
    private Entity alienShot;

    public Entity getAlienShot() {
        return alienShot;
    }

    public void setAlienShot(Entity alienShot) {
        this.alienShot = alienShot;
    }

    public void reset() {
        this.sx = Speeds.getUfoSpeed();
        this.cntDown = 0;
        this.frame = 0;
        this.visible = false;
    }

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

    public void collisionPlayerShot(Ufo ufo, Player player) {
        ufo.frame = 1;
        ufo.cntDown = 1000;
        player.getPlayerShot().visible = false;
        Sound.stop(Sound.SOUNDS.UFO);
        Sound.play(Sound.SOUNDS.UFO_HIT);
    }


    public void collisionAlienShot(Player player, Entity shot) {
        shot.y = Game.HEIGHT + shot.h;

        Sound.play(Sound.SOUNDS.PLY_HIT);
        player.dx = 0;
        player.frame = 1;
        player.cntDown = 2000;
    }
}
