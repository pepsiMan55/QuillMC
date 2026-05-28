package net.sopepsi.api.player;

/**
 * Online player (API view — no Minecraft types).
 */
public interface Player {

	String getName();

	void sendMessage(String message);

	boolean isOp();

	void kick(String reason);

	double getX();

	double getY();

	double getZ();
}
