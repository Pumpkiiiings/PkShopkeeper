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

    public MainMenuGUI(PkShopkeepers plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, PkShop shop) {
        String titleStr = plugin.getConfigManager().getGuiString("main-menu.title", "Shop Control Panel - %id%");
        titleStr = titleStr.replace("%id%", shop.getId());
        
        Component title = plugin.getConfigManager().parseString(titleStr);
        Inventory inv = Bukkit.createInventory(null, 27, title);
        
        plugin.getShopManager().getEditingPlayers().put(player.getUniqueId(), shop);

        inv.setItem(10, createItem(Material.CHEST, plugin.getConfigManager().getMenuComponent("main-menu.btn-edit-trades"), plugin.getConfigManager().getGuiStringList("main-menu.btn-edit-trades-lore").toArray(new String[0])));
        inv.setItem(11, createItem(Material.NAME_TAG, plugin.getConfigManager().getMenuComponent("main-menu.btn-change-name"), plugin.getConfigManager().getGuiStringList("main-menu.btn-change-name-lore").stream().map(s -> s.replace("%name%", shop.getName())).toArray(String[]::new)));
        inv.setItem(12, createItem(Material.ZOMBIE_HEAD, plugin.getConfigManager().getMenuComponent("main-menu.btn-change-type"), plugin.getConfigManager().getGuiStringList("main-menu.btn-change-type-lore").stream().map(s -> s.replace("%type%", shop.getEntityType().name()).replace("%baby%", shop.isBaby() ? "Sí" : "No")).toArray(String[]::new)));
        
        if (shop.getEntityType() == org.bukkit.entity.EntityType.VILLAGER) {
            inv.setItem(14, createItem(Material.OAK_SAPLING, plugin.getConfigManager().getMenuComponent("main-menu.btn-villager-type"), plugin.getConfigManager().getGuiStringList("main-menu.btn-villager-type-lore").stream().map(s -> s.replace("%type%", shop.getVillagerType().name())).toArray(String[]::new)));
            inv.setItem(15, createItem(Material.EMERALD, plugin.getConfigManager().getMenuComponent("main-menu.btn-villager-level"), plugin.getConfigManager().getGuiStringList("main-menu.btn-villager-level-lore").stream().map(s -> s.replace("%level%", String.valueOf(shop.getVillagerLevel()))).toArray(String[]::new)));
        }
        
        inv.setItem(16, createItem(Material.BARRIER, plugin.getConfigManager().getMenuComponent("main-menu.btn-delete-shop"), plugin.getConfigManager().getGuiStringList("main-menu.btn-delete-shop-lore").toArray(new String[0])));

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
        
        String titleStr = plugin.getConfigManager().getGuiString("main-menu.title", "Shop Control Panel - %id%");
        titleStr = titleStr.replace("%id%", shop.getId());
        
        Component title = plugin.getConfigManager().parseString(titleStr);
        if (!event.getView().title().equals(title)) return;
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        if (slot == 10) {
            plugin.getShopEditorListener().openEditor(player, shop);
        } else if (slot == 11) {
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getMessage("enter-name"));
            plugin.getShopManager().getRenamingPlayers().put(player.getUniqueId(), shop);
        } else if (slot == 12) {
            plugin.getEntitySelectorGUI().openMenu(player, shop);
        } else if (slot == 14 && shop.getEntityType() == org.bukkit.entity.EntityType.VILLAGER) {
            plugin.getVillagerTypeGUI().openMenu(player, shop);
        } else if (slot == 15 && shop.getEntityType() == org.bukkit.entity.EntityType.VILLAGER) {
            plugin.getVillagerLevelGUI().openMenu(player, shop);
        } else if (slot == 16) {
            player.closeInventory();
            plugin.getShopManager().removeShop(shop.getId());
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getShopEntityListener().removeEntity(shop);
            });
            player.sendMessage(plugin.getConfigManager().getMessage("shop-deleted"));
        }
    }
    
    @EventHandler
    public void onClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        PkShop shop = plugin.getShopManager().getEditingPlayers().get(player.getUniqueId());
        if (shop == null) return;
        
        String titleStr = plugin.getConfigManager().getGuiString("main-menu.title", "Shop Control Panel - %id%");
        titleStr = titleStr.replace("%id%", shop.getId());
        Component expectedTitle = plugin.getConfigManager().parseString(titleStr);
        
        if (event.getView().title().equals(expectedTitle)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.getOpenInventory().getTopInventory().getSize() <= 5) {
                    plugin.getShopManager().getEditingPlayers().remove(player.getUniqueId());
                }
            }, 1L);
        }
    }
}
