package net.sopepsi.server.command;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICommandListener;
import net.minecraft.src.Packet3Chat;
import net.sopepsi.api.Server;
import net.sopepsi.api.command.CommandSender;

/**
 * Bridges legacy {@link ICommandListener} to the public {@link CommandSender} API.
 */
public final class CommandSenderAdapter implements CommandSender {

	private final ICommandListener listener;
	private final Server server;

	private CommandSenderAdapter(ICommandListener listener, Server server) {
		this.listener = listener;
		this.server = server;
	}

	public static CommandSender from(ICommandListener listener, Server server) {
		return new CommandSenderAdapter(listener, server);
	}

	public ICommandListener getListener() {
		return this.listener;
	}

	@Override
	public String getName() {
		return this.listener.getUsername();
	}

	@Override
	public boolean isOp() {
		if(this.isConsole()) {
			return true;
		}
		return this.server.isOp(this.getName());
	}

	@Override
	public boolean isConsole() {
		return "CONSOLE".equalsIgnoreCase(this.getName());
	}

	@Override
	public boolean hasPermission(String permission) {
		return this.server.getPermissionManager().has(this, permission);
	}

	@Override
	public void sendMessage(String message) {
		if(this.listener instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)this.listener;
			player.field_421_a.sendPacket(new Packet3Chat(message));
		} else {
			this.listener.log(message);
		}
	}
}
