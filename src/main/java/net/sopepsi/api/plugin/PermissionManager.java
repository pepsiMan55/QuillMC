package net.sopepsi.api.plugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sopepsi.api.command.CommandSender;

/**
 * Resolves permission nodes declared in plugin descriptors.
 */
public final class PermissionManager {

	private final Map<String, PermissionDefault> defaults = new HashMap<String, PermissionDefault>();

	public void register(PluginPermissionMeta meta) {
		if(meta == null || meta.getName() == null) {
			return;
		}
		this.defaults.put(meta.getName().toLowerCase(Locale.ROOT), meta.getDefaultPermission());
	}

	public void registerAll(Iterable<PluginPermissionMeta> permissions) {
		if(permissions == null) {
			return;
		}
		for(PluginPermissionMeta meta : permissions) {
			register(meta);
		}
	}

	public void clear() {
		this.defaults.clear();
	}

	public boolean has(CommandSender sender, String permission) {
		if(permission == null || permission.length() == 0) {
			return true;
		}
		if(sender.isConsole()) {
			return true;
		}
		if(sender.isOp()) {
			return true;
		}
		PermissionDefault def = this.defaults.get(permission.toLowerCase(Locale.ROOT));
		if(def == null) {
			return false;
		}
		return def == PermissionDefault.TRUE;
	}

	public boolean isEveryone(String permission) {
		if(permission == null || permission.length() == 0) {
			return true;
		}
		PermissionDefault def = this.defaults.get(permission.toLowerCase(Locale.ROOT));
		return def == PermissionDefault.TRUE;
	}
}
