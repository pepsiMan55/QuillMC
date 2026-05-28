package net.sopepsi.server.player;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Packet255KickDisconnect;
import net.minecraft.src.Packet3Chat;
import net.sopepsi.api.Server;
import net.sopepsi.api.player.Player;

public final class PlayerImpl implements Player {

	private final EntityPlayerMP handle;
	private final Server server;

	public PlayerImpl(EntityPlayerMP handle, Server server) {
		this.handle = handle;
		this.server = server;
	}

	public EntityPlayerMP getHandle() {
		return this.handle;
	}

	@Override
	public String getName() {
		return this.handle.username;
	}

	@Override
	public void sendMessage(String message) {
		this.handle.field_421_a.sendPacket(new Packet3Chat(message));
	}

	@Override
	public boolean isOp() {
		return this.server.isOp(this.getName());
	}

	@Override
	public void kick(String reason) {
		this.handle.field_421_a.sendPacket(new Packet255KickDisconnect(reason));
		this.handle.field_421_a.func_43_c(reason);
	}

	@Override
	public double getX() {
		return this.handle.posX;
	}

	@Override
	public double getY() {
		return this.handle.posY;
	}

	@Override
	public double getZ() {
		return this.handle.posZ;
	}
}
