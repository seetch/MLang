package me.seetch.mlang;

import lombok.experimental.UtilityClass;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/**
 * Строит ключи переводов в формате ванильных языковых файлов Minecraft,
 * например "block.minecraft.stone". Сам ничего не переводит, только собирает
 * ключ, по которому MLang потом ищет строку в загруженном языковом файле.
 */
@UtilityClass
public class TranslationKeyGenerator {

    private static final String BLOCK_PREFIX = "block.minecraft.";
    private static final String ITEM_PREFIX = "item.minecraft.";
    private static final String EFFECT_PREFIX = "effect.minecraft.";
    private static final String ENCHANTMENT_PREFIX = "enchantment.minecraft.";
    private static final String ENTITY_PREFIX = "entity.minecraft.";
    private static final String CUSTOM_PREFIX = "mlang.";

    /** Возвращает ключ перевода материала. Префикс зависит от того, блок это или предмет. */
    public String getMaterialKey(Material material) {
        String prefix = material.isBlock() ? BLOCK_PREFIX : ITEM_PREFIX;
        return prefix + material.name().toLowerCase(Locale.ROOT);
    }

    /** Возвращает ключ перевода эффекта зелья. */
    public String getEffectKey(Effect effect) {
        return EFFECT_PREFIX + effect.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Возвращает ключ перевода зачарования. Для зачарований из немайнкрафтовского
     * namespace (например добавленных другим плагином) вместо стандартного префикса
     * "enchantment.minecraft." подставляет в префикс реальный namespace зачарования.
     */
    public String getEnchantmentKey(Enchantment enchantment) {
        NamespacedKey key = enchantment.getKey();
        String prefix = NamespacedKey.MINECRAFT.equals(key.getNamespace())
                ? ENCHANTMENT_PREFIX
                : "enchantment." + key.getNamespace() + ".";
        return prefix + key.getKey();
    }

    /** Возвращает ключ перевода типа сущности. */
    public String getEntityKey(EntityType entityType) {
        return ENTITY_PREFIX + entityType.name().toLowerCase(Locale.ROOT);
    }

    /** Возвращает ключ перевода предмета. Для null или воздуха возвращает ключ блока воздуха. */
    public String getItemStackKey(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return BLOCK_PREFIX + "air";
        }
        return getMaterialKey(itemStack.getType());
    }

    /** Строит собственный ключ перевода с префиксом mlang, заменяя пробелы и дефисы на подчёркивания. */
    public String getCustomKey(String key) {
        return CUSTOM_PREFIX + key.toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');
    }
}
