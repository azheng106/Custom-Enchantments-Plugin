package me.aquaponics.customenchantments.enchants.bow;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Artemis implements Listener {
    private final Plugin plugin;


    public Artemis(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent e) {
        Random random = new Random();
        int rand = random.nextInt(100) + 1; // number from 1 to 100, inclusive
        LivingEntity entity = e.getEntity();
        ItemStack bow = e.getBow();
        ItemMeta bowMeta = bow.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "artemis");
        if (bowMeta != null && bowMeta.getPersistentDataContainer().has(key)) {
            if (rand <= 20) {
                List<Entity> nearbyEntities = entity.getNearbyEntities(100, 100, 100).stream()
                        .filter(e1 -> e1 instanceof LivingEntity && !e1.equals(entity))
                        .filter(e1 -> { // Only home onto mobs in a 90 deg cone of vision from bowman
                            Vector playerDirection = entity.getLocation().getDirection();
                            Vector toPlayer = e1.getLocation().subtract(entity.getLocation()).toVector();
                            double angle = Math.toDegrees(toPlayer.angle(playerDirection));
                            return angle <= 45;
                        })
                        .filter(entity::hasLineOfSight) // Only home onto things the bowman can see
                        .sorted(Comparator.comparingDouble(e1 -> e1.getLocation().distance(entity.getLocation())))
                        .toList();
                if (!nearbyEntities.isEmpty()) {
                    Entity nearestEntity = nearbyEntities.get(0);
                    Location targetLocation = nearestEntity.getLocation().subtract(0, nearestEntity.getHeight() / getDivisor(nearestEntity), 0);
                    Vector direction = targetLocation.subtract(entity.getLocation()).toVector().normalize();
                    Arrow arrow = (Arrow) e.getProjectile();
                    arrow.setVelocity(direction.multiply(arrow.getVelocity().length()));
                    if (entity instanceof Player p) {
                        p.sendMessage(ChatColor.AQUA + "Artemis blessed your arrow!");
                    }
                }
            }
        }
    }


    /**
     * Subtract correct amount from location object of entity we want to home to.
     * Ensures homing arrows will actually hit their target
     * @param e The entity we are homing to
     * @return Lower return value means arrow homes to a lower (smaller y) location
     */
    private double getDivisor(Entity e) {
        double height = e.getHeight();
        if (height >= 2) {
            return 3.2;
        }
        if (height < 2 && height > 1.5) {
            return 2.2;
        }
        return 1;
    }
}
