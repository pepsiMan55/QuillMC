package net.sopepsi.api;

/**
 * Plugin entry point. Prefer extending {@link JavaPlugin} for {@code getServer()}, {@code getLogger()},
 * and {@code registerEvents(Listener)}.
 */
public interface Plugin {

	void onEnable(Server server);

	default void onDisable() {
	}
}
