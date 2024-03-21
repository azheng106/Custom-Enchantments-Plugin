package me.aquaponics.customenchantments.enchants;

import me.aquaponics.customenchantments.commands.EnchantCommandExecutor;
import me.aquaponics.customenchantments.utils.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static me.aquaponics.customenchantments.CustomEnchantments.maxLevels;

public class AnvilHandler implements Listener {
    /**
     * Allow the custom enchants to be anviled together
     */
    private final Plugin plugin;

    public AnvilHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent e) {
        ItemStack[] items = e.getInventory().getContents();
        if (items[0] == null || items[1] == null) return;
        if (items[0].getType() != items[1].getType()) return;

        ItemMeta meta1 = items[0].getItemMeta();
        ItemMeta meta2 = items[1].getItemMeta();
        ItemStack result = e.getResult();
        if (result == null || result.getType() == Material.AIR) {
            result = items[0].clone();
        }
        ItemMeta resultMeta = result.getItemMeta();

        // Handle custom enchants
        for (EnchantList enchant : EnchantList.values()) {
            String enchantString = enchant.toString().toLowerCase();
            NamespacedKey key = new NamespacedKey(plugin, enchantString);
            int level1 = (meta1 != null ? meta1.getPersistentDataContainer().get(key, PersistentDataType.INTEGER) : null) == null ? 0 : meta1.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
            int level2 = (meta2 != null ? meta2.getPersistentDataContainer().get(key, PersistentDataType.INTEGER) : null) == null ? 0 : meta2.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
            int resultLevel = Math.min(level1 + level2, maxLevels.get(enchant));
            if (resultMeta != null) {
                resultMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, resultLevel);
            }
            if (resultLevel > 0) {
                List<String> lore = ((resultMeta != null ? resultMeta.getLore() : null) == null) ? new ArrayList<>() : resultMeta.getLore();
                lore.removeIf(string -> string.contains(EnchantCommandExecutor.normalizeName(enchantString)));
                lore.add(ChatColor.GRAY + EnchantCommandExecutor.normalizeName(enchantString) + " " + NumberUtils.toRoman(resultLevel));
                resultMeta.setLore(lore);
            }
        }

        result.setItemMeta(resultMeta);
        e.setResult(result);
    }
}
