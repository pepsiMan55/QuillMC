package com.example.hello;

import net.sopepsi.api.JavaPlugin;
import net.sopepsi.api.event.EventHandler;
import net.sopepsi.api.event.Listener;
import net.sopepsi.api.event.player.PlayerJoinEvent;

public class Main extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		registerEvents(this);

		registerCommand("ping", ctx -> {
			ctx.reply("Pong!");
			return true;
		});
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		getLogger().info(event.getPlayer().getName() + " joined the server");
	}
}
