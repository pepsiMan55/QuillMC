package net.sopepsi.server;

import java.util.Iterator;
import java.util.Set;

import net.minecraft.server.QuillMinecraftServer;
import net.sopepsi.api.Server;
import net.sopepsi.api.command.CommandContext;
import net.sopepsi.api.command.CommandRegistry;
import net.sopepsi.api.player.Player;
import net.sopepsi.api.plugin.LoadedPlugin;

/**
 * Built-in console and in-game (op) commands.
 */
public final class BuiltInCommands {

	private BuiltInCommands() {
	}

	public static void register(QuillMinecraftServer server, Server api, CommandRegistry registry) {
		registry.register("help", "List commands", ctx -> cmdHelp(ctx, registry));
		registry.register("?", "List commands", ctx -> cmdHelp(ctx, registry));
		registry.register("plugins", "List loaded plugins", ctx -> cmdPlugins(api, ctx));
		registry.register("pl", "List loaded plugins", ctx -> cmdPlugins(api, ctx));
		registry.register("version", "Server version", ctx -> {
			ctx.reply(api.getVersion());
			return true;
		});
		registry.register("ver", "Server version", ctx -> {
			ctx.reply(api.getVersion());
			return true;
		});
		registry.register("about", "Server info", ctx -> {
			ctx.reply(api.getName() + " - " + api.getVersion());
			return true;
		});
		registry.register("stop", "Stop the server", ctx -> {
			if(!ctx.getSender().isOp()) {
				ctx.reply("You do not have permission.");
				return true;
			}
			ctx.reply("Stopping server...");
			api.stop();
			return true;
		});
		registry.register("save-all", "Save the world", ctx -> {
			if(!ctx.getSender().isOp()) {
				ctx.reply("You do not have permission.");
				return true;
			}
			api.saveWorld();
			ctx.reply("World saved.");
			return true;
		});
		registry.register("save", "Save the world", ctx -> {
			if(!ctx.getSender().isOp()) {
				ctx.reply("You do not have permission.");
				return true;
			}
			api.saveWorld();
			ctx.reply("World saved.");
			return true;
		});
		registry.register("list", "List online players", ctx -> {
			java.util.List<Player> players = api.getOnlinePlayers();
			if(players.isEmpty()) {
				ctx.reply("No players online.");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append(players.size()).append(" / ").append(api.getMaxPlayers()).append(" players: ");
				for(int i = 0; i < players.size(); ++i) {
					if(i > 0) {
						sb.append(", ");
					}
					sb.append(players.get(i).getName());
				}
				ctx.reply(sb.toString());
			}
			return true;
		});
		registry.register("say", "Broadcast a message", ctx -> {
			if(!ctx.getSender().isOp()) {
				ctx.reply("You do not have permission.");
				return true;
			}
			if(ctx.getArgCount() == 0) {
				ctx.reply("Usage: say <message>");
				return true;
			}
			String message = joinArgs(ctx.getArgs());
			api.broadcast("[" + ctx.getSender().getName() + "] " + message);
			return true;
		});
		registry.register("kick", "Kick a player", ctx -> cmdKick(api, ctx));
		registry.register("ban", "Ban a player", ctx -> cmdBan(server, ctx, true));
		registry.register("pardon", "Unban a player", ctx -> cmdBan(server, ctx, false));
		registry.register("ban-ip", "Ban an IP", ctx -> {
			if(!ctx.getSender().isOp()) {
				ctx.reply("You do not have permission.");
				return true;
			}
			if(ctx.getArgCount() < 1) {
				ctx.reply("Usage: ban-ip <address>");
				return true;
			}
			server.configManager.banIP(ctx.getArg(0));
			ctx.reply("Banned IP " + ctx.getArg(0));
			return true;
		});
		registry.register("pardon-ip", "Unban an IP", ctx -> {
			if(!ctx.getSender().isOp()) {
				ctx.reply("You do not have permission.");
				return true;
			}
			if(ctx.getArgCount() < 1) {
				ctx.reply("Usage: pardon-ip <address>");
				return true;
			}
			server.configManager.unbanIP(ctx.getArg(0));
			ctx.reply("Unbanned IP " + ctx.getArg(0));
			return true;
		});
		registry.register("op", "Grant operator", ctx -> cmdOp(server, ctx, true));
		registry.register("deop", "Revoke operator", ctx -> cmdOp(server, ctx, false));
		registry.register("whitelist", "Manage whitelist", ctx -> cmdWhitelist(server, ctx));
		registry.register("reload", "Reload plugins (restart required for jars)", ctx -> {
			if(!ctx.getSender().isOp()) {
				ctx.reply("You do not have permission.");
				return true;
			}
			ctx.reply("To load new plugin JARs, stop the server, add files to plugins/, and start again.");
			return true;
		});
		registry.register("seed", "Show world seed", ctx -> {
			if(server.worldMngr != null) {
				ctx.reply("Seed: " + server.worldMngr.randomSeed);
			} else {
				ctx.reply("World not loaded.");
			}
			return true;
		});
		registry.register("time", "Show world time", ctx -> {
			if(server.worldMngr != null) {
				ctx.reply("World time: " + server.worldMngr.worldTime);
			} else {
				ctx.reply("World not loaded.");
			}
			return true;
		});
	}

	private static boolean cmdHelp(CommandContext ctx, CommandRegistry registry) {
		ctx.reply("--- Minecraft commands ---");
		for(CommandRegistry.RegisteredCommand cmd : registry.getCommands().values()) {
			String owner = cmd.isPluginCommand() ? " [" + cmd.pluginName + "]" : "";
			ctx.reply("/" + cmd.name + owner + (cmd.description != null ? " — " + cmd.description : ""));
		}
		return true;
	}

	private static boolean cmdPlugins(Server api, CommandContext ctx) {
		if(!api.getPropertyBoolean(net.sopepsi.api.ServerProperties.PLUGIN_LIST, true) && !ctx.getSender().isOp()) {
			ctx.reply("You do not have permission.");
			return true;
		}
		java.util.List<LoadedPlugin> plugins = api.getPlugins();
		if(plugins.isEmpty()) {
			ctx.reply("No plugins loaded.");
			return true;
		}
		ctx.reply("Loaded plugins (" + plugins.size() + "):");
		for(LoadedPlugin plugin : plugins) {
			String authors = plugin.getDescription().getAuthors().isEmpty()
					? "unknown"
					: plugin.getDescription().getAuthors().get(0);
			String state = plugin.isEnabled() ? "ENABLED" : "DISABLED";
			ctx.reply(plugin.getDescription().getName()
					+ " v" + plugin.getDescription().getVersion()
					+ " by " + authors + " [" + state + "]");
		}
		return true;
	}

	private static boolean cmdKick(Server api, CommandContext ctx) {
		if(!ctx.getSender().isOp()) {
			ctx.reply("You do not have permission.");
			return true;
		}
		if(ctx.getArgCount() < 1) {
			ctx.reply("Usage: kick <player> [reason]");
			return true;
		}
		Player target = api.getPlayer(ctx.getArg(0));
		if(target == null) {
			ctx.reply("Player not found.");
			return true;
		}
		String reason = ctx.getArgCount() > 1 ? joinArgs(slice(ctx.getArgs(), 1)) : "Kicked by operator";
		target.kick(reason);
		ctx.reply("Kicked " + target.getName());
		return true;
	}

	private static boolean cmdBan(QuillMinecraftServer server, CommandContext ctx, boolean ban) {
		if(!ctx.getSender().isOp()) {
			ctx.reply("You do not have permission.");
			return true;
		}
		if(ctx.getArgCount() < 1) {
			ctx.reply(ban ? "Usage: ban <player>" : "Usage: pardon <player>");
			return true;
		}
		String name = ctx.getArg(0);
		if(ban) {
			server.configManager.banPlayer(name);
			Player online = server.getApi().getPlayer(name);
			if(online != null) {
				online.kick("Banned");
			}
			ctx.reply("Banned " + name);
		} else {
			server.configManager.unbanPlayer(name);
			ctx.reply("Unbanned " + name);
		}
		return true;
	}

	private static boolean cmdOp(QuillMinecraftServer server, CommandContext ctx, boolean grant) {
		if(!ctx.getSender().isOp()) {
			ctx.reply("You do not have permission.");
			return true;
		}
		if(ctx.getArgCount() < 1) {
			ctx.reply(grant ? "Usage: op <player>" : "Usage: deop <player>");
			return true;
		}
		String name = ctx.getArg(0);
		if(grant) {
			server.configManager.opPlayer(name);
			ctx.reply("Opped " + name);
		} else {
			server.configManager.deopPlayer(name);
			ctx.reply("De-opped " + name);
		}
		return true;
	}

	private static boolean cmdWhitelist(QuillMinecraftServer server, CommandContext ctx) {
		if(!ctx.getSender().isOp()) {
			ctx.reply("You do not have permission.");
			return true;
		}
		if(ctx.getArgCount() == 0) {
			ctx.reply("Usage: whitelist <on|off|list|add|remove> [player]");
			return true;
		}
		String sub = ctx.getArg(0).toLowerCase();
		if("on".equals(sub)) {
			server.propertyManagerObj.setBooleanProperty(net.sopepsi.api.ServerProperties.WHITE_LIST, true);
			server.whitelistEnabled = true;
			ctx.reply("Whitelist enabled.");
		} else if("off".equals(sub)) {
			server.propertyManagerObj.setBooleanProperty(net.sopepsi.api.ServerProperties.WHITE_LIST, false);
			server.whitelistEnabled = false;
			ctx.reply("Whitelist disabled.");
		} else if("list".equals(sub)) {
			Set set = server.getWhitelist();
			if(set.isEmpty()) {
				ctx.reply("Whitelist is empty.");
			} else {
				StringBuilder sb = new StringBuilder("Whitelist: ");
				Iterator it = set.iterator();
				while(it.hasNext()) {
					sb.append(it.next());
					if(it.hasNext()) {
						sb.append(", ");
					}
				}
				ctx.reply(sb.toString());
			}
		} else if("add".equals(sub) && ctx.getArgCount() >= 2) {
			server.addToWhitelist(ctx.getArg(1));
			ctx.reply("Added " + ctx.getArg(1) + " to whitelist.");
		} else if("remove".equals(sub) && ctx.getArgCount() >= 2) {
			server.removeFromWhitelist(ctx.getArg(1));
			ctx.reply("Removed " + ctx.getArg(1) + " from whitelist.");
		} else {
			ctx.reply("Usage: whitelist <on|off|list|add|remove> [player]");
		}
		return true;
	}

	private static String joinArgs(String[] args) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < args.length; ++i) {
			if(i > 0) {
				sb.append(' ');
			}
			sb.append(args[i]);
		}
		return sb.toString();
	}

	private static String[] slice(String[] args, int from) {
		String[] out = new String[args.length - from];
		System.arraycopy(args, from, out, 0, out.length);
		return out;
	}
}
