package net.minecraft.src;

public class ThreadSleepForever extends Thread {
	final net.minecraft.server.QuillMinecraftServer mc;

	public ThreadSleepForever(net.minecraft.server.QuillMinecraftServer var1) {
		this.mc = var1;
		this.setDaemon(true);
		this.start();
	}

	public void run() {
		while(true) {
			try {
				Thread.sleep(2147483647L);
			} catch (InterruptedException var2) {
			}
		}
	}
}
