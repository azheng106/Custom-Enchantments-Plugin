package me.aquaponics.customenchantments.enchants.bow;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class Telebow implements Listener {
    private final Plugin plugin;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>(); // <A Player's UUID, Long>
    private final HashMap<UUID, Player> arrowPlayers = new HashMap<>(); // <An arrow's UUID, Player>

    public Telebow(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent e) {
        Entity entity = e.getEntity();
        ItemStack bow = e.getBow();
        ItemMeta bowMeta = bow.getItemMeta();
        if (entity instanceof Player player && e.getProjectile() instanceof Arrow arrow) {
            if (!player.isSneaking()) {
                return;
            }
            NamespacedKey key = new NamespacedKey(plugin, "telebow");
            UUID uuid = player.getUniqueId();

            if (bowMeta != null && bowMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                int level = bowMeta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                long time = System.currentTimeMillis();
                int TELEBOW_COOLDOWN_MS = switch (level) {
                    case 1 -> 45000;
                    case 2 -> 30000;
                    case 3 -> 15000;
                    default -> 0;
                };

                if (cooldowns.containsKey(uuid) && time - cooldowns.get(uuid) < TELEBOW_COOLDOWN_MS) {
                    player.sendMessage(ChatColor.DARK_AQUA + "Telebow on cooldown (" +
                            String.format("%.2f", (TELEBOW_COOLDOWN_MS - (time - cooldowns.get(uuid))) / 1000D) + "s)");
                } else {
                    cooldowns.put(uuid, time);
                    arrowPlayers.put(arrow.getUniqueId(), player);
                }
            }
        }
    }

    @EventHandler
    public void onArrowLand(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow arrow) {
            UUID arrowId = arrow.getUniqueId();
            if (!arrowPlayers.containsKey(arrowId)) {
                return;
            }
            Location arrowLocation = arrow.getLocation();
            Player player = arrowPlayers.get(arrowId);
            player.teleport(arrowLocation);
            arrowPlayers.remove(arrowId);
        }
    }
}
