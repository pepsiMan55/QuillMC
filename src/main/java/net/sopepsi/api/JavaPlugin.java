package net.sopepsi.api;

import net.sopepsi.api.command.CommandHandler;
import net.sopepsi.api.event.EventBus;
import net.sopepsi.api.event.Listener;
import net.sopepsi.api.plugin.PluginDescription;

/**
 * Base class for plugins. Subclass this instead of implementing {@link Plugin} directly.
 *
 * <pre>{@code
 * public class Main extends JavaPlugin implements Listener {
 *     @Override
 *     public void onEnable() {
 *         getServer().registerCommand("ping", ctx -> { ctx.reply("Pong!"); return true; });
 *         registerEvents(this);
 *     }
 *
 *     @EventHandler
 *     public void onJoin(PlayerJoinEvent e) {
 *         getLogger().info(e.getPlayer().getName() + " joined");
 *     }
 * }
 * }</pre>
 */
public abstract class JavaPlugin implements Plugin, Listener {

	private Server server;
	private Logger logger;
	private PluginDescription description;

	@Override
	public final void onEnable(Server server) {
		this.server = server;
		onEnable();
	}

	/** Called when the plugin is enabled; use {@link #getServer()} and {@link #getLogger()}. */
	protected abstract void onEnable();

	@Override
	public void onDisable() {
		onDisablePlugin();
	}

	/** Override for cleanup when the plugin is disabled. */
	protected void onDisablePlugin() {
	}

	public Server getServer() {
		return this.server;
	}

	public Logger getLogger() {
		return this.logger;
	}

	public PluginDescription getDescription() {
		return this.description;
	}

	public String getName() {
		return this.description != null ? this.description.getName() : getClass().getSimpleName();
	}

	public void registerCommand(String name, CommandHandler handler) {
		this.server.registerCommand(name, handler);
	}

	public void registerCommand(String name, String description, CommandHandler handler) {
		this.server.registerCommand(name, description, handler);
	}

	public void registerEvents(Listener listener) {
		this.server.getEventBus().register(listener, this);
	}

	public void attach(Logger logger, PluginDescription description) {
		this.logger = logger;
		this.description = description;
	}
}
