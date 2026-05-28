package net.minecraft.src;

public final class ThreadServerApplication extends Thread {
	final net.minecraft.server.QuillMinecraftServer mcServer;

	public ThreadServerApplication(String var1, net.minecraft.server.QuillMinecraftServer var2) {
		super(var1);
		this.mcServer = var2;
	}

	public void run() {
		this.mcServer.run();
	}
}
