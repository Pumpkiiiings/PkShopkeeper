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

public class MainMenuGUI implements Listener {

    private final PkShopkeepers plugin;
    private final String TITLE = "§8Panel de Control";

    public MainMenuGUI(PkShopkeepers plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, PkShop shop) {
        String titleStr = plugin.getConfig().getString("menus.main-menu-title", "Panel de control - %id%");
        titleStr = titleStr.replace("%id%", shop.getId());
        
        Component title = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(titleStr);
        Inventory inv = Bukkit.createInventory(null, 27, title);
        
        plugin.getShopManager().getEditingPlayers().put(player.getUniqueId(), shop);

        inv.setItem(10, createItem(Material.CHEST, plugin.getConfigManager().getMenuComponent("btn-edit-trades"), "§7Abre el menú para añadir", "§7o quitar monedas e items."));
        inv.setItem(12, createItem(Material.NAME_TAG, plugin.getConfigManager().getMenuComponent("btn-change-name"), "§7Renombra a " + shop.getName()));
        inv.setItem(14, createItem(Material.ZOMBIE_HEAD, plugin.getConfigManager().getMenuComponent("btn-change-type"), "§7Actualmente: §a" + shop.getEntityType().name(), "§7Bebé: §a" + (shop.isBaby() ? "Sí" : "No")));
        inv.setItem(16, createItem(Material.BARRIER, plugin.getConfigManager().getMenuComponent("btn-delete-shop"), "§7Borrará la tienda permanentemente."));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, Component name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        java.util.List<Component> loreList = new java.util.ArrayList<>();
        for (String l : lore) {
            loreList.add(Component.text(l));
        }
        meta.lore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PkShop shop = plugin.getShopManager().getEditingPlayers().get(player.getUniqueId());
        if (shop == null) return;
        
        String titleStr = plugin.getConfig().getString("menus.main-menu-title", "Panel de control - %id%");
        titleStr = titleStr.replace("%id%", shop.getId());
        Component expectedTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(titleStr);
        
        if (!event.getView().title().equals(expectedTitle)) return;
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        if (slot == 10) {
            plugin.getShopEditorListener().openEditor(player, shop);
        } else if (slot == 12) {
            player.closeInventory();
            player.sendMessage(plugin.getConfig().getString("menus.rename-prompt", "§e[PkShopkeepers] Escribe el nuevo nombre en el chat (soporta & para colores):"));
            plugin.getShopManager().getRenamingPlayers().put(player.getUniqueId(), shop);
        } else if (slot == 14) {
            plugin.getEntitySelectorGUI().openMenu(player, shop);
        } else if (slot == 16) {
            player.closeInventory();
            plugin.getShopManager().removeShop(shop.getId());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getShopEntityListener().removeEntity(shop);
            });
            player.sendMessage("§cTienda eliminada.");
        }
    }
    
    @EventHandler
    public void onClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        PkShop shop = plugin.getShopManager().getEditingPlayers().get(player.getUniqueId());
        if (shop == null) return;
        
        String titleStr = plugin.getConfig().getString("menus.main-menu-title", "Panel de control - %id%");
        titleStr = titleStr.replace("%id%", shop.getId());
        Component expectedTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(titleStr);
        
        if (event.getView().title().equals(expectedTitle)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.getOpenInventory().getTopInventory().getSize() <= 5) {
                    plugin.getShopManager().getEditingPlayers().remove(player.getUniqueId());
                }
            }, 1L);
        }
    }
}
