package net.sopepsi.api.plugin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import net.sopepsi.api.JavaPlugin;
import net.sopepsi.api.Logger;
import net.sopepsi.api.Plugin;
import net.sopepsi.api.Server;
import net.sopepsi.server.plugin.PluginClassLoader;
import net.sopepsi.server.plugin.PluginYamlParser;

public final class PluginManager {

	private final Server server;
	private final File pluginsFolder;
	private final List<LoadedPlugin> loaded = new ArrayList<LoadedPlugin>();
	private final Logger logger;

	public PluginManager(Server server, File pluginsFolder) {
		this.server = server;
		this.pluginsFolder = pluginsFolder;
		this.logger = server.getLogger();
		if(!pluginsFolder.exists()) {
			if(pluginsFolder.mkdirs()) {
				this.logger.info("Created plugins folder: " + pluginsFolder.getAbsolutePath());
			}
		}
	}

	public void loadPlugins() {
		this.loaded.clear();
		if(!this.pluginsFolder.isDirectory()) {
			this.logger.warning("Plugin folder is not a directory: " + this.pluginsFolder.getAbsolutePath());
			return;
		}

		File[] jars = this.pluginsFolder.listFiles();
		if(jars == null || jars.length == 0) {
			this.logger.info("No plugins found in " + this.pluginsFolder.getAbsolutePath() + " (drop .jar files here)");
			return;
		}

		int count = 0;
		for(File jar : jars) {
			if(!jar.isFile() || !jar.getName().toLowerCase().endsWith(".jar")) {
				continue;
			}
			try {
				LoadedPlugin plugin = loadJar(jar);
				if(plugin != null) {
					this.loaded.add(plugin);
					++count;
					this.logger.info("Loaded plugin " + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion()
							+ " from " + jar.getName());
				}
			} catch (Exception e) {
				this.logger.log(Level.SEVERE, "Failed to load plugin from " + jar.getName(), e);
			}
		}

		this.logger.info("Loaded " + count + " plugin(s) from " + this.pluginsFolder.getAbsolutePath());
	}

	private LoadedPlugin loadJar(File jarFile) throws Exception {
		PluginDescription description = readDescription(jarFile);
		if(description == null) {
			this.logger.warning("Skipping " + jarFile.getName() + ": missing quill-plugin.yml");
			return null;
		}

		URL jarUrl = jarFile.toURI().toURL();
		PluginClassLoader classLoader = new PluginClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());
		Class<?> mainClass = Class.forName(description.getMainClass(), true, classLoader);
		Object instance = mainClass.newInstance();
		if(!(instance instanceof Plugin)) {
			throw new IllegalStateException("Main class must implement Plugin: " + description.getMainClass());
		}

		Plugin plugin = (Plugin)instance;
		Logger pluginLogger = this.server.createLogger(description.getName());
		return new LoadedPlugin(description, plugin, pluginLogger, this.server);
	}

	private PluginDescription readDescription(File jarFile) throws Exception {
		JarFile jar = new JarFile(jarFile);
		try {
			String[] paths = {"quill-plugin.yml", "plugin.yml", "META-INF/quill-plugin.yml"};
			for(String path : paths) {
				JarEntry entry = jar.getJarEntry(path);
				if(entry != null) {
					InputStream in = jar.getInputStream(entry);
					try {
						return PluginYamlParser.parse(in);
					} finally {
						in.close();
					}
				}
			}
			return null;
		} finally {
			jar.close();
		}
	}

	public void enablePlugins() {
		this.server.getPermissionManager().clear();
		for(LoadedPlugin loadedPlugin : this.loaded) {
			this.server.getPermissionManager().registerAll(loadedPlugin.getDescription().getPermissions().values());
		}

		for(LoadedPlugin loadedPlugin : this.loaded) {
			String pluginName = loadedPlugin.getDescription().getName();
			try {
				Plugin plugin = loadedPlugin.getInstance();
				if(plugin instanceof JavaPlugin) {
					((JavaPlugin)plugin).attach(loadedPlugin.getLogger(), loadedPlugin.getDescription());
				}
				loadedPlugin.getLogger().info("Enabling...");
				this.server.beginPluginRegistration(pluginName);
				try {
					plugin.onEnable(this.server);
					loadedPlugin.setEnabled(true);
					loadedPlugin.getLogger().info("Enabled.");
					validateDeclaredCommands(loadedPlugin, pluginName);
				} finally {
					this.server.endPluginRegistration();
				}
			} catch (Exception e) {
				loadedPlugin.getLogger().log(Level.SEVERE, "Failed to enable plugin", e);
			}
		}
	}

	private void validateDeclaredCommands(LoadedPlugin loadedPlugin, String pluginName) {
		java.util.List<String> registered = this.server.getCommandRegistry().getRegisteredNamesForPlugin(pluginName);
		for(String declared : loadedPlugin.getDescription().getCommands().keySet()) {
			if(!registered.contains(declared)) {
				this.logger.warning("Plugin " + pluginName + " declares command '/" + declared
						+ "' in plugin.yml but did not register it in onEnable()");
			}
		}
	}

	public void disablePlugins() {
		for(int i = this.loaded.size() - 1; i >= 0; --i) {
			LoadedPlugin loadedPlugin = this.loaded.get(i);
			if(!loadedPlugin.isEnabled()) {
				continue;
			}
			try {
				loadedPlugin.getLogger().info("Disabling...");
				loadedPlugin.getInstance().onDisable();
				this.server.getEventBus().unregister(loadedPlugin.getInstance());
				loadedPlugin.setEnabled(false);
				loadedPlugin.getLogger().info("Disabled.");
			} catch (Exception e) {
				loadedPlugin.getLogger().log(Level.WARNING, "Error while disabling", e);
			}
		}
	}

	public List<LoadedPlugin> getLoadedPlugins() {
		return Collections.unmodifiableList(new ArrayList<LoadedPlugin>(this.loaded));
	}
}
