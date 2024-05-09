package me.aquaponics.customenchantments;

import me.aquaponics.customenchantments.commands.BonemerangCommandExecutor;
import me.aquaponics.customenchantments.commands.EnchantCommandExecutor;
import me.aquaponics.customenchantments.enchants.AnvilHandler;
import me.aquaponics.customenchantments.enchants.EnchantList;
import me.aquaponics.customenchantments.enchants.armor.LavaWalker;
import me.aquaponics.customenchantments.enchants.armor.Phoenix;
import me.aquaponics.customenchantments.enchants.bow.Artemis;
import me.aquaponics.customenchantments.enchants.bow.MegaLongBow;
import me.aquaponics.customenchantments.enchants.bow.Telebow;
import me.aquaponics.customenchantments.enchants.melee.Assassin;
import me.aquaponics.customenchantments.enchants.melee.Lifesteal;
import me.aquaponics.customenchantments.enchants.melee.Perun;
import me.aquaponics.customenchantments.items.Bonemerang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class CustomEnchantments extends JavaPlugin {
    public static HashMap<EnchantList, Integer> maxLevels = new HashMap<>();

    @Override
    public void onEnable() {
        // Put all enchants and max levels here
        maxLevels.put(EnchantList.PERUN, 2);
        maxLevels.put(EnchantList.MEGALONGBOW, 3);
        maxLevels.put(EnchantList.ASSASSIN, 3);
        maxLevels.put(EnchantList.TELEBOW, 3);
        maxLevels.put(EnchantList.LIFESTEAL, 3);
        maxLevels.put(EnchantList.LAVAWALKER, 2);
        maxLevels.put(EnchantList.PHOENIX, 1);
        maxLevels.put(EnchantList.ARTEMIS, 1);

        this.getCommand("cenchant").setExecutor(new EnchantCommandExecutor());
        this.getCommand("bonemerang").setExecutor(new BonemerangCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new Perun(this), this);
        getServer().getPluginManager().registerEvents(new MegaLongBow(this), this);
        getServer().getPluginManager().registerEvents(new Assassin(this), this);
        getServer().getPluginManager().registerEvents(new Telebow(this), this);
        getServer().getPluginManager().registerEvents(new Lifesteal(this), this);
        getServer().getPluginManager().registerEvents(new LavaWalker(this), this);
        getServer().getPluginManager().registerEvents(new Phoenix(this), this);
        getServer().getPluginManager().registerEvents(new AnvilHandler(this), this);
        getServer().getPluginManager().registerEvents(new Artemis(this), this);

        getServer().getPluginManager().registerEvents(new Bonemerang(this), this);

        // Recipe to craft a perun axe
        ItemStack perunAxe = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = perunAxe.getItemMeta();
        NamespacedKey perunAxeKey = new NamespacedKey(this, "perun");
        meta.getPersistentDataContainer().set(perunAxeKey, PersistentDataType.INTEGER, 1);
        // If item already has lore, then append our new lore on. Else, create new lore object
        List<String> lore = (meta.getLore() == null) ? new ArrayList<>() : meta.getLore();
        lore.add(ChatColor.GRAY + "Perun I");
        meta.setLore(lore);
        perunAxe.setItemMeta(meta);
        ShapedRecipe perunAxeRecipe = new ShapedRecipe(perunAxeKey, perunAxe);
        perunAxeRecipe.shape("GTF", "GS ", " S ");
        perunAxeRecipe.setIngredient('G', Material.GOLD_INGOT);
        perunAxeRecipe.setIngredient('T', Material.TNT);
        perunAxeRecipe.setIngredient('F', Material.FIRE_CHARGE);
        perunAxeRecipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(perunAxeRecipe);
    }
}
