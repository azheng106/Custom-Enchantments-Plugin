package me.aquaponics.customenchantments.enchants.bow;

import me.aquaponics.customenchantments.CustomEnchantments;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MegaLongBow implements Listener {
    private final Plugin plugin;

    public MegaLongBow(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent e) {
        Entity entity = e.getEntity();
        ItemStack bow = e.getBow();
        ItemMeta bowMeta = bow.getItemMeta();
        if (entity instanceof Player player) {
            NamespacedKey key = new NamespacedKey(plugin, "megalongbow");

            if (bowMeta != null && bowMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                int level = bowMeta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                Arrow arrow = (Arrow) e.getProjectile();
                arrow.setVelocity(arrow.getVelocity().normalize().multiply(3));

                switch (level) {
                    case 1:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 80, 1));
                        break;
                    case 2:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 2));
                        break;
                    case 3:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 120, 3));
                        break;
                }
            }
        }
    }
}
