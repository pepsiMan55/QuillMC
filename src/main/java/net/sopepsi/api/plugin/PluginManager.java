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

		File[] files = this.pluginsFolder.listFiles(new java.io.FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});

		if(files == null) {
			this.logger.warning("Could not read plugin folder");
			return;
		}

		if(files.length == 0) {
			this.logger.info("No plugin .jar files found in " + this.pluginsFolder.getAbsolutePath());
			return;
		}

		java.util.Set<String> loadedNames = new java.util.HashSet<String>();
		int count = 0;
		for(File jarFile : files) {
			try {
				LoadedPlugin plugin = loadJar(jarFile);
				if(plugin != null) {
					String pluginName = plugin.getDescription().getName();
					
					// Check for duplicates
					if(loadedNames.contains(pluginName)) {
						this.logger.warning("SKIPPING duplicate plugin: " + pluginName + " from " + jarFile.getName());
						continue;
					}
					
					loadedNames.add(pluginName);
					this.loaded.add(plugin);
					++count;
				}
			} catch (Exception e) {
				this.logger.log(Level.SEVERE, "Failed to load plugin from " + jarFile.getName(), e);
			}
		}
	}

	private LoadedPlugin loadJar(File jarFile) throws Exception {
		PluginDescription description = readDescription(jarFile);
		if(description == null) {
			this.logger.warning("Skipping " + jarFile.getName() + ": missing plugin descriptor");
			return null;
		}

		URL jarUrl = jarFile.toURI().toURL();
		PluginClassLoader classLoader = new PluginClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());

		Class<?> mainClass = Class.forName(description.getMainClass(), true, classLoader);
		Object instance = mainClass.newInstance();
		
		if(!(instance instanceof Plugin)) {
			throw new IllegalStateException("Main class " + description.getMainClass() + " must implement Plugin interface");
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

	public int getLoadedPluginCount() {
		return this.loaded.size();
	}

	public List<String> getLoadedPluginNames() {
		List<String> names = new ArrayList<String>();
		for(LoadedPlugin plugin : this.loaded) {
			names.add(plugin.getDescription().getName());
		}
		return names;
	}
}
