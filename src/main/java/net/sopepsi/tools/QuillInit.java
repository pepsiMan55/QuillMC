package net.sopepsi.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Scaffolds a new plugin project (Paper-style {@code quill init}).
 *
 * <pre>java -jar quill-server.jar init MyPlugin [package] [author]</pre>
 */
public final class QuillInit {

	private QuillInit() {
	}

	public static void main(String[] args) {
		System.exit(run(args));
	}

	public static int run(String[] args) {
		if(args.length < 2) {
			printUsage();
			return 1;
		}
		String pluginName = args[1];
		String packageName = args.length > 2 ? args[2] : "com.example." + pluginName.toLowerCase().replaceAll("[^a-z0-9]", "");
		String author = args.length > 3 ? args[3] : "YourName";
		File root = new File(pluginName);
		if(root.exists()) {
			System.err.println("Directory already exists: " + root.getAbsolutePath());
			return 1;
		}
		try {
			scaffold(root, pluginName, packageName, author);
			System.out.println("Created plugin project: " + root.getAbsolutePath());
			System.out.println("  cd " + pluginName);
			System.out.println("  Build quill-api.jar first: gradlew apiJar (from server repo)");
			System.out.println("  Then: gradlew jar  and copy build/libs/" + pluginName + ".jar to plugins/");
			return 0;
		} catch (IOException e) {
			System.err.println("Failed to create project: " + e.getMessage());
			e.printStackTrace();
			return 1;
		}
	}

	private static void scaffold(File root, String pluginName, String packageName, String author) throws IOException {
		String pkgPath = packageName.replace('.', '/');
		File srcMain = new File(root, "src/main/java/" + pkgPath);
		File res = new File(root, "src/main/resources");
		srcMain.mkdirs();
		res.mkdirs();

		String mainClass = packageName + ".Main";
		write(new File(srcMain, "Main.java"), mainJava(pluginName, packageName));
		write(new File(res, "quill-plugin.yml"), pluginYml(pluginName, mainClass, author));
		write(new File(root, "build.gradle"), buildGradle(pluginName));
		write(new File(root, "settings.gradle"), "rootProject.name = '" + pluginName + "'\n");
	}

	private static String mainJava(String pluginName, String packageName) {
		return ""
				+ "package " + packageName + ";\n"
				+ "\n"
				+ "import net.sopepsi.api.JavaPlugin;\n"
				+ "import net.sopepsi.api.event.EventHandler;\n"
				+ "import net.sopepsi.api.event.Listener;\n"
				+ "import net.sopepsi.api.event.player.PlayerJoinEvent;\n"
				+ "\n"
				+ "public class Main extends JavaPlugin implements Listener {\n"
				+ "\n"
				+ "\t@Override\n"
				+ "\tpublic void onEnable() {\n"
				+ "\t\tregisterEvents(this);\n"
				+ "\t\tregisterCommand(\"ping\", ctx -> {\n"
				+ "\t\t\tctx.reply(\"Pong!\");\n"
				+ "\t\t\treturn true;\n"
				+ "\t\t});\n"
				+ "\t\tgetLogger().info(\"" + pluginName + " enabled!\");\n"
				+ "\t}\n"
				+ "\n"
				+ "\t@EventHandler\n"
				+ "\tpublic void onJoin(PlayerJoinEvent event) {\n"
				+ "\t\tgetLogger().info(event.getPlayer().getName() + \" joined\");\n"
				+ "\t}\n"
				+ "}\n";
	}

	private static String pluginYml(String name, String main, String author) {
		return ""
				+ "name: " + name + "\n"
				+ "version: 1.0.0\n"
				+ "main: " + main + "\n"
				+ "author: " + author + "\n"
				+ "description: A Quill plugin\n";
	}

	private static String buildGradle(String pluginName) {
		return ""
				+ "plugins {\n"
				+ "    id 'java'\n"
				+ "}\n"
				+ "\n"
				+ "group = 'com.example'\n"
				+ "version = '1.0.0'\n"
				+ "\n"
				+ "repositories {\n"
				+ "    mavenCentral()\n"
				+ "    flatDir { dirs '../build/libs' }\n"
				+ "}\n"
				+ "\n"
				+ "dependencies {\n"
				+ "    compileOnly files('../build/libs/quill-api.jar')\n"
				+ "}\n"
				+ "\n"
				+ "jar {\n"
				+ "    archiveBaseName = '" + pluginName + "'\n"
				+ "    from('src/main/resources') { include 'quill-plugin.yml' }\n"
				+ "}\n";
	}

	private static void write(File file, String content) throws IOException {
		file.getParentFile().mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(file));
		try {
			out.print(content);
		} finally {
			out.close();
		}
	}

	private static void printUsage() {
		System.out.println("Quill plugin scaffold");
		System.out.println("Usage: java -jar quill-server.jar init <PluginName> [package] [author]");
		System.out.println("Example: java -jar quill-server.jar init MyPlugin com.myplugin.dev Steve");
	}
}
