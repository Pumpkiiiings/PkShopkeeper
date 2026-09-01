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

            int maxShops = plugin.getConfigManager().getInt("settings.max-shops-per-player", -1);
            if (maxShops >= 0 && plugin.getShopManager().countShopsOwnedBy(event.getPlayer().getUniqueId()) >= maxShops) {
                event.getPlayer().sendMessage(plugin.getConfigManager().getMessage("max-shops-reached", "%max%", String.valueOf(maxShops)));
                return;
            }

            java.util.List<String> allowedTypes = plugin.getConfigManager().getStringList("settings.enabled-living-shops");
            if (!allowedTypes.isEmpty() && allowedTypes.stream().noneMatch(type -> type.equalsIgnoreCase(EntityType.VILLAGER.name()))) {
                event.getPlayer().sendMessage(plugin.getConfigManager().getMessage("invalid-entity"));
                return;
            }
            
            String newId = plugin.getShopManager().getNextId();
            PkShop newShop = new PkShop(newId);
            newShop.setName(plugin.getConfigManager().getRawString("settings.default-shop-name", "New Shop"));
            newShop.setLocation(event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5));
            newShop.setOwnerUUID(event.getPlayer().getUniqueId());
            newShop.setEntityType(EntityType.VILLAGER);
            
            plugin.getShopManager().addShop(newShop);
            plugin.getShopManager().saveShops();
            
            plugin.getShopEntityListener().spawnShop(newShop);
            
            event.getPlayer().sendMessage(plugin.getConfigManager().getMessage("shop-created"));
        }
    }
}
