package net.sopepsi.api.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.src.ICommandListener;
import net.sopepsi.api.Server;
import net.sopepsi.api.ServerProperties;
import net.sopepsi.server.command.CommandSenderAdapter;

/**
 * Central command dispatcher for built-in and plugin commands.
 */
public final class CommandRegistry {

	private final Server server;
	private final Map<String, RegisteredCommand> commands = new LinkedHashMap<String, RegisteredCommand>();

	public CommandRegistry(Server server) {
		this.server = server;
	}

	public void register(String name, String description, CommandHandler handler) {
		register(name, description, null, handler);
	}

	public void register(String name, String description, String usage, CommandHandler handler) {
		String key = normalize(name);
		this.commands.put(key, new RegisteredCommand(name, description, usage, null, handler, null));
	}

	public void registerPlugin(String pluginName, String name, String description, String permission, CommandHandler handler) {
		String key = normalize(name);
		this.commands.put(key, new RegisteredCommand(name, description, null, permission, handler, pluginName));
	}

	public boolean unregister(String name) {
		return this.commands.remove(normalize(name)) != null;
	}

	public boolean dispatch(String line, ICommandListener listener) {
		if(line == null || line.trim().length() == 0) {
			return false;
		}
		String trimmed = line.trim();
		int space = trimmed.indexOf(' ');
		String label = space < 0 ? trimmed : trimmed.substring(0, space);
		String[] args = space < 0 ? new String[0] : trimmed.substring(space + 1).trim().split("\\s+");
		if(args.length == 1 && args[0].length() == 0) {
			args = new String[0];
		}

		RegisteredCommand command = this.commands.get(normalize(label));
		if(command == null) {
			return false;
		}

		CommandSender sender = CommandSenderAdapter.from(listener, this.server);
		if(command.permission != null && command.permission.length() > 0
				&& !this.server.getPermissionManager().has(sender, command.permission)) {
			sender.sendMessage("You do not have permission.");
			return true;
		}

		CommandContext ctx = new CommandContext(this.server, sender, label, args);
		try {
			return command.handler.execute(ctx);
		} catch (Exception e) {
			sender.sendMessage("Command failed: " + e.getMessage());
			this.server.getLogger().log(java.util.logging.Level.WARNING, "Error in /" + label, e);
			return true;
		}
	}

	public boolean isAllowedForNonOp(String label) {
		String key = normalize(label);
		if("list".equals(key)) {
			return true;
		}
		if("plugins".equals(key) || "pl".equals(key)) {
			return this.server.getPropertyBoolean(ServerProperties.PLUGIN_LIST, true);
		}
		RegisteredCommand command = this.commands.get(key);
		if(command != null && command.isPluginCommand()) {
			return this.server.getPermissionManager().isEveryone(command.permission);
		}
		return false;
	}

	public List<String> getRegisteredNamesForPlugin(String pluginName) {
		List<String> names = new ArrayList<String>();
		for(RegisteredCommand command : this.commands.values()) {
			if(pluginName.equals(command.pluginName)) {
				names.add(command.name.toLowerCase(Locale.ROOT));
			}
		}
		return names;
	}

	public Set<String> getCommandNames() {
		TreeSet<String> names = new TreeSet<String>();
		for(RegisteredCommand cmd : this.commands.values()) {
			names.add(cmd.name);
		}
		return Collections.unmodifiableSet(names);
	}

	public Map<String, RegisteredCommand> getCommands() {
		return Collections.unmodifiableMap(this.commands);
	}

	private static String normalize(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	public static final class RegisteredCommand {
		public final String name;
		public final String description;
		public final String usage;
		public final String permission;
		public final CommandHandler handler;
		public final String pluginName;

		RegisteredCommand(String name, String description, String usage, String permission,
				CommandHandler handler, String pluginName) {
			this.name = name;
			this.description = description;
			this.usage = usage;
			this.permission = permission;
			this.handler = handler;
			this.pluginName = pluginName;
		}

		public boolean isPluginCommand() {
			return this.pluginName != null;
		}
	}
}
