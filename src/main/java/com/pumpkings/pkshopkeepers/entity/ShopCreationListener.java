package com.pumpkings.pkshopkeepers.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.EntityType;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import java.util.UUID;

public class ShopCreationListener implements Listener {

    private final PkShopkeepers plugin;

    public ShopCreationListener(PkShopkeepers plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        NamespacedKey key = new NamespacedKey(plugin, "shop_creation_item");
        if (item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            
            if (!event.getPlayer().hasPermission("pkshopkeepers.admin")) {
                event.getPlayer().sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return;
            }
            
            String newId = plugin.getShopManager().getNextId();
            PkShop newShop = new PkShop(newId);
            newShop.setName("Nueva Tienda");
            newShop.setLocation(event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5));
            newShop.setEntityType(EntityType.VILLAGER);
            
            plugin.getShopManager().addShop(newShop);
            plugin.getShopManager().saveShops();
            
            com.pumpkings.pkshopkeepers.utils.FoliaScheduler.runRegionTask(plugin, newShop.getLocation(), () -> {
                new ShopEntityListener(plugin, plugin.getShopManager()).spawnShop(newShop);
            });
            
            event.getPlayer().sendMessage(plugin.getConfigManager().getMessage("shop-created"));
        }
    }
}
