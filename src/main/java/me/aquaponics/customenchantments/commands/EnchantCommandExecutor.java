package me.aquaponics.customenchantments.commands;

import me.aquaponics.customenchantments.CustomEnchantments;
import me.aquaponics.customenchantments.enchants.EnchantList;
import me.aquaponics.customenchantments.utils.NumberUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class EnchantCommandExecutor implements CommandExecutor {
    private final HashSet<Material> axes = new HashSet<>(Arrays.asList(
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.GOLDEN_AXE,
            Material.IRON_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE
    ));

    private final HashSet<Material> swords = new HashSet<>(Arrays.asList(
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.GOLDEN_SWORD,
            Material.IRON_SWORD,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD
    ));

    private final HashSet<Material> boots = new HashSet<>(Arrays.asList(
            Material.LEATHER_BOOTS,
            Material.CHAINMAIL_BOOTS,
            Material.GOLDEN_BOOTS,
            Material.IRON_BOOTS,
            Material.DIAMOND_BOOTS,
            Material.NETHERITE_BOOTS
    ));

    private final HashSet<Material> leggings = new HashSet<>(Arrays.asList(
            Material.LEATHER_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS,
            Material.GOLDEN_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.NETHERITE_LEGGINGS
    ));

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player player)) {
            return true;
        }
        if (args.length != 2) {
            player.sendMessage("Usage: /cenchant <enchant> <level>");
            return true;
        }

        String enchantString = args[0];
        if (enchantString.equalsIgnoreCase("mlb")) {
            enchantString = "megalongbow";
        }
        EnchantList enchantEnum = EnchantList.valueOf(enchantString.toUpperCase());

        enchantString = normalizeName(enchantString);
        int level = Integer.parseInt(args[1]);

        if (level > CustomEnchantments.maxLevels.get(enchantEnum)) {
            player.sendMessage("Inputted level exceeds max level");
            return true;
        }

        ItemStack currentHeldItem = player.getInventory().getItemInMainHand();
        if (!enchantCanBeAppliedToItem(player, enchantEnum, currentHeldItem.getType())) {
            /*
             If enchant cannot be applied to held item (e.g. perun cannot be applied to shovels)
             then don't continue generating replacement item
             */
            return true;
        }
        ItemStack replacementItem = applyEnchant(currentHeldItem, enchantString, level);
        player.getInventory().setItemInMainHand(replacementItem);

        return true;
    }

    /**
     * Apply an enchantment to an item. Updates item's lore and PersistentDataContainer
     * @param item: The item to apply the enchantment to
     * @param enchantmentName: The name of the enchantment to apply
     * @param level: The level of the enchantment to apply
     * @return The item with the enchantment applied
     */
    public ItemStack applyEnchant(ItemStack item, String enchantmentName, int level) {
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(
                CustomEnchantments.getPlugin(CustomEnchantments.class),
                enchantmentName.toLowerCase().replace(" ", ""));
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);

        // If item already has lore, then append our new lore on. Else, create new lore object
        List<String> lore = (meta.getLore() == null) ? new ArrayList<>() : meta.getLore();
        lore.removeIf(string -> string.contains(enchantmentName));
        lore.add(ChatColor.GRAY + enchantmentName + " " + NumberUtils.toRoman(level));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Correctly format the capitals/lowercases in an enchant name so the name looks good when added to an item
     * @param str: The enchant name to format
     */
    public static String normalizeName(String str) {
        if (str.equalsIgnoreCase("megalongbow")) {
            return "Mega Longbow";
        }
        if (str.equalsIgnoreCase("lavawalker")) {
            return "Lava Walker";
        }
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Check if an enchantment is able to be applied to the held item.
     * e.g. Weapon enchants can only be applied to axes and swords
     * @param player: The player who is trying to apply the enchant (used to send error messages to the player)
     * @param enchant: The enchantment that the player is trying to apply
     * @param currentItem: The item that the player is trying to apply the enchant to
     */
    public boolean enchantCanBeAppliedToItem(Player player, EnchantList enchant, Material currentItem) {
        switch (enchant) {
            case PERUN, ASSASSIN:
                if ( !(axes.contains(currentItem) || swords.contains(currentItem)) ) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Can only be applied to swords or axes");
                    return false;
                }
                break;
            case LIFESTEAL:
                if (!swords.contains(currentItem)) {
                    player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Can only be applied to swords");
                    return false;
                }
                break;
            case MEGALONGBOW, TELEBOW, ARTEMIS:
                if (currentItem != Material.BOW) {
                    player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Can only be applied to bows");
                    return false;
                }
                break;
            case LAVAWALKER:
                if (!boots.contains(currentItem)) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Can only be applied to boots");
                    return false;
                }
                break;
            case PHOENIX:
                if (!leggings.contains(currentItem)) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Can only be applied to boots");
                }
                break;
            default:
                player.sendMessage("weird error in enchantCanBeAppliedToItem method. please report");
                return false;
        }
        return true;
    }
}
