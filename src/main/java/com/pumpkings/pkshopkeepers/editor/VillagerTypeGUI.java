package com.pumpkings.pkshopkeepers.editor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;

public class VillagerTypeGUI implements Listener {

    private final PkShopkeepers plugin;

    public VillagerTypeGUI(PkShopkeepers plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, PkShop shop) {
        String titleStr = plugin.getConfigManager().getGuiString("villager-type-menu.title", "Select Biome");
        Component title = plugin.getConfigManager().parseString(titleStr);
        Inventory inv = Bukkit.createInventory(null, 27, title);
        
        plugin.getShopManager().getEditingPlayers().put(player.getUniqueId(), shop);

        int slot = 9;
        for (Villager.Type type : org.bukkit.Registry.VILLAGER_TYPE) {
            if (slot > 17) break;
            Material mat = getMaterialForType(type.getKey().getKey().toUpperCase());
            String nameStr = plugin.getConfigManager().getGuiString("villager-type-menu.types." + type.getKey().getKey().toLowerCase() + ".name", "&e" + type.getKey().getKey().toUpperCase());
            Component name = plugin.getConfigManager().parseString(nameStr);
            inv.setItem(slot++, createItem(mat, name));
        }

        player.openInventory(inv);
    }

    private Material getMaterialForType(String typeName) {
        switch (typeName) {
            case "DESERT": return Material.SAND;
            case "JUNGLE": return Material.JUNGLE_LOG;
            case "PLAINS": return Material.OAK_LOG;
            case "SAVANNA": return Material.ACACIA_LOG;
            case "SNOW": return Material.SNOW_BLOCK;
            case "SWAMP": return Material.SLIME_BLOCK;
            case "TAIGA": return Material.SPRUCE_LOG;
            default: return Material.OAK_SAPLING;
        }
    }

    private ItemStack createItem(Material mat, Component name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PkShop shop = plugin.getShopManager().getEditingPlayers().get(player.getUniqueId());
        if (shop == null) return;
        
        String titleStr = plugin.getConfigManager().getGuiString("villager-type-menu.title", "Select Biome");
        Component title = plugin.getConfigManager().parseString(titleStr);
        if (!event.getView().title().equals(title)) return;
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Villager.Type selected = null;
        for (Villager.Type type : org.bukkit.Registry.VILLAGER_TYPE) {
            if (getMaterialForType(type.getKey().getKey().toUpperCase()) == clicked.getType()) {
                selected = type;
                break;
            }
        }
        
        if (selected != null) {
            shop.setVillagerType(selected);
            plugin.getShopEntityListener().removeEntity(shop);
            plugin.getShopEntityListener().spawnShop(shop);
            plugin.getShopManager().saveShops();
            player.sendMessage(plugin.getConfigManager().getMessage("villager-type-changed", "%type%", selected.getKey().getKey().toUpperCase()));
            plugin.getMainMenuGUI().openMenu(player, shop);
        }
    }
}
