package net.minecraft.src;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

final class ServerWindowAdapter extends WindowAdapter {
	final net.minecraft.server.QuillMinecraftServer mcServer;

	ServerWindowAdapter(net.minecraft.server.QuillMinecraftServer var1) {
		this.mcServer = var1;
	}

	public void windowClosing(WindowEvent var1) {
		this.mcServer.requestStop();

		while(!this.mcServer.shutdownComplete) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException var3) {
				var3.printStackTrace();
			}
		}

		System.exit(0);
	}
}
