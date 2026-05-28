# Quill Plugin API (Paper-style)

## Create a new plugin

```powershell
java -jar build\libs\quill-server-1.2.6-SNAPSHOT.jar init MyPlugin com.myplugin.dev YourName
cd MyPlugin
# after building quill-api.jar from the server repo:
gradle jar
```

Or from the server repo: `gradlew quillInit -PpluginName=MyPlugin`

## Plugin code

```java
public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        registerEvents(this);
        registerCommand("ping", ctx -> { ctx.reply("Pong!"); return true; });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        getLogger().info(e.getPlayer().getName() + " joined");
    }
}
```

## API types (no `Quill` prefix)

| Paper-like | Quill |
|------------|-------|
| `JavaPlugin` | `net.sopepsi.api.JavaPlugin` |
| `Plugin` | `net.sopepsi.api.Plugin` |
| `Server` | `net.sopepsi.api.Server` |
| `Logger` | `net.sopepsi.api.Logger` |
| `Player` | `net.sopepsi.api.player.Player` |
| `Listener` | `net.sopepsi.api.event.Listener` |
| `@EventHandler` | `net.sopepsi.api.event.EventHandler` |

## Events

- `PlayerJoinEvent`
- `PlayerQuitEvent`
- `PlayerChatEvent` (cancellable; use `setMessage` / `setCancelled`)

## Build plugin

1. `gradlew apiJar` in server repo → `build/libs/quill-api.jar`
2. Add as **compileOnly** in your plugin project
3. Drop built JAR in `plugins/`
