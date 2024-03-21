package me.aquaponics.customenchantments.enchants.armor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LavaWalker implements Listener {
    private final Plugin plugin;
    public LavaWalker(Plugin plugin) {
        this.plugin = plugin;
    }

    private final HashMap<Location, UUID> convertedLavaLocations = new HashMap<>();
    private final Set<Location> toRemove = new HashSet<>();

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        ItemStack boots = player.getEquipment().getBoots();
        if (boots == null) { // If player is not wearing boots, convert their magma back to lava
            for (Location loc : convertedLavaLocations.keySet()) {
                if (Bukkit.getPlayer(convertedLavaLocations.get(loc)) == player) {
                    loc.getBlock().setType(Material.LAVA);
                    toRemove.add(loc);
                }
            }
            return;
        }
        ItemMeta bootsMeta = boots.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "lavawalker");
        if (bootsMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            int level = bootsMeta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
            int RADIUS = switch (level) {
                case 1 -> 2;
                case 2 -> 3;
                default -> 0;
            };

            // Convert lava blocks within RADIUS of player to magma blocks
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    Location playerLoc = player.getLocation();
                    Location blockLoc = playerLoc.clone().add(x, -1, z);
                    if (blockLoc.distance(playerLoc) <= RADIUS) {
                        if (blockLoc.getBlock().getType() == Material.LAVA) {
                            blockLoc.getBlock().setType(Material.MAGMA_BLOCK);
                            convertedLavaLocations.put(blockLoc, uuid);
                        }
                    }
                }
            }

            // Convert magma blocks that are out of RADIUS back to lava blocks
            for (Location loc : convertedLavaLocations.keySet()) {
                if (loc.getBlock().getType() == Material.MAGMA_BLOCK) {
                    Player p = Bukkit.getPlayer(convertedLavaLocations.get(loc)); // Get the Player that is matched with loc
                    Location pLoc = p.getLocation();
                    if (pLoc.distance(loc) > RADIUS) {
                        loc.getBlock().setType(Material.LAVA);
                        toRemove.add(loc);
                    }
                }
            }
        }
        else { // If player is wearing boots that don't have Lava Walker, convert all their magma blocks back into lava
            for (Location loc : convertedLavaLocations.keySet()) {
                if (Bukkit.getPlayer(convertedLavaLocations.get(loc)) == player) {
                    loc.getBlock().setType(Material.LAVA);
                    toRemove.add(loc);
                }
            }
        }
        for (Location loc : toRemove) {
            convertedLavaLocations.remove(loc);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByBlockEvent e) {
        // Prevent player from taking damage from magma blocks while wearing Lava Walker
        Entity entity = e.getEntity();
        if (entity instanceof Player player) {
            if (!(player.getEquipment() != null &&
                  player.getEquipment().getBoots() != null &&
                  player.getEquipment().getBoots().getItemMeta() != null)) {
                return;
            }
            ItemStack boots = player.getEquipment().getBoots();
            ItemMeta bootsMeta = boots.getItemMeta();
            NamespacedKey key = new NamespacedKey(plugin, "lavawalker");
            if (bootsMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                if (e.getDamager().getType() == Material.MAGMA_BLOCK) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Make sure converted magma blocks don't drop items when broken
        Block block = e.getBlock();
        Location blockLoc = block.getLocation();
        if (isLocationInConvertedLavaLocations(blockLoc)) {
            e.setDropItems(false);
            block.setType(Material.LAVA);
            convertedLavaLocations.remove(blockLoc);
        }
    }

    private boolean isLocationInConvertedLavaLocations(Location loc) {
        for (Location location : convertedLavaLocations.keySet()) {
            if (location.getWorld().equals(loc.getWorld()) &&
                    location.getBlockX() == loc.getBlockX() &&
                    location.getBlockY() == loc.getBlockY() &&
                    location.getBlockZ() == loc.getBlockZ()) {
                return true;
            }
        }
        return false;
    }
}
