package net.sopepsi.api.event;

/**
 * Base type for future Quill events (player join, chat, etc.). Extend the event system in
 * {@code net.sopepsi.server.event} without changing plugin compile dependencies.
 */
public abstract class Event {

	private boolean cancelled;

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
