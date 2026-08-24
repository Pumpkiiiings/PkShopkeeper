package com.pumpkings.pkshopkeepers.editor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;

public class VillagerLevelGUI implements Listener {

    private final PkShopkeepers plugin;

    public VillagerLevelGUI(PkShopkeepers plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, PkShop shop) {
        String titleStr = plugin.getConfigManager().getGuiString("villager-level-menu.title", "Seleccionar Nivel");
        Component title = plugin.getConfigManager().parseString(titleStr);
        Inventory inv = Bukkit.createInventory(null, 27, title);
        
        plugin.getShopManager().getEditingPlayers().put(player.getUniqueId(), shop);

        for (int i = 1; i <= 5; i++) {
            Material mat = Material.EMERALD;
            String nameStr = plugin.getConfigManager().getGuiString("villager-level-menu.btn-level", "&aNivel %level%").replace("%level%", String.valueOf(i));
            Component name = plugin.getConfigManager().parseString(nameStr);
            inv.setItem(10 + i, createItem(mat, name, i));
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, Component name, int level) {
        ItemStack item = new ItemStack(mat, level);
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
        
        String titleStr = plugin.getConfigManager().getGuiString("villager-level-menu.title", "Seleccionar Nivel");
        Component title = plugin.getConfigManager().parseString(titleStr);
        if (!event.getView().title().equals(title)) return;
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getRawSlot();
        if (slot >= 11 && slot <= 15) {
            int level = slot - 10;
            shop.setVillagerLevel(level);
            plugin.getShopEntityListener().removeEntity(shop);
            plugin.getShopEntityListener().spawnShop(shop);
            plugin.getShopManager().saveShops();
            player.sendMessage(plugin.getConfigManager().getMessage("villager-level-changed", "%level%", String.valueOf(level)));
            plugin.getMainMenuGUI().openMenu(player, shop);
        }
    }
}
