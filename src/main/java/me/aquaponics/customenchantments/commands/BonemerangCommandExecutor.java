package me.aquaponics.customenchantments.commands;

import me.aquaponics.customenchantments.CustomEnchantments;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class BonemerangCommandExecutor implements CommandExecutor {
    private final Plugin plugin;
    public BonemerangCommandExecutor(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player p) {
            if (!p.hasPermission("customench.bonemerang")) {
                p.sendMessage(ChatColor.RED + "You do not have permission to run this command");
                return true;
            }
            ItemStack bonemerang = new ItemStack(Material.BONE, 1);
            ItemMeta meta = bonemerang.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Bonemerang");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "bonemerang"), PersistentDataType.INTEGER, 0);

            List<String> lore = (meta.getLore() == null) ? new ArrayList<>() : meta.getLore();
            lore.add("");
            lore.add(ChatColor.GOLD + "Ability: Swing " + ChatColor.RESET +
                            "" + ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT CLICK");
            lore.add("Throw the bone like a boomerang, dealing damage to entities in its path");

            meta.setLore(lore);
            bonemerang.setItemMeta(meta);
            p.getInventory().addItem(bonemerang);
        }
        return true;
    }
}
