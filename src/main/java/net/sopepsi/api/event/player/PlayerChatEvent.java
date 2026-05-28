package net.sopepsi.api.event.player;

import net.sopepsi.api.player.Player;

public class PlayerChatEvent extends PlayerEvent {

	private String message;

	public PlayerChatEvent(Player player, String message) {
		super(player);
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
