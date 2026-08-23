package com.pumpkings.pkshopkeepers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.pumpkings.pkshopkeepers.shop.ShopManager;

public class PkShopkeepers extends JavaPlugin implements Listener {

    private static PkShopkeepers instance;
    private ShopManager shopManager;
    private com.pumpkings.pkshopkeepers.utils.ConfigManager configManager;
    private com.pumpkings.pkshopkeepers.entity.ShopEntityListener shopEntityListener;
    private com.pumpkings.pkshopkeepers.editor.ShopEditorListener shopEditorListener;
    private com.pumpkings.pkshopkeepers.editor.MainMenuGUI mainMenuGUI;
    private com.pumpkings.pkshopkeepers.editor.EntitySelectorGUI entitySelectorGUI;
    private com.pumpkings.pkshopkeepers.editor.ProfessionSelectorGUI professionSelectorGUI;

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new com.pumpkings.pkshopkeepers.utils.ConfigManager(this);
        this.shopManager = new ShopManager(this);
        this.shopManager.loadShops();

        this.shopEntityListener = new com.pumpkings.pkshopkeepers.entity.ShopEntityListener(this, shopManager);
        new com.pumpkings.pkshopkeepers.entity.ShopCreationListener(this);
        
        boolean fancyNpcs = false;
        if (getServer().getPluginManager().getPlugin("FancyNpcs") != null) {
            new com.pumpkings.pkshopkeepers.compat.FancyNpcsInteractListener(this);
            fancyNpcs = true;
        }
        
        this.shopEditorListener = new com.pumpkings.pkshopkeepers.editor.ShopEditorListener(this);
        this.mainMenuGUI = new com.pumpkings.pkshopkeepers.editor.MainMenuGUI(this);
        this.entitySelectorGUI = new com.pumpkings.pkshopkeepers.editor.EntitySelectorGUI(this);
        this.professionSelectorGUI = new com.pumpkings.pkshopkeepers.editor.ProfessionSelectorGUI(this);
        getServer().getPluginManager().registerEvents(this, this);

        printAsciiBanner(fancyNpcs);
        
        new com.pumpkings.pkshopkeepers.commands.CommandManager(this);
    }

    private void printAsciiBanner(boolean fancyNpcs) {
        String[] ascii = {
            "                                                                                       ",
            "▄▄▄▄▄▄▄           ▄▄▄▄▄▄▄ ▄▄                                                           ",
            "███▀▀███▄ ▄▄     █████▀▀▀ ██                ▄▄                                         ",
            "███▄▄███▀ ██ ▄█▀  ▀████▄  ████▄ ▄███▄ ████▄ ██ ▄█▀ ▄█▀█▄ ▄█▀█▄ ████▄ ▄█▀█▄ ████▄ ▄█▀▀▀ ",
            "███▀▀▀▀   ████      ▀████ ██ ██ ██ ██ ██ ██ ████   ██▄█▀ ██▄█▀ ██ ██ ██▄█▀ ██ ▀▀ ▀███▄ ",
            "███       ██ ▀█▄ ███████▀ ██ ██ ▀███▀ ████▀ ██ ▀█▄ ▀█▄▄▄ ▀█▄▄▄ ████▀ ▀█▄▄▄ ██    ▄▄▄█▀ ",
            "                                      ██                       ██                      ",
            "                                      ▀▀                       ▀▀                      "
        };
        
        for (String line : ascii) {
            getServer().getConsoleSender().sendMessage("§e" + line);
        }
        
        String version = getPluginMeta().getVersion();
        String mcVersion = getServer().getMinecraftVersion();
        int shopsLoaded = shopManager.getShops().size();
        String fancyText = fancyNpcs ? "§aDetected" : "§cNo Detected";
        
        getServer().getConsoleSender().sendMessage("§fVersion: §a" + version);
        getServer().getConsoleSender().sendMessage("§fMinecraft Version: §b" + mcVersion);
        getServer().getConsoleSender().sendMessage("§fShopkeepers loaded: §d" + shopsLoaded);
        getServer().getConsoleSender().sendMessage("§fFancyNpc: " + fancyText);
        getServer().getConsoleSender().sendMessage("§7¡Thanks for use my plugin!");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (shopManager.getRenamingPlayers().containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            com.pumpkings.pkshopkeepers.shop.PkShop shop = shopManager.getRenamingPlayers().remove(event.getPlayer().getUniqueId());
            String newName = event.getMessage().replace("&", "§");
            
            getServer().getScheduler().runTask(this, () -> {
                shop.setName(newName);
                shopManager.saveShops();
                getServer().getPluginManager().callEvent(new org.bukkit.event.world.ChunkUnloadEvent(shop.getLocation().getChunk()));
                getServer().getPluginManager().callEvent(new org.bukkit.event.world.ChunkLoadEvent(shop.getLocation().getChunk(), false));
                event.getPlayer().sendMessage(configManager.getMessage("name-changed", "%name%", newName));
                mainMenuGUI.openMenu(event.getPlayer(), shop);
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        shopManager.getEditingPlayers().remove(uuid);
        shopManager.getRenamingPlayers().remove(uuid);
    }

    @Override
    public void onDisable() {
        if (shopManager != null) {
            shopManager.saveShops();
        }
    }

    public static PkShopkeepers getInstance() {
        return instance;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public com.pumpkings.pkshopkeepers.editor.ShopEditorListener getShopEditorListener() {
        return shopEditorListener;
    }

    public com.pumpkings.pkshopkeepers.editor.MainMenuGUI getMainMenuGUI() {
        return mainMenuGUI;
    }

    public com.pumpkings.pkshopkeepers.editor.EntitySelectorGUI getEntitySelectorGUI() {
        return entitySelectorGUI;
    }

    public com.pumpkings.pkshopkeepers.editor.ProfessionSelectorGUI getProfessionSelectorGUI() {
        return professionSelectorGUI;
    }

    public com.pumpkings.pkshopkeepers.utils.ConfigManager getConfigManager() {
        return configManager;
    }

    public com.pumpkings.pkshopkeepers.entity.ShopEntityListener getShopEntityListener() {
        return shopEntityListener;
    }
}
