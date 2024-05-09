package me.aquaponics.customenchantments.enchants.melee;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class Perun implements Listener {
    private final Plugin plugin;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public Perun(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity damaged = e.getEntity();
        LivingEntity damagedEntity = (LivingEntity) damaged;

        if (damager instanceof Player player) {
            UUID uuid = player.getUniqueId();
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(plugin, "perun");

            if (meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                long time = System.currentTimeMillis();
                int PERUN_COOLDOWN_MS = 3500;
                if (cooldowns.containsKey(uuid) && time - cooldowns.get(uuid) < PERUN_COOLDOWN_MS) {
                    player.sendMessage(ChatColor.RED + "Perun Lightning is on cooldown (" +
                            String.format("%.2f", (PERUN_COOLDOWN_MS - (time - cooldowns.get(uuid))) / 1000D) + "s)");
                } else {
                    int level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    damagedEntity.getWorld().strikeLightningEffect(damaged.getLocation());
                    switch (level) {
                        case 1:
                            damagedEntity.damage(2);
                            break;
                        case 2:
                            damagedEntity.damage(4);
                            break;
                    }
                    cooldowns.put(uuid, time);
                    player.sendMessage(ChatColor.GOLD + "Health Remaining: " +
                            String.format("%.2f", damagedEntity.getHealth()));
                }
            }
        }
    }
}
