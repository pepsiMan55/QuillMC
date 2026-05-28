package net.sopepsi.api.plugin;

/**
 * Permission entry from {@code permissions:} in plugin.yml / quill-plugin.yml.
 */
public final class PluginPermissionMeta {

	private final String name;
	private final String description;
	private final PermissionDefault defaultPermission;

	public PluginPermissionMeta(String name, String description, PermissionDefault defaultPermission) {
		this.name = name;
		this.description = description == null ? "" : description;
		this.defaultPermission = defaultPermission == null ? PermissionDefault.OP : defaultPermission;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public PermissionDefault getDefaultPermission() {
		return this.defaultPermission;
	}
}
