package net.sopepsi.api.event;

import net.sopepsi.api.Plugin;

public interface EventBus {

	void register(Listener listener, Plugin plugin);

	void unregister(Plugin plugin);

	void call(Event event);
}
