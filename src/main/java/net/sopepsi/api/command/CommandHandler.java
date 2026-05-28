package net.sopepsi.api.command;

/**
 * Functional command handler used by plugins and the command registry.
 */
@FunctionalInterface
public interface CommandHandler {
	boolean execute(CommandContext context);
}
