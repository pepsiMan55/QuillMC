package net.sopepsi.api.command;

/**
 * Who ran a command (console, player, or plugin).
 */
public interface CommandSender {

	String getName();

	boolean isOp();

	boolean isConsole();

	boolean hasPermission(String permission);

	void sendMessage(String message);
}
