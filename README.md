# Quill [![Version](https://img.shields.io/badge/version-1.2.6--2-344ceb)](https://craftmen.uk/quill-api) [![Java](https://img.shields.io/badge/java-8-orange)](https://adoptium.net/) [![License](https://img.shields.io/github/license/pepsiMan55/QuillMC)](https://github.com/pepsiMan55/QuillMC)

---

## The only Minecraft server optimized for plugins on old Minecraft versions.

Quill is a high-performance Minecraft server software built specifically for legacy Minecraft versions.

Unlike modern server software focused on the newest releases, Quill is designed to bring:

* Modern plugin development
* Better server performance
* Expanded APIs

…to old Minecraft versions like **Alpha**, **Beta**, and early release builds.

Quill provides a Paper-like development experience while remaining optimized for classic Minecraft gameplay and protocol behavior.

**Quill was built off of minecrafts obfuscated server software, but I did all the dirty work and deobfuscated some of the stuff**

---

# Features

* Plugin API designed for legacy Minecraft
* Better threading and scheduler systems
* Event-driven plugin architecture
* Gradle + Maven dependency support
* Lightweight API for plugin developers
* Built for Java 8 compatibility
* Faster than traditional old-version server software
* Supports large plugin ecosystems on old versions

---

# Support and Community

* Website: https://craftmen.uk
* API Repository: https://craftmen.uk/quill-api
* GitHub: https://github.com/pepsiMan55/QuillMC

---

# How To (Server Admins)

Quill runs like a normal Minecraft server jar.

## Download

Download the latest build from:

```text
https://craftmen.uk
```

---

## Run the Server

```bash
java -Xms1G -Xmx1G -jar quill-1.2.6.jar nogui
```

---

## Plugin Folder

Plugins go inside:

```text
plugins/
```

Restart the server after adding or updating plugins.

---

# How To (Plugin Developers)

Quill provides a full plugin API similar to Paper/Bukkit. (Not exactly just more watered down to support old versions.)

Plugin developers do **not** need to manually download API jars.

---

## Gradle

### `build.gradle`

```gradle
group = 'net.sopepsi'
version = '1.2.6-3'

configurations {
    quill
}

dependencies {
    quill files('libs/quill-api-0.0.2.jar')
}

tasks.register("downloadQuill") {
    doLast {
        def url = "https://github.com/pepsiMan55/QuillMC/releases/download/a1.2.6-3/quill-api-0.0.2.jar"
        def file = file("libs/quill-api-0.0.2.jar")

        if (!file.exists()) {
            file.parentFile.mkdirs()
            new URL(url).withInputStream { i ->
                file.withOutputStream { it << i }
            }
            println "Downloaded Quill API"
        }
    }
}
```

---

## Kotlin DSL

```kotlin
group = 'net.sopepsi'
version = '1.2.6-3'

configurations {
    quill
}

dependencies {
    quill files('libs/quill-api-0.0.2.jar')
}

tasks.register("downloadQuill") {
    doLast {
        def url = "https://github.com/pepsiMan55/QuillMC/releases/download/a1.2.6-3/quill-api-0.0.2.jar"
        def file = file("libs/quill-api-0.0.2.jar")

        if (!file.exists()) {
            file.parentFile.mkdirs()
            new URL(url).withInputStream { i ->
                file.withOutputStream { it << i }
            }
            println "Downloaded Quill API"
        }
    }
}
```

---

## Maven

```xml
<repository>
    <id>quill</id>
    <url>https://github.com/pepsiMan55/QuillMC/releases/download/a1.2.6-2/quill-api-0.0.1.jar</url>
</repository>
```

```xml
<dependency>
    <groupId>net.sopepsi</groupId>
    <artifactId>quill-api</artifactId>
    <version>1.2.6-1</version>
    <scope>provided</scope>
</dependency>
```

---

# Example Plugin

## `Main.java`

```java
package com.example.myplugin;

import net.sopepsi.api.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Hello Quill!");
    }

    @Override
    protected void onDisablePlugin() {
        getLogger().info("Plugin disabled.");
    }
}
```

---

## `quill-plugin.yml`

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.myplugin.Main
author: YourName
```

---

# Building Plugins

Build using Gradle:

```bash
./gradlew build
```

Compiled jars will appear in:

```text
build/libs/
```

Copy the plugin jar into your server's:

```text
plugins/
```

directory and restart the server.

---

# Compiling Quill From Source

## Requirements

* Java 8
* Git
* Internet connection

---

## Clone Repository

```bash
git clone https://github.com/pepsiMan55/QuillMC.git
```

---

## Build

```bash
./gradlew build
```

Compiled jars will appear inside:

```text
build/libs/
```

---

# Why Quill?

Modern Minecraft server software focuses almost entirely on old versions.

Old Minecraft servers are usually left with:

* Poor optimization
* Outdated APIs
* Limited plugin support
* Difficult development workflows

Quill changes that.

Quill brings modern server software concepts to old Minecraft versions while preserving the original gameplay experience.

---

# Goals

* Make old Minecraft server development modern again
* Provide stable plugin APIs for legacy versions
* Improve performance without changing gameplay
* Make plugin development simple and accessible
* Create a long-term ecosystem for classic Minecraft servers

---

# License

See `LICENSE` for details.
