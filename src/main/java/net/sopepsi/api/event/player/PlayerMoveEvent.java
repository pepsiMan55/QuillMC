package net.sopepsi.api.event.player;

import net.sopepsi.api.player.Player;

public class PlayerMoveEvent extends PlayerEvent {

    private final double fromX, fromY, fromZ;
    private final double toX, toY, toZ;

    public PlayerMoveEvent(Player player,
                           double fromX, double fromY, double fromZ,
                           double toX, double toY, double toZ) {
        super(player);

        this.fromX = fromX;
        this.fromY = fromY;
        this.fromZ = fromZ;

        this.toX = toX;
        this.toY = toY;
        this.toZ = toZ;
    }

    public double getFromX() { return fromX; }
    public double getFromY() { return fromY; }
    public double getFromZ() { return fromZ; }

    public double getToX() { return toX; }
    public double getToY() { return toY; }
    public double getToZ() { return toZ; }
}