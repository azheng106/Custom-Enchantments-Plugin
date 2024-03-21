package me.aquaponics.customenchantments.enchants.weapon;

import org.bukkit.*;
import org.bukkit.entity.Entity;
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

public class Assassin implements Listener {
    private final Plugin plugin;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public Assassin(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity damaged = e.getEntity();

        if (damager instanceof Player player) {
            UUID uuid = player.getUniqueId();
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(plugin, "assassin");

            if (meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                if (!player.isSneaking()) {
                    return;
                }

                int level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                long time = System.currentTimeMillis();
                int TP_COOLDOWN_MS = switch (level) {
                    case 1 -> 20000;
                    case 2 -> 15000;
                    case 3 -> 10000;
                    default -> 0;
                };

                if (cooldowns.containsKey(uuid) && time - cooldowns.get(uuid) < TP_COOLDOWN_MS) {
                    player.sendMessage(ChatColor.DARK_RED + "Assassin Teleport on cooldown (" +
                            String.format("%.2f", (TP_COOLDOWN_MS - (time - cooldowns.get(uuid))) / 1000D) + "s)");
                } else {
                    Location targetLocation = damaged.getLocation();
                    Location behindTarget = targetLocation.add(targetLocation.getDirection().multiply(-1));
                    if (behindTarget.getBlock().getType().isAir() &&
                        behindTarget.clone().add(0, -1, 0).getBlock().getType().isAir() &&
                        behindTarget.clone().add(0, -2, 0).getBlock().getType().isAir()) {
                        player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Canceled Assassin teleport to prevent falling");
                    } else {
                        behindTarget.setYaw(targetLocation.getYaw()); // Set player to be looking at the target's back after teleporting
                        damager.teleport(behindTarget);
                        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 100);
                        cooldowns.put(uuid, time);
                    }
                }
            }
        }
    }


}
