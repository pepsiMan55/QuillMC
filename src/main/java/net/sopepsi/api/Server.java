package net.sopepsi.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Packet3Chat;
import net.minecraft.src.PropertyManager;
import net.minecraft.server.QuillMinecraftServer;
import net.sopepsi.api.command.CommandHandler;
import net.sopepsi.api.command.CommandRegistry;
import net.sopepsi.api.event.EventBus;
import net.sopepsi.api.player.Player;
import net.sopepsi.api.plugin.LoadedPlugin;
import net.sopepsi.api.plugin.PermissionManager;
import net.sopepsi.api.plugin.PluginCommandMeta;
import net.sopepsi.api.plugin.PluginDescription;
import net.sopepsi.server.player.PlayerImpl;

public final class Server {

	private final QuillMinecraftServer server;
	private final Logger logger;
	private final EventBus eventBus;
	private final PermissionManager permissionManager = new PermissionManager();
	private String registeringPluginName;

	public Server(QuillMinecraftServer server, EventBus eventBus) {
		this.server = server;
		this.eventBus = eventBus;
		this.logger = new Logger("Server");
	}

	public String getVersion() {
		return QuillMinecraftServer.VERSION;
	}

	public String getName() {
		return this.server.serverDisplayName;
	}

	public File getDataFolder() {
		return new File(".");
	}

	public File getPluginsFolder() {
		return this.server.getPluginsFolder();
	}

	public Logger getLogger() {
		return this.logger;
	}

	public Logger createLogger(String pluginName) {
		return new Logger(pluginName);
	}

	public EventBus getEventBus() {
		return this.eventBus;
	}

	public PropertyManager getProperties() {
		return this.server.propertyManagerObj;
	}

	public String getProperty(String key, String defaultValue) {
		return this.server.propertyManagerObj.getStringProperty(key, defaultValue);
	}

	public int getPropertyInt(String key, int defaultValue) {
		return this.server.propertyManagerObj.getIntProperty(key, defaultValue);
	}

	public boolean getPropertyBoolean(String key, boolean defaultValue) {
		return this.server.propertyManagerObj.getBooleanProperty(key, defaultValue);
	}

	public CommandRegistry getCommandRegistry() {
		return this.server.getCommandRegistry();
	}

	public PermissionManager getPermissionManager() {
		return this.permissionManager;
	}

	public void registerCommand(String name, CommandHandler handler) {
		if(this.registeringPluginName != null) {
			registerPluginCommand(this.registeringPluginName, name, "Plugin command", handler);
		} else {
			this.getCommandRegistry().register(name, "Plugin command", handler);
		}
	}

	public void registerCommand(String name, String description, CommandHandler handler) {
		if(this.registeringPluginName != null) {
			registerPluginCommand(this.registeringPluginName, name, description, handler);
		} else {
			this.getCommandRegistry().register(name, description, handler);
		}
	}

	public void registerPluginCommand(String pluginName, String name, String description, CommandHandler handler) {
		PluginDescription pluginDescription = findPluginDescription(pluginName);
		if(pluginDescription != null && !pluginDescription.declaresCommand(name)) {
			this.logger.warning("Plugin " + pluginName + " registered command '/" + name
					+ "' without declaring it in plugin.yml (plugin.yml)");
		}
		String permission = null;
		String commandDescription = description;
		if(pluginDescription != null) {
			PluginCommandMeta meta = pluginDescription.getCommand(name);
			if(meta != null) {
				permission = meta.getPermission();
				if(meta.getDescription().length() > 0) {
					commandDescription = meta.getDescription();
				}
			}
		}
		this.getCommandRegistry().registerPlugin(pluginName, name, commandDescription, permission, handler);
		if(pluginDescription != null) {
			PluginCommandMeta meta = pluginDescription.getCommand(name);
			if(meta != null) {
				for(String alias : meta.getAliases()) {
					this.getCommandRegistry().registerPlugin(pluginName, alias, commandDescription, permission, handler);
				}
			}
		}
	}

	private PluginDescription findPluginDescription(String pluginName) {
		for(LoadedPlugin plugin : getPlugins()) {
			if(plugin.getDescription().getName().equals(pluginName)) {
				return plugin.getDescription();
			}
		}
		return null;
	}

	public void beginPluginRegistration(String pluginName) {
		this.registeringPluginName = pluginName;
	}

	public void endPluginRegistration() {
		this.registeringPluginName = null;
	}

	public void broadcast(String message) {
		if(this.server.configManager != null) {
			this.server.configManager.sendPacketToAllPlayers(new Packet3Chat(message));
		}
		this.logger.info(message);
	}

	public List<Player> getOnlinePlayers() {
		if(this.server.configManager == null) {
			return Collections.emptyList();
		}
		List<Player> players = new ArrayList<Player>();
		List entities = this.server.configManager.playerEntities;
		for(int i = 0; i < entities.size(); ++i) {
			EntityPlayerMP mp = (EntityPlayerMP)entities.get(i);
			players.add(new PlayerImpl(mp, this));
		}
		return Collections.unmodifiableList(players);
	}

	public Player getPlayer(String name) {
		if(name == null) {
			return null;
		}
		for(Player player : getOnlinePlayers()) {
			if(player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}

	public int getMaxPlayers() {
		if(this.server.configManager == null) {
			return getPropertyInt(ServerProperties.MAX_PLAYERS, 20);
		}
		return this.server.configManager.getMaxPlayers();
	}

	public boolean isOp(String name) {
		return this.server.configManager != null && this.server.configManager.isOp(name);
	}

	public void stop() {
		this.server.requestStop();
	}

	public void saveWorld() {
		if(this.server.worldMngr != null) {
			this.server.worldMngr.func_485_a(true, null);
		}
	}

	public List<LoadedPlugin> getPlugins() {
		if(this.server.getPluginManager() == null) {
			return Collections.emptyList();
		}
		return this.server.getPluginManager().getLoadedPlugins();
	}

	/** @deprecated Plugins must not use Minecraft internals. */
	@Deprecated
	public QuillMinecraftServer getUnsafe() {
		return this.server;
	}
}
