package net.sopepsi.api.event.player;

import net.sopepsi.api.player.Player;

public class PlayerRespawnEvent extends PlayerEvent {

    private double x;
    private double y;
    private double z;

    public PlayerRespawnEvent(Player player, double x, double y, double z) {
        super(player);

        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getRespawnX() {
        return x;
    }

    public double getRespawnY() {
        return y;
    }

    public double getRespawnZ() {
        return z;
    }
}