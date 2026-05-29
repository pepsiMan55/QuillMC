package net.minecraft.server;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.ConsoleLogManager;
import net.minecraft.src.ICommandListener;
import net.minecraft.src.IProgressUpdate;
import net.minecraft.src.IUpdatePlayerListBox;
import net.minecraft.src.NetworkListenThread;
import net.minecraft.src.PropertyManager;
import net.minecraft.src.ServerCommand;
import net.minecraft.src.ServerConfigurationManager;
import net.minecraft.src.ServerGUI;
import net.minecraft.src.ThreadCommandReader;
import net.minecraft.src.ThreadServerApplication;
import net.minecraft.src.ThreadSleepForever;
import net.minecraft.src.Vec3D;
import net.minecraft.src.WorldManager;
import net.minecraft.src.WorldServer;
import net.minecraft.src.EntityTracker;
import net.sopepsi.api.Server;
import net.sopepsi.api.ServerProperties;
import net.sopepsi.api.command.CommandRegistry;
import net.sopepsi.api.event.EventBus;
import net.sopepsi.api.plugin.PluginManager;
import net.sopepsi.server.BuiltInCommands;
import net.sopepsi.server.event.EventBusImpl;
import net.sopepsi.tools.QuillInit;

public class QuillMinecraftServer implements ICommandListener, Runnable {
	public static final Logger logger = Logger.getLogger("Quill");
	public static final String VERSION = "Quill a1.2.6-3 (2026-5-28)";

	/** Legacy command cooldown map (unused in current tree). */
	public static HashMap commandCooldowns = new HashMap();

	public NetworkListenThread networkListenThread;
	public PropertyManager propertyManagerObj;
	public WorldServer worldMngr;
	public ServerConfigurationManager configManager;
	/** @deprecated Use {@link #entityTracker} */
	@Deprecated
	public EntityTracker field_6028_k;
	/** @deprecated Use {@link #networkListenThread} */
	@Deprecated
	public NetworkListenThread field_6036_c;
	/** @deprecated Use {@link #spawnAnimals} */
	@Deprecated
	public boolean field_9011_n;
	private boolean serverRunning = true;
	public boolean shutdownComplete = false;
	int tickCounter = 0;
	public String progressMessage;
	public int progressPercent;
	private List tickListeners = new ArrayList();
	private List commands = Collections.synchronizedList(new ArrayList());
	public EntityTracker entityTracker;
	public boolean onlineMode;
	public boolean spawnAnimals;
	public boolean pvpEnabled;
	public int spawnProtectionRadius;
	public boolean whitelistEnabled;
	public String serverDisplayName;
	public boolean pluginsEnabled;

	private final EventBusImpl eventBus = new EventBusImpl();
	private final Server api = new Server(this, this.eventBus);
	private final CommandRegistry commandRegistry = new CommandRegistry(this.api);
	private PluginManager pluginManager;
	private File pluginsFolder;
	private final Set whitelist = new HashSet();
	private File whitelistFile;

	public QuillMinecraftServer() {
		new ThreadSleepForever(this);
	}

	public Server getApi() {
		return this.api;
	}

	public EventBus getEventBus() {
		return this.eventBus;
	}

	public CommandRegistry getCommandRegistry() {
		return this.commandRegistry;
	}

	public PluginManager getPluginManager() {
		return this.pluginManager;
	}

	public File getPluginsFolder() {
		return this.pluginsFolder;
	}

	private boolean initializeServer() throws UnknownHostException {
		ThreadCommandReader commandReader = new ThreadCommandReader(this);
		commandReader.setDaemon(true);
		commandReader.start();
		ConsoleLogManager.init();
		logger.info("Starting " + VERSION + " for Minecraft a1.2.6");
		logger.warning("**** WARNING");
		logger.warning("API Has been updated to 0.0.2, check GitHub for more info!");

		if(Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
			logger.warning("**** NOT ENOUGH RAM!");
			logger.warning("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar quill-a1.2.6.jar\"");
		}

		logger.info("Loading properties");
		this.propertyManagerObj = new PropertyManager(new File("server.properties"));
		ServerProperties.ensureDefaults(this.propertyManagerObj);

		String bindAddress = this.propertyManagerObj.getStringProperty(ServerProperties.SERVER_IP, "");
		this.onlineMode = this.propertyManagerObj.getBooleanProperty(ServerProperties.ONLINE_MODE, true);
		this.spawnAnimals = this.propertyManagerObj.getBooleanProperty(ServerProperties.SPAWN_ANIMALS, true);
		this.pvpEnabled = this.propertyManagerObj.getBooleanProperty(ServerProperties.PVP, true);
		this.spawnProtectionRadius = this.propertyManagerObj.getIntProperty(ServerProperties.SPAWN_PROTECTION, 16);
		this.whitelistEnabled = this.propertyManagerObj.getBooleanProperty(ServerProperties.WHITE_LIST, false);
		this.serverDisplayName = this.propertyManagerObj.getStringProperty(ServerProperties.SERVER_NAME, "Minecraft Server");
		this.pluginsEnabled = this.propertyManagerObj.getBooleanProperty(ServerProperties.PLUGINS_ENABLED, true);
		String pluginFolderName = this.propertyManagerObj.getStringProperty(ServerProperties.PLUGIN_FOLDER, "plugins");
		this.pluginsFolder = new File(pluginFolderName);
		
		this.whitelistFile = new File("whitelist.txt");
		this.loadWhitelist();

		BuiltInCommands.register(this, this.api, this.commandRegistry);

		InetAddress inetAddress = null;
		if(bindAddress.length() > 0) {
			inetAddress = InetAddress.getByName(bindAddress);
		}

		int port = this.propertyManagerObj.getIntProperty(ServerProperties.SERVER_PORT, 25565);
		logger.info("Starting Minecraft server on " + (bindAddress.length() == 0 ? "*" : bindAddress) + ":" + port);

		try {
			this.networkListenThread = new NetworkListenThread(this, inetAddress, port);
			this.field_6036_c = this.networkListenThread;
		} catch (IOException e) {
			logger.warning("**** FAILED TO BIND TO PORT!");
			logger.log(Level.WARNING, "The exception was: " + e.toString());
			logger.warning("Perhaps a server is already running on that port?");
			return false;
		}

		if(!this.onlineMode) {
			logger.warning("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
			logger.warning("The server will make no attempt to authenticate usernames. Beware.");
			logger.warning("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
			logger.warning("To change this, set \"online-mode\" to \"true\" in server.properties.");
		}

		this.configManager = new ServerConfigurationManager(this);
		this.entityTracker = new EntityTracker(this);
		this.field_6028_k = this.entityTracker;
		this.field_9011_n = this.spawnAnimals;

		if(this.pluginsEnabled) {
			this.pluginManager = new PluginManager(this.api, this.pluginsFolder);
			this.pluginManager.loadPlugins();
			
			int pluginCount = this.pluginManager.getLoadedPluginCount();
			logger.info("[PluginInitializerManager] Initialized " + pluginCount + " plugin" + (pluginCount != 1 ? "s" : ""));
			
			if(pluginCount > 0) {
				java.util.List<String> pluginNames = this.pluginManager.getLoadedPluginNames();
				StringBuilder sb = new StringBuilder("[PluginInitializerManager] Quill plugins (" + pluginCount + "):");
				for(String name : pluginNames) {
					sb.append("\n - ").append(name);
				}
				logger.info(sb.toString());
			} else {
				logger.info("[PluginInitializerManager] Quill plugins (0):");
			}
		} else {
			logger.info("Plugins disabled in server.properties");
			this.pluginsFolder.mkdirs();
		}

		String levelName = this.propertyManagerObj.getStringProperty(ServerProperties.LEVEL_NAME, "world");
		logger.info("Preparing level \"" + levelName + "\"");
		this.prepareLevel(levelName);

		if(this.pluginManager != null) {
			this.pluginManager.enablePlugins();
		}

		logger.info("Done! For help, type \"help\" or \"?\"");
		return true;
	}

	private void prepareLevel(String levelName) {
		logger.info("Preparing start region");
		this.worldMngr = new WorldServer(this, new File("."), levelName, this.propertyManagerObj.getBooleanProperty(ServerProperties.HELLWORLD, false) ? -1 : 0);
		this.worldMngr.func_4072_a(new WorldManager(this));
		this.worldMngr.monstersEnabled = this.propertyManagerObj.getBooleanProperty(ServerProperties.SPAWN_MONSTERS, true) ? 1 : 0;
		this.configManager.setPlayerManager(this.worldMngr);
		byte radius = 10;

		for(int x = -radius; x <= radius; ++x) {
			this.setProgress("Preparing spawn area", (x + radius) * 100 / (radius + radius + 1));

			for(int z = -radius; z <= radius; ++z) {
				if(!this.serverRunning) {
					return;
				}

				this.worldMngr.A.loadChunk((this.worldMngr.spawnX >> 4) + x, (this.worldMngr.spawnZ >> 4) + z);
			}
		}

		this.clearProgress();
	}

	private void setProgress(String message, int percent) {
		this.progressMessage = message;
		this.progressPercent = percent;
		System.out.println(message + ": " + percent + "%");
	}

	private void clearProgress() {
		this.progressMessage = null;
		this.progressPercent = 0;
	}

	private void saveServerWorld() {
		logger.info("Saving chunks");
		this.worldMngr.func_485_a(true, (IProgressUpdate)null);
	}

	private void stopServer() {
		logger.info("Stopping server");
		if(this.pluginManager != null) {
			this.pluginManager.disablePlugins();
		}
		if(this.configManager != null) {
			this.configManager.savePlayerStates();
		}
		if(this.worldMngr != null) {
			this.saveServerWorld();
		}
	}

	public void requestStop() {
		this.serverRunning = false;
	}

	public void run() {
		try {
			if(this.initializeServer()) {
				long lastTick = System.currentTimeMillis();
				long tickDebt = 0L;

				while(this.serverRunning) {
					long now = System.currentTimeMillis();
					long elapsed = now - lastTick;
					if(elapsed > 2000L) {
						logger.warning("Can\'t keep up! Did the system time change, or is the server overloaded?");
						elapsed = 2000L;
					}

					if(elapsed < 0L) {
						logger.warning("Time ran backwards! Did the system time change?");
						elapsed = 0L;
					}

					tickDebt += elapsed;
					lastTick = now;

					while(tickDebt > 50L) {
						tickDebt -= 50L;
						this.tick();
					}

					Thread.sleep(1L);
				}
			} else {
				while(this.serverRunning) {
					this.commandLineParser();

					try {
						Thread.sleep(10L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Unexpected exception", e);

			while(this.serverRunning) {
				this.commandLineParser();

				try {
					Thread.sleep(10L);
				} catch (InterruptedException interrupted) {
					interrupted.printStackTrace();
				}
			}
		} finally {
			this.stopServer();
			this.shutdownComplete = true;
			System.exit(0);
		}
	}

	private void tick() {
		ArrayList expired = new ArrayList();
		Iterator cooldownIterator = commandCooldowns.keySet().iterator();

		while(cooldownIterator.hasNext()) {
			String key = (String)cooldownIterator.next();
			int remaining = ((Integer)commandCooldowns.get(key)).intValue();
			if(remaining > 0) {
				commandCooldowns.put(key, Integer.valueOf(remaining - 1));
			} else {
				expired.add(key);
			}
		}

		for(int i = 0; i < expired.size(); ++i) {
			commandCooldowns.remove(expired.get(i));
		}

		AxisAlignedBB.clearBoundingBoxPool();
		Vec3D.initialize();
		++this.tickCounter;
		if(this.tickCounter % 20 == 0) {
			this.configManager.sendPacketToAllPlayers(new net.minecraft.src.Packet4UpdateTime(this.worldMngr.worldTime));
		}

		this.worldMngr.tick();

		while(this.worldMngr.func_6156_d()) {
		}

		this.worldMngr.func_459_b();
		this.networkListenThread.func_715_a();
		this.configManager.func_637_b();
		this.entityTracker.func_607_a();

		for(int i = 0; i < this.tickListeners.size(); ++i) {
			((IUpdatePlayerListBox)this.tickListeners.get(i)).update();
		}

		try {
			this.commandLineParser();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Unexpected exception while parsing console command", e);
		}
	}

	public void addCommand(String command, ICommandListener listener) {
		this.commands.add(new ServerCommand(command, listener));
	}

	public void commandLineParser() {
		while(this.commands.size() > 0) {
			ServerCommand entry = (ServerCommand)this.commands.remove(0);
			String line = entry.command;
			ICommandListener listener = entry.commandListener;
			if(!this.commandRegistry.dispatch(line, listener)) {
				logger.info("Unknown console command. Type \"help\" for help.");
			}
		}
	}

	public void addTickListener(IUpdatePlayerListBox listener) {
		this.tickListeners.add(listener);
	}

	public static void main(String[] args) {
		if(args.length > 0 && "init".equalsIgnoreCase(args[0])) {
			System.exit(QuillInit.run(args));
		}
		try {
			QuillMinecraftServer server = new QuillMinecraftServer();
			if(!GraphicsEnvironment.isHeadless() && (args.length <= 0 || !args[0].equals("nogui"))) {
				ServerGUI.initGui(server);
			}

			(new ThreadServerApplication("Server thread", server)).start();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to start the minecraft server", e);
		}
	}

	public File getFile(String path) {
		return new File(path);
	}

	public void log(String message) {
		logger.info(message);
	}

	public String getUsername() {
		return "CONSOLE";
	}

	public static boolean isServerRunning(QuillMinecraftServer server) {
		return server.serverRunning;
	}

	public boolean isPlayerWhitelisted(String username) {
		return this.whitelist.contains(username.trim().toLowerCase());
	}

	public Set getWhitelist() {
		return Collections.unmodifiableSet(this.whitelist);
	}

	public void addToWhitelist(String username) {
		this.whitelist.add(username.trim().toLowerCase());
		this.saveWhitelist();
	}

	public void removeFromWhitelist(String username) {
		this.whitelist.remove(username.trim().toLowerCase());
		this.saveWhitelist();
	}

	private void loadWhitelist() {
		this.whitelist.clear();
		if(!this.whitelistFile.exists()) {
			return;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.whitelistFile));
			String line;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0 && !line.startsWith("#")) {
					this.whitelist.add(line.toLowerCase());
				}
			}
			reader.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load whitelist.txt", e);
		}
	}

	private void saveWhitelist() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.whitelistFile));
			Iterator iterator = this.whitelist.iterator();
			while(iterator.hasNext()) {
				writer.write((String)iterator.next());
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to save whitelist.txt", e);
		}
	}

	@Deprecated
	public void func_6016_a() {
		this.requestStop();
	}

	@Deprecated
	public void func_6022_a(IUpdatePlayerListBox listener) {
		this.addTickListener(listener);
	}

	@Deprecated
	public static boolean func_6015_a(QuillMinecraftServer server) {
		return isServerRunning(server);
	}
}
