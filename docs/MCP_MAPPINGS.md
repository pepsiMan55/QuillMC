# MCP-Style Mapping Reference (Alpha 1.2.6 / Quill)

Quill keeps legacy `net.minecraft.src` packages on disk while renaming server-facing symbols to readable names. Use this table when reading or patching decompiled code.

## Server (`QuillMinecraftServer`)

| Obfuscated | MCP / Quill name |
|------------|------------------|
| `field_6032_g` | `shutdownComplete` |
| `field_6028_k` | `entityTracker` (alias deprecated) |
| `field_6036_c` | `networkListenThread` (alias deprecated) |
| `field_9011_n` | `spawnAnimals` (alias deprecated) |
| `func_6015_a` | `isServerRunning` |
| `func_6016_a` | `requestStop` |
| `func_6022_a` | `addTickListener` |

## `ServerConfigurationManager`

| Obfuscated | MCP / Quill name |
|------------|------------------|
| `field_9252_f` | `bannedPlayers` |
| `func_640_a` | `getViewDistance` (player manager) |
| `func_637_b` | `tickPlayerManager` |
| `func_613_b` | `updatePlayer` |
| `func_9242_d` | `respawnPlayer` |

## `World` / `WorldServer`

| Obfuscated | MCP / Quill name |
|------------|------------------|
| `func_485_a` | `saveChunks` |
| `func_6156_d` | `doTick` (chunk unload queue) |
| `func_459_b` | `tickBlocks` |
| `func_4072_a` | `setWorldAccess` |
| `field_797_s` | `saveDirectory` |
| `.A` | `chunkProvider` (obfuscated field) |

## Networking

| Obfuscated | MCP / Quill name |
|------------|------------------|
| `func_715_a` | `networkTick` |
| `func_4010_d` | `handleChatCommand` |
| `field_421_a` | `netHandler` |

## Entity

| Obfuscated | MCP / Quill name |
|------------|------------------|
| `field_331_c` | `entityId` |
| `func_12016_d` | `removePlayer` |
| `func_12014_e` | `removeEntityForRespawn` |

## Commands & properties

Built-in commands live in `net.sopepsi.server.BuiltInCommands`. Plugins use only `net.sopepsi.api.*`.

Extended `server.properties` keys are defined in `net.sopepsi.api.ServerProperties`.

## Full remap workflow

1. Prioritize classes touched by `QuillMinecraftServer`, plugins, and commands.
2. Apply MCP/Reclass Alpha 1.2.6 names where available.
3. Add `@Deprecated` aliases on the server class for any renamed public fields still referenced in `net.minecraft.src`.
4. Never expose `net.minecraft.*` in `net.sopepsi.api`.
