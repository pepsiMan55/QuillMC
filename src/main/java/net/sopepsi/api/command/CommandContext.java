package net.sopepsi.api.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sopepsi.api.Server;

/**
 * Context passed to command handlers (plugin API).
 */
public final class CommandContext {

	private final Server server;
	private final CommandSender sender;
	private final String label;
	private final String[] args;

	public CommandContext(Server server, CommandSender sender, String label, String[] args) {
		this.server = server;
		this.sender = sender;
		this.label = label;
		this.args = args == null ? new String[0] : args;
	}

	public Server getServer() {
		return this.server;
	}

	public CommandSender getSender() {
		return this.sender;
	}

	public String getLabel() {
		return this.label;
	}

	public String[] getArgs() {
		return this.args.clone();
	}

	public List<String> getArgsList() {
		return Collections.unmodifiableList(Arrays.asList(this.args));
	}

	public String getArg(int index) {
		return index >= 0 && index < this.args.length ? this.args[index] : null;
	}

	public int getArgCount() {
		return this.args.length;
	}

	public void reply(String message) {
		this.sender.sendMessage(message);
	}

	public void broadcast(String message) {
		this.server.broadcast(message);
	}
}
