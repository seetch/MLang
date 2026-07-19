package me.seetch.mlang;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MLang {

    private static final String DEFAULT_LANGUAGE = "en_us";
    private static final String FALLBACK_VERSION = "1.20.4";
    private static final String GITHUB_BASE_URL = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private static MLang instance;

    @Getter
    private final JavaPlugin plugin;
    @Getter
    private final Gson gson;
    private final Logger logger;
    private final Map<String, JsonObject> loadedLanguages;
    private final HttpClient httpClient;
    private final ExecutorService loaderExecutor;

    @Getter
    private volatile String defaultLanguage;
    @Getter
    private volatile String defaultVersion;

    private MLang(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.logger = plugin.getLogger();
        this.loadedLanguages = new ConcurrentHashMap<>();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.loaderExecutor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("mlang-loader-", 0).factory());
        this.defaultLanguage = DEFAULT_LANGUAGE;
        this.defaultVersion = detectMinecraftVersion();
        initializeLanguagesDirectory();
    }

    public static synchronized MLang getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new MLang(Objects.requireNonNull(plugin, "plugin"));
        }
        return instance;
    }

    public void setDefaultLanguage(String languageCode) {
        Objects.requireNonNull(languageCode, "languageCode");
        this.defaultLanguage = languageCode.toLowerCase(Locale.ROOT);
    }

    public void setDefaultVersion(String version) {
        Objects.requireNonNull(version, "version");
        this.defaultVersion = version;
    }

    public CompletableFuture<Boolean> loadLanguageAsync(String languageCode, String version) {
        return CompletableFuture.supplyAsync(() -> loadLanguage(languageCode, version), loaderExecutor);
    }

    public boolean loadLanguage(String languageCode, String version) {
        Objects.requireNonNull(languageCode, "languageCode");
        Objects.requireNonNull(version, "version");

        String code = languageCode.toLowerCase(Locale.ROOT);
        if (loadedLanguages.containsKey(code)) {
            return true;
        }

        Path langFile = resolveLanguageFile(code, version.toLowerCase(Locale.ROOT));
        if (langFile == null) {
            return false;
        }

        return parseAndCacheLanguage(code, langFile);
    }

    public CompletableFuture<Boolean> loadDefaultLanguageAsync() {
        return loadLanguageAsync(defaultLanguage, defaultVersion);
    }

    public String getTranslation(String languageCode, String key) {
        Objects.requireNonNull(languageCode, "languageCode");
        Objects.requireNonNull(key, "key");

        String code = languageCode.toLowerCase(Locale.ROOT);
        JsonObject langJson = loadedLanguages.get(code);
        if (langJson != null && langJson.has(key)) {
            return langJson.get(key).getAsString();
        }
        if (!code.equals(defaultLanguage)) {
            return getTranslation(defaultLanguage, key);
        }
        return key;
    }

    public String getTranslation(String key) {
        return getTranslation(defaultLanguage, key);
    }

    public String getMaterialTranslation(String languageCode, Material material) {
        return getTranslation(languageCode, TranslationKeyGenerator.getMaterialKey(material));
    }

    public String getMaterialTranslation(Material material) {
        return getMaterialTranslation(defaultLanguage, material);
    }

    public String getEffectTranslation(String languageCode, Effect effect) {
        return getTranslation(languageCode, TranslationKeyGenerator.getEffectKey(effect));
    }

    public String getEffectTranslation(Effect effect) {
        return getEffectTranslation(defaultLanguage, effect);
    }

    public String getEnchantmentTranslation(String languageCode, Enchantment enchantment) {
        return getTranslation(languageCode, TranslationKeyGenerator.getEnchantmentKey(enchantment));
    }

    public String getEnchantmentTranslation(Enchantment enchantment) {
        return getEnchantmentTranslation(defaultLanguage, enchantment);
    }

    public String getEntityTranslation(String languageCode, EntityType entityType) {
        return getTranslation(languageCode, TranslationKeyGenerator.getEntityKey(entityType));
    }

    public String getEntityTranslation(EntityType entityType) {
        return getEntityTranslation(defaultLanguage, entityType);
    }

    public String getItemStackTranslation(String languageCode, ItemStack itemStack) {
        return getTranslation(languageCode, TranslationKeyGenerator.getItemStackKey(itemStack));
    }

    public String getItemStackTranslation(ItemStack itemStack) {
        return getItemStackTranslation(defaultLanguage, itemStack);
    }

    public boolean isLanguageLoaded(String languageCode) {
        Objects.requireNonNull(languageCode, "languageCode");
        return loadedLanguages.containsKey(languageCode.toLowerCase(Locale.ROOT));
    }

    public String[] getLoadedLanguages() {
        return loadedLanguages.keySet().toArray(new String[0]);
    }

    private void initializeLanguagesDirectory() {
        try {
            Files.createDirectories(languagesDirectory());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create languages directory", e);
        }
    }

    private Path languagesDirectory() {
        return plugin.getDataFolder().toPath().resolve("languages");
    }

    private Path resolveLanguageFile(String languageCode, String version) {
        Path langFile = languagesDirectory().resolve(languageCode + ".json");
        if (Files.exists(langFile)) {
            return langFile;
        }
        return downloadLanguageFile(languageCode, version, langFile) ? langFile : null;
    }

    private boolean downloadLanguageFile(String languageCode, String version, Path saveTo) {
        String fileUrl = GITHUB_BASE_URL + version + "/assets/minecraft/lang/" + languageCode + ".json";
        logger.info("Downloading language file: " + languageCode);

        HttpRequest request = HttpRequest.newBuilder(URI.create(fileUrl))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        Path tempFile = null;
        try {
            Files.createDirectories(saveTo.getParent());
            tempFile = Files.createTempFile(saveTo.getParent(), languageCode, ".tmp");

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    logger.severe("Failed to download language file: " + languageCode
                            + " - HTTP " + response.statusCode());
                    return false;
                }
                try (OutputStream out = Files.newOutputStream(tempFile)) {
                    body.transferTo(out);
                }
            }

            // The file appears in place only as a whole, so an interrupted download never leaves broken JSON.
            Files.move(tempFile, saveTo, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Successfully downloaded language file: " + languageCode);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to download language file: " + languageCode, e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Download interrupted: " + languageCode, e);
            return false;
        } finally {
            deleteQuietly(tempFile);
        }
    }

    private boolean parseAndCacheLanguage(String languageCode, Path langFile) {
        try (Reader reader = Files.newBufferedReader(langFile, StandardCharsets.UTF_8)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            if (jsonObject == null) {
                logger.severe("Language file is empty: " + languageCode);
                return false;
            }
            loadedLanguages.put(languageCode, jsonObject);
            logger.info("Successfully loaded language: " + languageCode);
            return true;
        } catch (IOException | JsonParseException e) {
            logger.log(Level.SEVERE, "Failed to parse language file: " + languageCode, e);
            return false;
        }
    }

    private void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }

    private String detectMinecraftVersion() {
        try {
            return Bukkit.getBukkitVersion().split("-", 2)[0];
        } catch (Exception e) {
            return FALLBACK_VERSION;
        }
    }
}
