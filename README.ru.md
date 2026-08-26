[English](README.md) | **Русский**

# mlang

Библиотека переводов Minecraft для плагинов на Bukkit/Spigot/Paper: удобочитаемые
переводы материалов, эффектов, зачарований, сущностей и предметов, без
необходимости самому собирать и обновлять языковые файлы.

- Автоматическая загрузка языковых файлов с GitHub (InventivetalentDev/minecraft-assets)
- Асинхронная загрузка на виртуальных потоках, не блокирует сервер
- Кэширование переводов в памяти, плюс кэш на диске в папке данных плагина
- Поддержка всех официальных языков Minecraft (en_us, ru_ru, es_es, de_de и другие)
- Откат на язык по умолчанию, если ключ или язык не найден
- Простой API: пара строк для старта

## Требования

- Java 21+
- PaperMC/Spigot 1.20+

## Откуда тянуть библиотеку (repo.seetch.ru)

Публикуется в `https://repo.seetch.ru/releases`.

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

Reposilite рендерит его в браузере по адресу
`https://repo.seetch.ru/javadoc/releases/me/seetch/mlang/<версия>/`, например
`https://repo.seetch.ru/javadoc/releases/me/seetch/mlang/1.1.0/`.

## Быстрый старт

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
        // использует то, что задано в setDefaultLanguage()
    }
}
```

Экземпляр один на JVM, а не на плагин: первый вызов getInstance привязывает
используемую папку данных и логгер. Если несколько плагинов на одном сервере
вызывают getInstance с разными экземплярами плагина, все они получат один и тот
же объект, привязанный к тому плагину, который вызвал первым.

## API

### Инициализация и настройка

```java
MLang lang = MLang.getInstance(plugin);

lang.setDefaultLanguage("en_us");   // код языка в нижнем регистре, например en_us, ru_ru, de_de
lang.setDefaultVersion("1.20.4");   // версия Minecraft без номера сборки
```

### Загрузка языков

```java
// Асинхронно, рекомендуемый способ: выполняется на виртуальном потоке,
// безопасно вызывать из главного потока
lang.loadLanguageAsync("ru_ru", "1.20.4")
    .thenAccept(success -> { /* ... */ });

// Синхронно, блокирующий вызов: вызывать только из своего асинхронного контекста
boolean success = lang.loadLanguage("es_es", "1.20.4");

lang.loadDefaultLanguageAsync();
```

### Получение переводов

```java
String material = lang.getMaterialTranslation("ru_ru", Material.DIAMOND);              // "Алмаз"
String effect = lang.getEffectTranslation("en_us", Effect.SPEED);                      // "Speed"
String enchantment = lang.getEnchantmentTranslation("de_de", Enchantment.SHARPNESS);   // "Schärfe"
String entity = lang.getEntityTranslation("fr_fr", EntityType.ZOMBIE);                 // "Zombie"
String item = lang.getItemStackTranslation("es_es", itemStack);
String custom = lang.getTranslation("ru_ru", "block.minecraft.stone");                 // "Камень"
```

### Утилиты

```java
String key = TranslationKeyGenerator.getMaterialKey(Material.STONE); // "block.minecraft.stone"

boolean isLoaded = lang.isLanguageLoaded("ru_ru");
String[] languages = lang.getLoadedLanguages();
```

## Структура файлов

mlang создаёт папку `languages` в папке данных плагина и хранит там скачанные
языковые файлы:

```
plugins/
└── YourPlugin/
    └── languages/
        ├── en_us.json
        ├── ru_ru.json
        └── ...
```

## Лицензия

[MIT](LICENSE)

## Благодарности

- [InventivetalentDev](https://github.com/InventivetalentDev) за minecraft-assets
- Сообществу Bukkit/Spigot/Paper за платформу

## Поддержка

Вопросы и предложения: [Issue](https://github.com/seetch/mlang/issues) на GitHub.
