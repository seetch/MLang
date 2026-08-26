**English** | [Русский](README.ru.md)

# mlang

Minecraft language library for Bukkit/Spigot/Paper plugins: user-friendly
translations for materials, effects, enchantments, entities and items, without
having to ship or maintain your own language files.

- Automatic downloading of language files from GitHub (InventivetalentDev/minecraft-assets)
- Asynchronous loading on virtual threads, non-blocking for the server
- In-memory caching of loaded translations, plus an on-disk cache in the plugin's data folder
- Multi-language support (en_us, ru_ru, es_es, de_de and every other official Minecraft language)
- Fallback to the default language when a key or a language is missing
- Simple API: a couple of lines to get started

## Requirements

- Java 21+
- PaperMC/Spigot 1.20+

## Pulling the library from repo.seetch.ru

Published to `https://repo.seetch.ru/releases`.

### Maven

```xml
<repository>
    <id>seetch-repo-releases</id>
    <url>https://repo.seetch.ru/releases</url>
</repository>

<dependency>
    <groupId>me.seetch</groupId>
    <artifactId>mlang</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url "https://repo.seetch.ru/releases" }
}

dependencies {
    implementation 'me.seetch:mlang:1.1.0'
}
```

### Javadoc

Reposilite renders it in the browser at
`https://repo.seetch.ru/javadoc/releases/me/seetch/mlang/<version>/`, e.g.
`https://repo.seetch.ru/javadoc/releases/me/seetch/mlang/1.1.0/`.

## Quick start

```java
import me.seetch.mlang.MLang;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        MLang lang = MLang.getInstance(this);
        lang.setDefaultLanguage("ru_ru");
        lang.setDefaultVersion("1.20.4");

        lang.loadDefaultLanguageAsync().thenAccept(success -> {
            if (success) {
                getLogger().info("Language loaded successfully!");
            }
        });
    }

    public void exampleUsage() {
        MLang lang = MLang.getInstance(this);

        String translation = lang.getMaterialTranslation("ru_ru", Material.DIAMOND_SWORD);
        // "Алмазный меч"

        String defaultTranslation = lang.getMaterialTranslation(Material.STONE);
        // uses whatever setDefaultLanguage() was set to
    }
}
```

One instance per JVM, not per plugin: the first call to `getInstance` binds the
data folder and logger it uses. If several plugins on the same server call
`getInstance` with different plugin instances, they all share the one bound to
whichever plugin called it first.

## API

### Initialization and configuration

```java
MLang lang = MLang.getInstance(plugin);

lang.setDefaultLanguage("en_us");   // lowercase language code, e.g. en_us, ru_ru, de_de
lang.setDefaultVersion("1.20.4");   // Minecraft version without the build suffix
```

### Loading languages

```java
// Async, recommended: runs on a virtual thread, safe to call from the main thread
lang.loadLanguageAsync("ru_ru", "1.20.4")
    .thenAccept(success -> { /* ... */ });

// Sync, blocking: only call from an async context yourself
boolean success = lang.loadLanguage("es_es", "1.20.4");

lang.loadDefaultLanguageAsync();
```

### Getting translations

```java
String material = lang.getMaterialTranslation("ru_ru", Material.DIAMOND);              // "Алмаз"
String effect = lang.getEffectTranslation("en_us", Effect.SPEED);                      // "Speed"
String enchantment = lang.getEnchantmentTranslation("de_de", Enchantment.SHARPNESS);   // "Schärfe"
String entity = lang.getEntityTranslation("fr_fr", EntityType.ZOMBIE);                 // "Zombie"
String item = lang.getItemStackTranslation("es_es", itemStack);
String custom = lang.getTranslation("ru_ru", "block.minecraft.stone");                 // "Камень"
```

### Utilities

```java
String key = TranslationKeyGenerator.getMaterialKey(Material.STONE); // "block.minecraft.stone"

boolean isLoaded = lang.isLanguageLoaded("ru_ru");
String[] languages = lang.getLoadedLanguages();
```

## File layout

mlang creates a `languages` folder in the plugin's data folder and stores
downloaded language files there:

```
plugins/
└── YourPlugin/
    └── languages/
        ├── en_us.json
        ├── ru_ru.json
        └── ...
```

## License

[MIT](LICENSE)

## Acknowledgments

- [InventivetalentDev](https://github.com/InventivetalentDev) for minecraft-assets
- Bukkit/Spigot/Paper community for the platform

## Support

Questions or suggestions: open an [Issue](https://github.com/seetch/mlang/issues).
