package me.aquaponics.customenchantments.items;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bonemerang implements Listener {
    private int ticks = 0;
    private final HashMap<UUID, ArmorStand> bonemerangsInPlay = new HashMap<>();
    private final Plugin plugin;

    public Bonemerang(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (e.getItem() == null || e.getItem().getType() != Material.BONE) return;
        if (!e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "bonemerang"))) return;
        Player player = e.getPlayer();
        if (!bonemerangsInPlay.containsKey(player.getUniqueId())) {
            startBoneProjectile(player);
        }
    }

    public void startBoneProjectile(Player player) {
        UUID uuid = player.getUniqueId();
        Location spawnLoc = player.getEyeLocation().subtract(0, 1.5, 0);
        ArmorStand armorStand = player.getWorld().spawn(spawnLoc, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setSmall(false);
            stand.setBasePlate(false);
            stand.setMarker(true); // Makes hitbox much smaller
            stand.getEquipment().setHelmet(new ItemStack(Material.BONE));
        });
        bonemerangsInPlay.put(uuid, armorStand);


        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bonemerangsInPlay.containsKey(uuid)) {
                    return;
                }
                float SPEED = 0.6f;
                float toYaw = (float) Math.toRadians(spawnLoc.getYaw());
                float toPitch = (float) Math.toRadians(spawnLoc.getPitch());

                // Make the boomerang track the player on the way back
                float backYaw = (float) Math.toRadians(player.getLocation().getYaw());
                float backPitch = (float) Math.toRadians(player.getLocation().getPitch());

                // Make armor stand move forward/backward
                Location teleportLoc = armorStand.getLocation();
                if (teleportLoc.clone().add(0, 1.485, 0).getBlock().getType().isSolid()) {
                    // Solid blocks will make the bone go poof
                    ticks = 0;
                    armorStand.remove();
                    bonemerangsInPlay.remove(uuid);
                    player.getWorld().spawnParticle(Particle.SNOWFLAKE, teleportLoc, 50);
                    this.cancel(); // Cancel the runnable
                }
                if (ticks < 18) { // Go away from player
                    teleportLoc.add(-1 * Math.sin(toYaw) * SPEED,
                            -1 * Math.sin(toPitch) * SPEED,
                            Math.cos(toYaw) * SPEED);
                } else if (ticks < 36) { // Come back to player
                    teleportLoc.add(Math.sin(backYaw) * SPEED,
                            Math.sin(backPitch) * SPEED,
                            -1 * Math.cos(backYaw) * SPEED);
                    player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, teleportLoc, 1);
                } else { // Remove the armor stand after 36 ticks
                    ticks = 0;
                    armorStand.remove();
                    bonemerangsInPlay.remove(uuid);
                    this.cancel(); // Cancel the runnable
                }
                teleportLoc.setYaw((teleportLoc.getYaw() + 24.0f) % 360f);
                armorStand.teleport(teleportLoc);
                ticks++;

                // Make bone do damage and knockback to entities it hits
                List<Entity> nearbyEntities = (List<Entity>) player.getWorld().getNearbyEntities(armorStand.getLocation().clone().add(0, 1.575, 0), 0.4, 0.4, 0.4); // Account for the bone being at the very top of the armor stand
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity le && entity != player) {
                        le.damage(4);
                        Vector knockbackDirection = le.getLocation().toVector().subtract(armorStand.getLocation().toVector()).normalize();
                        le.setVelocity(knockbackDirection.multiply(0.1));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
