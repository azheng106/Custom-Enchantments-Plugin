package me.aquaponics.customenchantments.enchants.armor;

import me.aquaponics.customenchantments.CustomEnchantments;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class Phoenix implements Listener {
    private final Plugin plugin;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public Phoenix(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof Player player) {
            if (player.getHealth() - e.getFinalDamage() <= 0) {
                UUID uuid = player.getUniqueId();
                NamespacedKey key = new NamespacedKey(plugin, "phoenix");
                ItemStack leggings = player.getEquipment().getLeggings();
                ItemMeta meta = leggings.getItemMeta();

                if (meta.getPersistentDataContainer().has(key)) {
                    long time = System.currentTimeMillis();
                    int PHOENIX_COOLDOWN_MS = 1000 * 60 * 20;
                    if (cooldowns.containsKey(uuid) && time - cooldowns.get(uuid) < PHOENIX_COOLDOWN_MS) {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "Phoenix on cooldown (" +
                                String.format("%.2f", (PHOENIX_COOLDOWN_MS - (time - cooldowns.get(uuid))) / (1000D * 60)) + "m)");
                    } else {
                        e.setCancelled(true);
                        player.setHealth(7);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 140, 1));
                        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 100);
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "Phoenix revived you from the ashes!");
                        cooldowns.put(uuid, time);
                    }
                }
            }
        }
    }
}
