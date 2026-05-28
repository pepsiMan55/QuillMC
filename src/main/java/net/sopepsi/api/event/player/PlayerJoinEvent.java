package net.sopepsi.api.event.player;

import net.sopepsi.api.player.Player;

public class PlayerJoinEvent extends PlayerEvent {

	public PlayerJoinEvent(Player player) {
		super(player);
	}
}
