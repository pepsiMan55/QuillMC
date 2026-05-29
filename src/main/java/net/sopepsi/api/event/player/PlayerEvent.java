package net.sopepsi.api.event.player;

import net.sopepsi.api.player.Player;
import net.sopepsi.api.event.Event;

public class PlayerEvent extends Event {

    private final Player player;

    public PlayerEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}