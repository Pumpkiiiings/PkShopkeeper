package com.pumpkings.pkshopkeepers.editor;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EntitySelectorGUI implements Listener {

    private final PkShopkeepers plugin;

    private java.util.Map<java.util.UUID, Integer> selectorPages = new java.util.HashMap<>();

    public EntitySelectorGUI(PkShopkeepers plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, PkShop shop) {
        openMenu(player, shop, 0);
    }

    public void openMenu(Player player, PkShop shop, int page) {
        Component title = plugin.getConfigManager().getMenuComponent("entity-selector.title");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        List<String> enabledTypes = plugin.getConfigManager().getStringList("settings.enabled-living-shops");
        if (enabledTypes.isEmpty()) {
            // Add all living entities as fallback
            for (EntityType type : EntityType.values()) {
                if (type.isAlive() && type.isSpawnable()) {
                    enabledTypes.add(type.name());
                }
            }
        }

        int startIndex = page * 45;
        int slot = 0;
        for (int i = startIndex; i < enabledTypes.size(); i++) {
            if (slot >= 45) break;
            try {
                EntityType type = EntityType.valueOf(enabledTypes.get(i).toUpperCase());
                inv.setItem(slot++, createIcon(type));
            } catch (Exception ignored) { }
        }

        for (int i = 45; i < 54; i++) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(" ")));
        }

        inv.setItem(45, createItem(Material.ARROW, plugin.getConfigManager().getMenuComponent("entity-selector.btn-prev-page")));
        inv.setItem(46, createItem(Material.BARRIER, plugin.getConfigManager().getMenuComponent("entity-selector.btn-back")));
        inv.setItem(48, createItem(Material.TOTEM_OF_UNDYING, plugin.getConfigManager().getMenuComponent("entity-selector.btn-baby-toggle")));
        
        if (shop.getEntityType() == EntityType.VILLAGER) {
            inv.setItem(50, createItem(Material.LECTERN, plugin.getConfigManager().getMenuComponent("entity-selector.btn-change-prof")));
        }
        
        inv.setItem(53, createItem(Material.ARROW, plugin.getConfigManager().getMenuComponent("entity-selector.btn-next-page")));

        plugin.getShopManager().getEditingPlayers().put(player.getUniqueId(), shop);
        selectorPages.put(player.getUniqueId(), page);
        player.openInventory(inv);
    }

    private ItemStack createIcon(EntityType type) {
        Material material = Material.PIG_SPAWN_EGG;
        try {
            material = Material.valueOf(type.name() + "_SPAWN_EGG");
        } catch (Exception e) {
            if (type == EntityType.ARMOR_STAND) material = Material.ARMOR_STAND;
            else if (type == EntityType.PLAYER) material = Material.PLAYER_HEAD;
            else if (type == EntityType.IRON_GOLEM) material = Material.IRON_BLOCK;
            else if (type == EntityType.SNOW_GOLEM) material = Material.SNOW_BLOCK;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(type.name(), NamedTextColor.GREEN));
            item.setItemMeta(meta);
        }
        return item;
    }

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
        Component title = plugin.getConfigManager().getMenuComponent("entity-selector.title");
        if (event.getView().title().equals(title)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            PkShop shop = plugin.getShopManager().getEditingPlayers().get(player.getUniqueId());
            if (shop == null) return;
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < 45) {
                if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                    String typeName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());
                    try {
                        EntityType type = EntityType.valueOf(typeName.trim());
                        shop.setEntityType(type);
                        respawnShop(shop);
                        player.sendMessage(plugin.getConfigManager().getMessage("type-changed", "%type%", type.name()));
                        int page = selectorPages.getOrDefault(player.getUniqueId(), 0);
                        openMenu(player, shop, page);
                    } catch (Exception ignored) { }
                }
            } else if (slot == 45) {
                int page = selectorPages.getOrDefault(player.getUniqueId(), 0);
                if (page > 0) openMenu(player, shop, page - 1);
            } else if (slot == 53) {
                int page = selectorPages.getOrDefault(player.getUniqueId(), 0);
                openMenu(player, shop, page + 1);
            } else if (slot == 48) {
                shop.setBaby(!shop.isBaby());
                respawnShop(shop);
                player.sendMessage(plugin.getConfigManager().getMessage("baby-state", "%state%", shop.isBaby() ? "Sí" : "No"));
            } else if (slot == 50 && shop.getEntityType() == EntityType.VILLAGER) {
                plugin.getProfessionSelectorGUI().openMenu(player, shop);
            } else if (slot == 46) {
                plugin.getMainMenuGUI().openMenu(player, shop);
            }
        }
    }

    private void respawnShop(PkShop shop) {
        plugin.getShopManager().saveShops();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getShopEntityListener().removeEntity(shop);
            plugin.getShopEntityListener().spawnShop(shop);
        });
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Component title = plugin.getConfigManager().getMenuComponent("entity-selector.title");
        if (event.getView().title().equals(title)) {
            Player player = (Player) event.getPlayer();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.getOpenInventory().getTopInventory().getSize() != 54 && player.getOpenInventory().getTopInventory().getSize() != 27) {
                    plugin.getShopManager().getEditingPlayers().remove(player.getUniqueId());
                    selectorPages.remove(player.getUniqueId());
                }
            }, 1L);
        }
    }
}
