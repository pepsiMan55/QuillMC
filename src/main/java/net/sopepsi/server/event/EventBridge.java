package net.sopepsi.server.event;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.server.QuillMinecraftServer;
import net.sopepsi.api.Server;
import net.sopepsi.api.event.player.PlayerChatEvent;
import net.sopepsi.api.event.player.PlayerJoinEvent;
import net.sopepsi.api.event.player.PlayerQuitEvent;
import net.sopepsi.api.player.Player;
import net.sopepsi.server.player.PlayerImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Fires API events from Minecraft internals.
 */
public final class EventBridge {

	private static final Map<String, double[]> lastPos = new HashMap<>();

	private EventBridge() {
	}

	public static void firePlayerJoin(QuillMinecraftServer server, EntityPlayerMP entity) {
		Server api = server.getApi();
		if(api == null) {
			return;
		}
		Player player = new PlayerImpl(entity, api);
		api.getEventBus().call(new PlayerJoinEvent(player));
	}

	public static void firePlayerQuit(QuillMinecraftServer server, EntityPlayerMP entity, String quitMessage) {
		Server api = server.getApi();
		if(api == null) {
			return;
		}
		Player player = new PlayerImpl(entity, api);
		api.getEventBus().call(new PlayerQuitEvent(player, quitMessage));
	}

	/** @return false if chat was cancelled */
	public static boolean firePlayerChat(QuillMinecraftServer server, EntityPlayerMP entity, String message) {
		Server api = server.getApi();
		if(api == null) {
			return true;
		}
		Player player = new PlayerImpl(entity, api);
		PlayerChatEvent event = new PlayerChatEvent(player, message);
		api.getEventBus().call(event);
		return !event.isCancelled();
	}

	public static String getChatMessageAfterEvent(QuillMinecraftServer server, EntityPlayerMP entity, String message) {
		Server api = server.getApi();
		if(api == null) {
			return message;
		}
		Player player = new PlayerImpl(entity, api);
		PlayerChatEvent event = new PlayerChatEvent(player, message);
		api.getEventBus().call(event);
		if(event.isCancelled()) {
			return null;
		}
		return event.getMessage();
	}

	public static void firePlayerMove(QuillMinecraftServer server, EntityPlayerMP entity) {

		Server api = server.getApi();

		if(api == null) {
			return;
		}

		Player player = new PlayerImpl(entity, api);

		String key = player.getName().toLowerCase();

		double newX = player.getX();
		double newY = player.getY();
		double newZ = player.getZ();

		double[] last = lastPos.get(key);

		// first movement cache
		if(last == null) {
			lastPos.put(key, new double[]{newX, newY, newZ});
			return;
		}

		double fromX = last[0];
		double fromY = last[1];
		double fromZ = last[2];

		// only fire if player ACTUALLY moved
		if(fromX == newX && fromY == newY && fromZ == newZ) {
			return;
		}

		// update cache
		lastPos.put(key, new double[]{newX, newY, newZ});

		net.sopepsi.api.event.player.PlayerMoveEvent event =
				new net.sopepsi.api.event.player.PlayerMoveEvent(
						player,
						fromX, fromY, fromZ,
						newX, newY, newZ
				);

		try {
			api.getEventBus().call(event);
		} catch(Throwable t) {
			server.log("[Quill] Error while firing PlayerMoveEvent");
			t.printStackTrace();
		}
	}
}
