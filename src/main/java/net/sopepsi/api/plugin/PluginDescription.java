package net.sopepsi.api.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Metadata from {@code quill-plugin.yml} or {@code plugin.yml} inside a plugin JAR.
 */
public final class PluginDescription {

	private final String name;
	private final String version;
	private final String mainClass;
	private final String description;
	private final List<String> authors;
	private final List<String> depend;
	private final List<String> softDepend;
	private final Map<String, PluginCommandMeta> commands;
	private final Map<String, PluginPermissionMeta> permissions;

	public PluginDescription(String name, String version, String mainClass, String description,
			List<String> authors, List<String> depend, List<String> softDepend,
			Map<String, PluginCommandMeta> commands, Map<String, PluginPermissionMeta> permissions) {
		this.name = name;
		this.version = version;
		this.mainClass = mainClass;
		this.description = description;
		this.authors = authors == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<String>(authors));
		this.depend = depend == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<String>(depend));
		this.softDepend = softDepend == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<String>(softDepend));
		this.commands = commands == null ? Collections.<String, PluginCommandMeta>emptyMap()
				: Collections.unmodifiableMap(new LinkedHashMap<String, PluginCommandMeta>(commands));
		this.permissions = permissions == null ? Collections.<String, PluginPermissionMeta>emptyMap()
				: Collections.unmodifiableMap(new LinkedHashMap<String, PluginPermissionMeta>(permissions));
	}

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

	public String getMainClass() {
		return this.mainClass;
	}

	public String getDescription() {
		return this.description;
	}

	public List<String> getAuthors() {
		return this.authors;
	}

	public List<String> getDepend() {
		return this.depend;
	}

	public List<String> getSoftDepend() {
		return this.softDepend;
	}

	public Map<String, PluginCommandMeta> getCommands() {
		return this.commands;
	}

	public Map<String, PluginPermissionMeta> getPermissions() {
		return this.permissions;
	}

	public PluginCommandMeta getCommand(String name) {
		if(name == null) {
			return null;
		}
		return this.commands.get(name.toLowerCase(Locale.ROOT));
	}

	public boolean declaresCommand(String name) {
		return getCommand(name) != null;
	}
}
