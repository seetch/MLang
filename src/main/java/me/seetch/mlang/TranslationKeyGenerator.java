package me.seetch.mlang;

import lombok.experimental.UtilityClass;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class TranslationKeyGenerator {

    private static final String BLOCK_PREFIX = "block.minecraft.";
    private static final String ITEM_PREFIX = "item.minecraft.";
    private static final String EFFECT_PREFIX = "effect.minecraft.";
    private static final String ENCHANTMENT_PREFIX = "enchantment.minecraft.";
    private static final String ENTITY_PREFIX = "entity.minecraft.";
    private static final String CUSTOM_PREFIX = "mlang.";

    public String getMaterialKey(Material material) {
        String prefix = material.isBlock() ? BLOCK_PREFIX : ITEM_PREFIX;
        return prefix + material.name().toLowerCase();
    }

    public String getEffectKey(Effect effect) {
        return EFFECT_PREFIX + effect.name().toLowerCase();
    }

    public String getEnchantmentKey(Enchantment enchantment) {
        NamespacedKey key = enchantment.getKey();
        String prefix = key.getNamespace().equals("minecraft") ? ENCHANTMENT_PREFIX : "enchantment." + key.getNamespace() + ".";
        return prefix + key.getKey();
    }

    public String getEntityKey(EntityType entityType) {
        return ENTITY_PREFIX + entityType.name().toLowerCase();
    }

    public String getItemStackKey(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return BLOCK_PREFIX + "air";
        }

        Material material = itemStack.getType();
        String prefix = material.isBlock() ? BLOCK_PREFIX : ITEM_PREFIX;
        return prefix + material.name().toLowerCase();
    }

    public String getCustomKey(String key) {
        return CUSTOM_PREFIX + key.toLowerCase()
                .replace(' ', '_')
                .replace('-', '_');
    }
}