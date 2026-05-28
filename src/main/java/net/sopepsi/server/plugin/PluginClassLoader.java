package net.sopepsi.server.plugin;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Isolates plugin bytecode from {@code net.minecraft.*}. Plugins may only load their own classes,
 * JDK classes, and {@code net.sopepsi.api}.
 */
public final class PluginClassLoader extends URLClassLoader {

	public PluginClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(name.startsWith("net.sopepsi.api")) {
			return getParent().loadClass(name);
		}
		if(name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.")) {
			return getParent().loadClass(name);
		}
		if(name.startsWith("net.minecraft.")) {
			throw new ClassNotFoundException("Plugins cannot access net.minecraft.* — use net.sopepsi.api");
		}
		synchronized(getClassLoadingLock(name)) {
			Class<?> loaded = findLoadedClass(name);
			if(loaded == null) {
				try {
					loaded = findClass(name);
				} catch (ClassNotFoundException e) {
					if(name.startsWith("net.sopepsi.")) {
						return getParent().loadClass(name);
					}
					throw e;
				}
			}
			if(resolve) {
				resolveClass(loaded);
			}
			return loaded;
		}
	}
}
