package net.minecraft.src;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleLogManager {
	public static Logger logger = Logger.getLogger("Minecraft");

	public static void init() {
		ConsoleLogFormatter formatter = new ConsoleLogFormatter();
		logger.setUseParentHandlers(false);
		ConsoleHandler console = new ConsoleHandler();
		console.setFormatter(formatter);
		logger.addHandler(console);

		try {
			FileHandler file = new FileHandler("server.log", true);
			file.setFormatter(formatter);
			logger.addHandler(file);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to log to server.log", e);
		}

		Logger quill = Logger.getLogger("Quill");
		quill.setUseParentHandlers(false);
		for(int i = 0; i < logger.getHandlers().length; ++i) {
			quill.addHandler(logger.getHandlers()[i]);
		}
	}
}
