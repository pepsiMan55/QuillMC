package net.sopepsi.api;

import net.minecraft.src.PropertyManager;

/**
 * Keys written to {@code server.properties} on first start. Use these constants instead of raw strings.
 */
public final class ServerProperties {

	// --- Network ---
	public static final String SERVER_IP = "server-ip";
	public static final String SERVER_PORT = "server-port";
	public static final String ONLINE_MODE = "online-mode";
	public static final String MAX_PLAYERS = "max-players";

	// --- World ---
	public static final String LEVEL_NAME = "level-name";
	public static final String HELLWORLD = "hellworld";
	public static final String SPAWN_MONSTERS = "spawn-monsters";
	public static final String SPAWN_ANIMALS = "spawn-animals";
	public static final String SPAWN_PROTECTION = "spawn-protection";

	// --- Gameplay ---
	public static final String PVP = "pvp";
	public static final String WHITE_LIST = "white-list";
	public static final String SERVER_NAME = "server-name";
	public static final String ANNOUNCE_PLAYER_ACHIEVEMENTS = "announce-player-achievements";
	public static final String ENABLE_COMMAND_BLOCK = "enable-command-block";
	public static final String MAX_WORLD_SIZE = "max-world-size";
	public static final String ALLOW_FLIGHT = "allow-flight";

	// --- Performance / ops ---
	public static final String VIEW_DISTANCE = "view-distance";
	public static final String OP_PERMISSION_LEVEL = "op-permission-level";
	public static final String FUNCTION_PERMISSION_LEVEL = "function-permission-level";
	public static final String RESOURCE_PACK = "resource-pack";
	public static final String RESOURCE_PACK_SHA1 = "resource-pack-sha1";

	// --- Plugins ---
	public static final String PLUGINS_ENABLED = "plugins-enabled";
	public static final String PLUGIN_FOLDER = "plugin-folder";
	public static final String PLUGIN_VERBOSE_LOAD = "plugin-verbose-load";
	public static final String PLUGIN_LIST = "plugin-list";

	// --- Logging ---
	public static final String LOG_TO_FILE = "log-to-file";
	public static final String LOG_FILE = "log-file";
	public static final String LOG_TIMESTAMP = "log-timestamp";

	private ServerProperties() {
	}

	/**
	 * Ensures Quill-specific keys exist in {@code server.properties} with sensible defaults.
	 */
	public static void ensureDefaults(PropertyManager properties) {
		properties.getStringProperty(SERVER_IP, "");
		properties.getIntProperty(SERVER_PORT, 25565);
		properties.getBooleanProperty(ONLINE_MODE, true);
		properties.getIntProperty(MAX_PLAYERS, 20);

		properties.getStringProperty(LEVEL_NAME, "world");
		properties.getBooleanProperty(HELLWORLD, false);
		properties.getBooleanProperty(SPAWN_MONSTERS, true);
		properties.getBooleanProperty(SPAWN_ANIMALS, true);
		properties.getIntProperty(SPAWN_PROTECTION, 16);

		properties.getBooleanProperty(PVP, true);
		properties.getBooleanProperty(WHITE_LIST, false);
		properties.getStringProperty(SERVER_NAME, "Minecraft Server");
		properties.getIntProperty(MAX_WORLD_SIZE, 29999984);
		properties.getBooleanProperty(ALLOW_FLIGHT, false);

		properties.getIntProperty(VIEW_DISTANCE, 10);
		properties.getIntProperty(OP_PERMISSION_LEVEL, 4);
		properties.getIntProperty(FUNCTION_PERMISSION_LEVEL, 2);
		properties.getStringProperty(RESOURCE_PACK, "");
		properties.getStringProperty(RESOURCE_PACK_SHA1, "");

		properties.getBooleanProperty(PLUGINS_ENABLED, true);
		properties.getStringProperty(PLUGIN_FOLDER, "plugins");
		properties.getBooleanProperty(PLUGIN_VERBOSE_LOAD, true);
		properties.getBooleanProperty(PLUGIN_LIST, true);

		properties.getBooleanProperty(LOG_TO_FILE, true);
		properties.getStringProperty(LOG_FILE, "server.log");
		properties.getBooleanProperty(LOG_TIMESTAMP, true);
	}
}
