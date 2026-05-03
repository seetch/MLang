package me.seetch.mlang;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.extern.java.Log;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Log
@Getter
public class MLang {

    private static final String DEFAULT_LANGUAGE = "en_us";
    private static final String DEFAULT_VERSION = "1.20.4";
    private static final String GITHUB_BASE_URL = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/";

    private static MLang instance;

    private final JavaPlugin plugin;
    private final Gson gson;
    private final Map<String, JsonObject> loadedLanguages;

    private String defaultLanguage;
    private String defaultVersion;

    private MLang(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.loadedLanguages = new HashMap<>();
        this.defaultLanguage = DEFAULT_LANGUAGE;
        this.defaultVersion = detectMinecraftVersion();
        initializeLanguagesDirectory();
    }

    public static synchronized MLang getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new MLang(plugin);
        }
        return instance;
    }

    public void setDefaultLanguage(String languageCode) {
        this.defaultLanguage = languageCode.toLowerCase();
    }

    public void setDefaultVersion(String version) {
        this.defaultVersion = version;
    }

    public CompletableFuture<Boolean> loadLanguageAsync(String languageCode, String version) {
        return CompletableFuture.supplyAsync(() -> loadLanguage(languageCode, version));
    }

    public boolean loadLanguage(String languageCode, String version) {
        languageCode = languageCode.toLowerCase();
        version = version.toLowerCase();

        if (loadedLanguages.containsKey(languageCode)) {
            return true;
        }

        File langFile = resolveLanguageFile(languageCode, version);
        if (langFile == null) {
            return false;
        }

        return parseAndCacheLanguage(languageCode, langFile);
    }

    public CompletableFuture<Boolean> loadDefaultLanguageAsync() {
        return loadLanguageAsync(defaultLanguage, defaultVersion);
    }

    public String getTranslation(String languageCode, String key) {
        JsonObject langJson = loadedLanguages.get(languageCode.toLowerCase());
        if (langJson == null || !langJson.has(key)) {
            if (!languageCode.equals(defaultLanguage)) {
                return getTranslation(defaultLanguage, key);
            }
            return key;
        }
        return langJson.get(key).getAsString();
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
        return loadedLanguages.containsKey(languageCode.toLowerCase());
    }

    public String[] getLoadedLanguages() {
        return loadedLanguages.keySet().toArray(new String[0]);
    }

    private void initializeLanguagesDirectory() {
        File langDir = new File(plugin.getDataFolder(), "languages");
        if (!langDir.exists() && !langDir.mkdirs()) {
            log.warning("Failed to create languages directory");
        }
    }

    private File resolveLanguageFile(String languageCode, String version) {
        File langDir = new File(plugin.getDataFolder(), "languages");
        File langFile = new File(langDir, languageCode + ".json");

        if (!langFile.exists()) {
            String fileUrl = GITHUB_BASE_URL + version + "/assets/minecraft/lang/" + languageCode + ".json";
            if (!downloadLanguageFile(languageCode, fileUrl, langFile)) {
                return null;
            }
        }
        return langFile;
    }

    private boolean downloadLanguageFile(String languageCode, String fileUrl, File saveTo) {
        log.info("Downloading language file: " + languageCode);

        try (BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
             FileOutputStream out = new FileOutputStream(saveTo)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            log.info("Successfully downloaded language file: " + languageCode);
            return true;
        } catch (IOException e) {
            log.severe("Failed to download language file: " + languageCode + " - " + e.getMessage());
            return false;
        }
    }

    private boolean parseAndCacheLanguage(String languageCode, File langFile) {
        try (FileReader reader = new FileReader(langFile)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            if (jsonObject != null) {
                loadedLanguages.put(languageCode, jsonObject);
                log.info("Successfully loaded language: " + languageCode);
                return true;
            }
        } catch (IOException e) {
            log.severe("Failed to parse language file: " + languageCode + " - " + e.getMessage());
        }
        return false;
    }

    private String detectMinecraftVersion() {
        try {
            String version = org.bukkit.Bukkit.getBukkitVersion();
            String[] parts = version.split("-");
            return parts.length > 0 ? parts[0] : DEFAULT_VERSION;
        } catch (Exception e) {
            return DEFAULT_VERSION;
        }
    }
}