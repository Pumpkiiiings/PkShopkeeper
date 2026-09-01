package com.pumpkings.pkshopkeepers.editor;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import com.pumpkings.pkshopkeepers.shop.PkTradeOffer;
import com.pumpkings.pkshopkeepers.shop.ShopManager;
import net.kyori.adventure.text.Component;

public class ShopEditorListener implements Listener {

    private final PkShopkeepers plugin;

    public ShopEditorListener(PkShopkeepers plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openEditor(Player player, PkShop shop) {
        openEditor(player, shop, 0);
    }

    public void openEditor(Player player, PkShop shop, int page) {
        Component title = plugin.getConfigManager().getMenuComponent("editor.title");
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        for (int i = 0; i < 54; i++) {
            if (i % 9 == 3 || i % 9 == 7 || i % 9 == 8) {
                inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(" ")));
            }
        }

        // Navigation
        inv.setItem(45, createItem(Material.ARROW, plugin.getConfigManager().getMenuComponent("editor.btn-prev-page")));
        inv.setItem(53, createItem(Material.EMERALD_BLOCK, plugin.getConfigManager().getMenuComponent("editor.btn-save")));
        inv.setItem(52, createItem(Material.ARROW, plugin.getConfigManager().getMenuComponent("editor.btn-next-page")));

        List<PkTradeOffer> offers = shop.getOffers();
        int startIndex = page * 10; // Max 10 per page to leave bottom row for navigation
        int offerIndex = startIndex;
        
        for (int row = 0; row < 5; row++) {
            for (int group = 0; group < 2; group++) {
                if (offerIndex >= offers.size()) break;
                
                int startCol = group == 0 ? 0 : 4;
                int slot1 = row * 9 + startCol;
                int slot2 = row * 9 + startCol + 1;
                int slot3 = row * 9 + startCol + 2;

                PkTradeOffer offer = offers.get(offerIndex);
                if (offer.getItem1() != null) inv.setItem(slot1, offer.getItem1().clone());
                if (offer.getItem2() != null) inv.setItem(slot2, offer.getItem2().clone());
                if (offer.getResult() != null) inv.setItem(slot3, offer.getResult().clone());
                
                offerIndex++;
            }
        }

        plugin.getShopManager().getEditingPlayers().put(player.getUniqueId(), shop);
        // We need a way to store the current page. We can use a map.
        editingPages.put(player.getUniqueId(), page);
        
        player.openInventory(inv);
    }

    private java.util.Map<java.util.UUID, Integer> editingPages = new java.util.HashMap<>();

    private ItemStack createItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Component title = plugin.getConfigManager().getMenuComponent("editor.title");
        if (!event.getView().title().equals(title)) return;
        
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        if (slot % 9 == 3 || slot % 9 == 7 || slot % 9 == 8) {
            event.setCancelled(true);
        }

        Player player = (Player) event.getWhoClicked();
        
        if (slot == 45) { // Anterior
            event.setCancelled(true);
            int page = editingPages.getOrDefault(player.getUniqueId(), 0);
            if (page > 0) {
                savePage(player, event.getInventory());
                openEditor(player, plugin.getShopManager().getEditingPlayers().get(player.getUniqueId()), page - 1);
            }
        } else if (slot == 52) { // Siguiente
            event.setCancelled(true);
            int page = editingPages.getOrDefault(player.getUniqueId(), 0);
            savePage(player, event.getInventory());
            PkShop shop = plugin.getShopManager().getEditingPlayers().get(player.getUniqueId());
            if (shop.getOffers().size() >= (page + 1) * 10) {
                openEditor(player, shop, page + 1);
            } else {
                openEditor(player, shop, page);
            }
        } else if (slot == 53) { // Guardar
            event.setCancelled(true);
            savePage(player, event.getInventory());
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getMessage("shop-saved"));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Component title = plugin.getConfigManager().getMenuComponent("editor.title");
        if (event.getView().title().equals(title)) {
            Player player = (Player) event.getPlayer();
            if (plugin.getShopManager().getEditingPlayers().containsKey(player.getUniqueId())) {
                if (plugin.getConfigManager().getBoolean("settings.auto-save-on-close", true)) {
                    savePage(player, event.getInventory());
                }
                com.pumpkings.pkshopkeepers.utils.FoliaScheduler.runEntityTaskLater(plugin, player, () -> {
                    if (player.getOpenInventory().getTopInventory().getSize() != 54) { // Not in a GUI anymore
                        plugin.getShopManager().getEditingPlayers().remove(player.getUniqueId());
                        editingPages.remove(player.getUniqueId());
                    }
                }, 1L);
            }
        }
    }

    private void savePage(Player player, Inventory inv) {
        PkShop shop = plugin.getShopManager().getEditingPlayers().get(player.getUniqueId());
        if (shop == null) return;
        
        int page = editingPages.getOrDefault(player.getUniqueId(), 0);
        List<PkTradeOffer> offers = shop.getOffers();
        
        // Remove offers for this page
        int startIndex = page * 10;
        int maxEnd = Math.min(offers.size(), startIndex + 10);
        
        // Extract new ones from GUI
        List<PkTradeOffer> pageOffers = new ArrayList<>();
        for (int row = 0; row < 5; row++) {
            for (int group = 0; group < 2; group++) {
                int startCol = group == 0 ? 0 : 4;
                ItemStack item1 = inv.getItem(row * 9 + startCol);
                ItemStack item2 = inv.getItem(row * 9 + startCol + 1);
                ItemStack result = inv.getItem(row * 9 + startCol + 2);
                
                if (item1 != null && item1.getType() != Material.AIR && result != null && result.getType() != Material.AIR) {
                    pageOffers.add(new PkTradeOffer(item1.clone(), item2 != null ? item2.clone() : null, result.clone()));
                }
            }
        }
        
        // Replace old page with new page
        // 1. Remove old
        for (int i = 0; i < (maxEnd - startIndex); i++) {
            if (startIndex < offers.size()) offers.remove(startIndex);
        }
        
        // 2. Insert new
        int insertIndex = Math.min(startIndex, offers.size());
        offers.addAll(insertIndex, pageOffers);
        
        shop.setOffers(offers);
        plugin.getShopManager().saveShops();
    }
}
