# MLang - Minecraft Language Library

**MLang** - Powerful and easy-to-use library for working with Minecraft localization. Get user-friendly translations for items, effects, enchantments, and other game elements.

## Features

- Automatic downloading of language files from GitHub
- Asynchronous operations for non-blocking server work
- Caching of loaded translations for high performance
- Multi-language support (en_us, ru_ru, es_es, de_de, etc.)
- Fallback system - automatic fallback to default language
- Full coverage - materials, effects, enchantments, entities, ItemStack
- Simple API - just a few lines of code to get started

## Requirements

- Java 21+
- PaperMC/Spigot 1.20+

## Installation

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
maven {
    url "https://repo.seetch.ru/releases"
}

implementation 'me.seetch:mlang:1.1.0'
```

### Manual

Build the JAR file add it to your project.

## Quick Start

```java
import me.seetch.mlang.MLang;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize MLang (Singleton pattern)
        MLang lang = MLang.getInstance(this);

        // Set default language (e.g., Russian)
        lang.setDefaultLanguage("ru_ru");

        // Set Minecraft version (for finding correct translations)
        lang.setDefaultVersion("1.20.4");

        // Load language asynchronously (doesn't block the server)
        lang.loadDefaultLanguageAsync().thenAccept(success -> {
            if (success) {
                getLogger().info("Language loaded successfully!");
            }
        });
    }

    public void exampleUsage() {
        MLang lang = MLang.getInstance(this);

        // Get item translation in Russian
        String translation = lang.getMaterialTranslation("ru_ru", Material.DIAMOND_SWORD);
        // Result: "Алмазный меч"

        // Use default language (ru_ru)
        String defaultTranslation = lang.getMaterialTranslation(Material.STONE);
        // Result: "Камень"
    }
}
```

## API Documentation

### Initialization

```java
// Get MLang instance (singleton)
// plugin - your plugin extending JavaPlugin
MLang lang = MLang.getInstance(plugin);
```

### Configuration

```java
// Set default language (used when language is not specified)
// Format: language code in lowercase (en_us, ru_ru, de_de...)
lang.setDefaultLanguage("en_us");

// Set Minecraft version (for loading correct translations)
// Format: version without build number (1.20.4, 1.19.2...)
lang.setDefaultVersion("1.20.4");
```

### Loading Languages

```java
// Asynchronous loading (recommended)
// Returns CompletableFuture<Boolean>
lang.loadLanguageAsync("ru_ru", "1.20.4")
    .thenAccept(success -> {
        if (success) {
            getLogger().info("Russian language loaded!");
        }
    });

// Synchronous loading (blocks thread)
// Use only in async tasks!
boolean success = lang.loadLanguage("es_es", "1.20.4");

// Load default language (ru_ru + 1.20.4)
lang.loadDefaultLanguageAsync();
```

### Getting Translations

```java
// Materials (blocks and items)
String material = lang.getMaterialTranslation("ru_ru", Material.DIAMOND);
// "Алмаз"

String block = lang.getMaterialTranslation("en_us", Material.STONE);
// "Stone"

// Effects
String effect = lang.getEffectTranslation("en_us", Effect.SPEED);
// "Speed"

// Enchantments
String enchantment = lang.getEnchantmentTranslation("de_de", Enchantment.SHARPNESS);
// "Schärfe"

// Entity types
String entity = lang.getEntityTranslation("fr_fr", EntityType.ZOMBIE);
// "Zombie"

// ItemStack (with metadata support)
String item = lang.getItemStackTranslation("es_es", itemStack);
// "Espada de diamante"

// Direct key access (for custom keys)
String custom = lang.getTranslation("ru_ru", "block.minecraft.stone");
// "Камень"
```

### Utilities

```java
// Generate translation key for material
// Useful for creating custom language files
String key = TranslationKeyGenerator.getMaterialKey(Material.STONE);
// "block.minecraft.stone"

// Check if language is loaded
boolean isLoaded = lang.isLanguageLoaded("ru_ru");

// Get all loaded languages
String[] languages = lang.getLoadedLanguages();
```

## Supported Languages

MLang supports all official Minecraft languages:
- en_us - English (United States)
- ru_ru - Русский
- es_es - Español (España)
- de_de - Deutsch (Deutschland)
- fr_fr - Français (France)
- zh_cn - 简体中文
- ja_jp - 日本語
- And many others...

## File Structure

MLang automatically creates a `languages` folder in your plugin directory:

```
plugins/
└── YourPlugin/
    └── languages/       # downloaded language files
        ├── en_us.json
        ├── ru_ru.json
        └── ...
```

## Integration Examples

### Getting Item Display Name for Player

```java
public String getItemDisplayName(ItemStack item, String playerLanguage) {
    MLang lang = MLang.getInstance(yourPlugin);

    // Get item name translation
    String name = lang.getItemStackTranslation(playerLanguage, item);

    // Add visual effects for enchanted items
    if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
        name = "§a" + name + " §7(Enchanted)";
    }

    return name;
}
```

### Getting Entity Name

```java
public String getEntityName(EntityType type, String language) {
    // Simple call - uses default language
    return MLang.getInstance(yourPlugin).getEntityTranslation(language, type);
}
```

## Performance

- **Caching**: All loaded languages are stored in memory
- **Lazy loading**: Files are downloaded only when needed
- **Asynchronous**: Loading doesn't block the main server thread
- **Memory optimization**: Efficient resource usage

## Error Handling

```java
lang.loadLanguageAsync("invalid_lang", "1.20.4")
    .exceptionally(throwable -> {
        // This method is called on loading error
        getLogger().warning("Failed to load language: " + throwable.getMessage());
        return false;
    })
    .thenAccept(success -> {
        // This method is called on success OR after error
        if (!success) {
            getLogger().warning("Language was not loaded");
        }
    });
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [InventivetalentDev](https://github.com/InventivetalentDev) for minecraft-assets
- Bukkit/Spigot/Paper community for the excellent platform

## Support

If you have questions or suggestions, create an [Issue](https://github.com/seetch/MLang/issues) on GitHub.