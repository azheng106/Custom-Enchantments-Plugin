package me.aquaponics.customenchantments.enchants.melee;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class Lifesteal implements Listener {
    private final Plugin plugin;
    
    public Lifesteal(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();

        if (damager instanceof Player player) {
            if (!(player.getGameMode() == GameMode.SURVIVAL)) {
                return;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(plugin, "lifesteal");
            if (meta != null && meta.getPersistentDataContainer().has(key)) {
                int level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                double HEAL_PERCENT = switch (level) {
                    case 1 -> 0.05;
                    case 2 -> 0.08;
                    case 3 -> 0.13;
                    default -> 0;
                };

                double damageDone = e.getFinalDamage();
                double healAmount = HEAL_PERCENT * damageDone;
                double currentHealth = player.getHealth();

                // Heal player for a percentage of damage done, without exceeding max health
                player.setHealth(Math.min(healAmount + currentHealth, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
            }
        }
    }
}
