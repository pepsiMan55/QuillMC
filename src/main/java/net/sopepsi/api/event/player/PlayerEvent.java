package net.sopepsi.api.event.player;

import net.sopepsi.api.event.Event;
import net.sopepsi.api.player.Player;

public abstract class PlayerEvent extends Event {

	private final Player player;

	protected PlayerEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return this.player;
	}
}
