package net.sopepsi.api.plugin;

import net.sopepsi.api.Logger;
import net.sopepsi.api.Plugin;
import net.sopepsi.api.Server;

public final class LoadedPlugin {

	private final PluginDescription description;
	private final Plugin instance;
	private final Logger logger;
	private final Server server;
	private boolean enabled;

	public LoadedPlugin(PluginDescription description, Plugin instance, Logger logger, Server server) {
		this.description = description;
		this.instance = instance;
		this.logger = logger;
		this.server = server;
	}

	public PluginDescription getDescription() {
		return this.description;
	}

	public Plugin getInstance() {
		return this.instance;
	}

	public Logger getLogger() {
		return this.logger;
	}

	public Server getServer() {
		return this.server;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
