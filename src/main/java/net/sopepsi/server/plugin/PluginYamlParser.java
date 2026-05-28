package net.sopepsi.server.plugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sopepsi.api.plugin.PermissionDefault;
import net.sopepsi.api.plugin.PluginCommandMeta;
import net.sopepsi.api.plugin.PluginDescription;
import net.sopepsi.api.plugin.PluginPermissionMeta;

public final class PluginYamlParser {

	private static final int INDENT_TOP = 0;
	private static final int INDENT_SECTION_ITEM = 2;
	private static final int INDENT_ITEM_PROP = 4;

	private PluginYamlParser() {
	}

	public static PluginDescription parse(InputStream input) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		String name = null;
		String version = "1.0";
		String main = null;
		String description = "";
		List<String> authors = new ArrayList<String>();
		List<String> depend = new ArrayList<String>();
		List<String> softDepend = new ArrayList<String>();
		Map<String, PluginCommandMeta> commands = new LinkedHashMap<String, PluginCommandMeta>();
		Map<String, PluginPermissionMeta> permissions = new LinkedHashMap<String, PluginPermissionMeta>();

		String section = null;
		String currentItem = null;
		String listKey = null;
		String commandDescription = "";
		String commandUsage = null;
		String commandPermission = null;
		List<String> commandAliases = new ArrayList<String>();
		String permissionDescription = "";
		PermissionDefault permissionDefault = PermissionDefault.OP;

		String line;
		while((line = reader.readLine()) != null) {
			if(line.trim().length() == 0 || line.trim().startsWith("#")) {
				continue;
			}

			int indent = countIndent(line);
			String content = line.trim();

			if(content.startsWith("- ") && listKey != null) {
				String item = unquote(content.substring(2).trim());
				if("authors".equals(listKey)) {
					authors.add(item);
				} else if("depend".equals(listKey)) {
					depend.add(item);
				} else if("softdepend".equals(listKey)) {
					softDepend.add(item);
				} else if("aliases".equals(listKey)) {
					commandAliases.add(item);
				}
				continue;
			}

			if(indent <= INDENT_TOP) {
				flushCommand(commands, currentItem, commandDescription, commandUsage, commandPermission, commandAliases);
				flushPermission(permissions, currentItem, permissionDescription, permissionDefault, section);
				currentItem = null;
				listKey = null;
			}

			if(content.startsWith("- ")) {
				continue;
			}

			int colon = content.indexOf(':');
			if(colon < 0) {
				continue;
			}
			String key = content.substring(0, colon).trim();
			String value = content.substring(colon + 1).trim();
			String keyLower = key.toLowerCase(Locale.ROOT);

			if(indent <= INDENT_TOP) {
				if("name".equals(keyLower)) {
					name = unquote(value);
				} else if("version".equals(keyLower)) {
					version = unquote(value);
				} else if("main".equals(keyLower)) {
					main = unquote(value);
				} else if("description".equals(keyLower)) {
					description = unquote(value);
				} else if("author".equals(keyLower)) {
					authors.add(unquote(value));
				} else if("authors".equals(keyLower)) {
					if(value.length() > 0) {
						for(String part : value.split(",")) {
							authors.add(unquote(part.trim()));
						}
					} else {
						listKey = "authors";
					}
				} else if("depend".equals(keyLower)) {
					if(value.length() > 0) {
						for(String part : value.split(",")) {
							depend.add(part.trim());
						}
					} else {
						listKey = "depend";
					}
				} else if("softdepend".equals(keyLower)) {
					if(value.length() > 0) {
						for(String part : value.split(",")) {
							softDepend.add(part.trim());
						}
					} else {
						listKey = "softdepend";
					}
				} else if("commands".equals(keyLower)) {
					section = "commands";
				} else if("permissions".equals(keyLower)) {
					section = "permissions";
				} else {
					section = null;
				}
				continue;
			}

			if("commands".equals(section) && indent == INDENT_SECTION_ITEM) {
				flushCommand(commands, currentItem, commandDescription, commandUsage, commandPermission, commandAliases);
				flushPermission(permissions, currentItem, permissionDescription, permissionDefault, section);
				currentItem = keyLower;
				commandDescription = "";
				commandUsage = null;
				commandPermission = null;
				commandAliases = new ArrayList<String>();
				if(value.length() > 0) {
					commandDescription = unquote(value);
				}
				continue;
			}

			if("permissions".equals(section) && indent == INDENT_SECTION_ITEM) {
				flushCommand(commands, currentItem, commandDescription, commandUsage, commandPermission, commandAliases);
				flushPermission(permissions, currentItem, permissionDescription, permissionDefault, section);
				currentItem = key;
				permissionDescription = "";
				permissionDefault = PermissionDefault.OP;
				if(value.length() > 0) {
					permissionDescription = unquote(value);
				}
				continue;
			}

			if("commands".equals(section) && indent >= INDENT_ITEM_PROP && currentItem != null) {
				if("description".equals(keyLower)) {
					commandDescription = unquote(value);
				} else if("usage".equals(keyLower)) {
					commandUsage = unquote(value);
				} else if("permission".equals(keyLower)) {
					commandPermission = unquote(value);
				} else if("aliases".equals(keyLower)) {
					if(value.length() > 0) {
						parseInlineList(value, commandAliases);
					} else {
						listKey = "aliases";
					}
				}
				continue;
			}

			if("permissions".equals(section) && indent >= INDENT_ITEM_PROP && currentItem != null) {
				if("description".equals(keyLower)) {
					permissionDescription = unquote(value);
				} else if("default".equals(keyLower)) {
					permissionDefault = PermissionDefault.parse(unquote(value));
				}
			}
		}

		flushCommand(commands, currentItem, commandDescription, commandUsage, commandPermission, commandAliases);
		flushPermission(permissions, currentItem, permissionDescription, permissionDefault, section);

		if(name == null || main == null) {
			throw new IllegalArgumentException("quill-plugin.yml requires name and main");
		}
		return new PluginDescription(name, version, main, description, authors, depend, softDepend, commands, permissions);
	}

	private static void flushCommand(Map<String, PluginCommandMeta> commands, String name,
			String description, String usage, String permission, List<String> aliases) {
		if(name == null || name.length() == 0) {
			return;
		}
		String key = name.toLowerCase(Locale.ROOT);
		commands.put(key, new PluginCommandMeta(key, description, usage, permission, aliases));
	}

	private static void flushPermission(Map<String, PluginPermissionMeta> permissions, String name,
			String description, PermissionDefault defaultPermission, String section) {
		if(!"permissions".equals(section) || name == null || name.length() == 0) {
			return;
		}
		permissions.put(name.toLowerCase(Locale.ROOT), new PluginPermissionMeta(name, description, defaultPermission));
	}

	private static void parseInlineList(String value, List<String> out) {
		String trimmed = value.trim();
		if(trimmed.startsWith("[") && trimmed.endsWith("]")) {
			trimmed = trimmed.substring(1, trimmed.length() - 1);
		}
		if(trimmed.length() == 0) {
			return;
		}
		for(String part : trimmed.split(",")) {
			String item = unquote(part.trim());
			if(item.length() > 0) {
				out.add(item.toLowerCase(Locale.ROOT));
			}
		}
	}

	private static int countIndent(String line) {
		int count = 0;
		while(count < line.length() && line.charAt(count) == ' ') {
			++count;
		}
		return count;
	}

	private static String unquote(String value) {
		if(value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}
}
