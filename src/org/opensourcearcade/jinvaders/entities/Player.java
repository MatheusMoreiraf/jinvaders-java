package org.opensourcearcade.jinvaders.entities;

import org.opensourcearcade.jinvaders.Game;
import org.opensourcearcade.jinvaders.Imagens;
import org.opensourcearcade.jinvaders.Sound;
import org.opensourcearcade.jinvaders.Speeds;

public class Player extends Entity {
    private Entity playerShot;

    public Entity getPlayerShot() {
        return playerShot;
    }

    public void setPlayerShot(Entity playerShot) {
        this.playerShot = playerShot;
    }

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

    public void collisionPlayerShot(Entity alien, Player player, int alienCtr) {
        Sound.play(Sound.SOUNDS.INV_HIT);
        --alienCtr;
        alien.frame = Game.FRAMES_PER_IMAGE - 1;
        player.getPlayerShot().visible = false;
    }

    public void reset(Imagens imagens) {
        this.sx = Speeds.getPlayerSpeed();
        this.cntDown = 0;
        this.frame = 0;
        this.visible = true;
        this.x = Game.WIDTH / 2 - imagens.getPlyrImg().getWidth() / 2;
        // --- player shot ---
        if (this.getPlayerShot() == null) {
            this.setPlayerShot(new Entity());
            this.getPlayerShot().w = 2;
            this.getPlayerShot().h = 8;
        }
        this.getPlayerShot().x = this.x + this.w / 2 - 1;
        this.getPlayerShot().y = this.y - 10;
    }
}
