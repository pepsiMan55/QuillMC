package net.sopepsi.api;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Plugin logger - writes to the server console and {@code server.log} (Paper-style).
 */
public final class Logger {

	private final java.util.logging.Logger delegate;
	private final String prefix;

	public Logger(String pluginName) {
		this.delegate = java.util.logging.Logger.getLogger("Quill/" + pluginName);
		this.prefix = "[" + pluginName + "] ";
		wireToServerHandlers();
	}

	private void wireToServerHandlers() {
		java.util.logging.Logger serverLogger = java.util.logging.Logger.getLogger("Minecraft");
		if(serverLogger.getHandlers().length == 0) {
			serverLogger = java.util.logging.Logger.getLogger("Quill");
		}
		for(Handler handler : serverLogger.getHandlers()) {
			boolean already = false;
			for(Handler existing : this.delegate.getHandlers()) {
				if(existing == handler) {
					already = true;
					break;
				}
			}
			if(!already) {
				this.delegate.addHandler(handler);
			}
		}
		this.delegate.setUseParentHandlers(false);
	}

	public void info(String message) {
		this.delegate.info(this.prefix + message);
	}

	public void warning(String message) {
		this.delegate.warning(this.prefix + message);
	}

	public void severe(String message) {
		this.delegate.severe(this.prefix + message);
	}

	public void log(Level level, String message) {
		this.delegate.log(level, this.prefix + message);
	}

	public void log(Level level, String message, Throwable thrown) {
		this.delegate.log(level, this.prefix + message, thrown);
	}
}
