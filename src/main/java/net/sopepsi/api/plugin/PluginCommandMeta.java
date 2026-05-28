package net.sopepsi.api.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command entry from {@code commands:} in plugin.yml / quill-plugin.yml.
 */
public final class PluginCommandMeta {

	private final String name;
	private final String description;
	private final String usage;
	private final String permission;
	private final List<String> aliases;

	public PluginCommandMeta(String name, String description, String usage, String permission, List<String> aliases) {
		this.name = name;
		this.description = description == null ? "" : description;
		this.usage = usage;
		this.permission = permission;
		this.aliases = aliases == null ? Collections.<String>emptyList()
				: Collections.unmodifiableList(new ArrayList<String>(aliases));
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getUsage() {
		return this.usage;
	}

	public String getPermission() {
		return this.permission;
	}

	public List<String> getAliases() {
		return this.aliases;
	}
}
