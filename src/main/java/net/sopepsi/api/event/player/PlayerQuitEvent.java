package net.sopepsi.api.event.player;

import net.sopepsi.api.player.Player;

public class PlayerQuitEvent extends PlayerEvent {

	private final String quitMessage;

	public PlayerQuitEvent(Player player, String quitMessage) {
		super(player);
		this.quitMessage = quitMessage;
	}

	public String getQuitMessage() {
		return this.quitMessage;
	}
}
