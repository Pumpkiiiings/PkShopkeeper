package com.pumpkings.pkshopkeepers.editor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
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

public class ProfessionSelectorGUI implements Listener {

    private final PkShopkeepers plugin;

    public ProfessionSelectorGUI(PkShopkeepers plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, PkShop shop) {
        Component title = plugin.getConfigManager().getMenuComponent("profession-selector.title");
        Inventory inv = Bukkit.createInventory(null, 27, title);

        int slot = 0;
        for (Profession prof : org.bukkit.Registry.VILLAGER_PROFESSION) {
            if (slot >= 18) break;
            inv.setItem(slot++, createIcon(prof));
        }

        for (int i = 18; i < 27; i++) {
            inv.setItem(i, createItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(" ")));
        }

        inv.setItem(22, createItem(Material.ARROW, plugin.getConfigManager().getMenuComponent("profession-selector.btn-back")));

        plugin.getShopManager().getEditingPlayers().put(player.getUniqueId(), shop);
        player.openInventory(inv);
    }

    private ItemStack createIcon(Profession prof) {
        Material mat = Material.VILLAGER_SPAWN_EGG;
        String name = prof.getKey().getKey().toUpperCase();
        
        switch (name) {
            case "ARMORER": mat = Material.BLAST_FURNACE; break;
            case "BUTCHER": mat = Material.SMOKER; break;
            case "CARTOGRAPHER": mat = Material.CARTOGRAPHY_TABLE; break;
            case "CLERIC": mat = Material.BREWING_STAND; break;
            case "FARMER": mat = Material.COMPOSTER; break;
            case "FISHERMAN": mat = Material.BARREL; break;
            case "FLETCHER": mat = Material.FLETCHING_TABLE; break;
            case "LEATHERWORKER": mat = Material.CAULDRON; break;
            case "LIBRARIAN": mat = Material.LECTERN; break;
            case "MASON": mat = Material.STONECUTTER; break;
            case "NITWIT": mat = Material.GREEN_WOOL; break;
            case "NONE": mat = Material.VILLAGER_SPAWN_EGG; break;
            case "SHEPHERD": mat = Material.LOOM; break;
            case "TOOLSMITH": mat = Material.SMITHING_TABLE; break;
            case "WEAPONSMITH": mat = Material.GRINDSTONE; break;
        }
        
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, NamedTextColor.GREEN));
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
        Component title = plugin.getConfigManager().getMenuComponent("profession-selector.title");
        if (event.getView().title().equals(title)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            PkShop shop = plugin.getShopManager().getEditingPlayers().get(player.getUniqueId());
            if (shop == null) return;
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < 18) {
                String profName = null;
                if (clicked.getItemMeta() != null && clicked.getItemMeta().hasDisplayName()) {
                    profName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName()).trim();
                }
                
                if (profName != null) {
                    try {
                        org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(profName.toLowerCase());
                        Profession prof = org.bukkit.Registry.VILLAGER_PROFESSION.get(key);
                        if (prof != null) {
                            shop.setVillagerProfession(prof);
                            respawnShop(shop);
                            player.sendMessage(plugin.getConfigManager().getMessage("profession-changed", "%profession%", profName));
                            openMenu(player, shop);
                        }
                    } catch (Exception ignored) { }
                }
            } else if (slot == 22) {
                plugin.getEntitySelectorGUI().openMenu(player, shop);
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
        Component title = plugin.getConfigManager().getMenuComponent("profession-selector.title");
        if (event.getView().title().equals(title)) {
            Player player = (Player) event.getPlayer();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.getOpenInventory().getTopInventory().getSize() <= 5) {
                    plugin.getShopManager().getEditingPlayers().remove(player.getUniqueId());
                }
            }, 1L);
        }
    }
}
