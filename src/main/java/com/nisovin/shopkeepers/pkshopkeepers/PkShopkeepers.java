package com.nisovin.shopkeepers.pkshopkeepers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.nisovin.shopkeepers.pkshopkeepers.shop.ShopManager;

public class PkShopkeepers extends JavaPlugin implements Listener {

    private static PkShopkeepers instance;
    private ShopManager shopManager;
    private com.nisovin.shopkeepers.pkshopkeepers.utils.ConfigManager configManager;
    private com.nisovin.shopkeepers.pkshopkeepers.entity.ShopEntityListener shopEntityListener;
    private com.nisovin.shopkeepers.pkshopkeepers.editor.ShopEditorListener shopEditorListener;
    private com.nisovin.shopkeepers.pkshopkeepers.editor.MainMenuGUI mainMenuGUI;
    private com.nisovin.shopkeepers.pkshopkeepers.editor.EntitySelectorGUI entitySelectorGUI;
    private com.nisovin.shopkeepers.pkshopkeepers.editor.ProfessionSelectorGUI professionSelectorGUI;

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new com.nisovin.shopkeepers.pkshopkeepers.utils.ConfigManager(this);
        this.shopManager = new ShopManager(this);
        this.shopManager.loadShops();

        this.shopEntityListener = new com.nisovin.shopkeepers.pkshopkeepers.entity.ShopEntityListener(this, shopManager);
        new com.nisovin.shopkeepers.pkshopkeepers.entity.ShopCreationListener(this);
        
        boolean fancyNpcs = false;
        if (getServer().getPluginManager().getPlugin("FancyNpcs") != null) {
            new com.nisovin.shopkeepers.pkshopkeepers.compat.FancyNpcsInteractListener(this);
            fancyNpcs = true;
        }
        
        this.shopEditorListener = new com.nisovin.shopkeepers.pkshopkeepers.editor.ShopEditorListener(this);
        this.mainMenuGUI = new com.nisovin.shopkeepers.pkshopkeepers.editor.MainMenuGUI(this);
        this.entitySelectorGUI = new com.nisovin.shopkeepers.pkshopkeepers.editor.EntitySelectorGUI(this);
        this.professionSelectorGUI = new com.nisovin.shopkeepers.pkshopkeepers.editor.ProfessionSelectorGUI(this);
        getServer().getPluginManager().registerEvents(this, this);

        printAsciiBanner(fancyNpcs);
        
        new com.nisovin.shopkeepers.pkshopkeepers.commands.CommandManager(this);
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
            com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop shop = shopManager.getRenamingPlayers().remove(event.getPlayer().getUniqueId());
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

    public com.nisovin.shopkeepers.pkshopkeepers.editor.ShopEditorListener getShopEditorListener() {
        return shopEditorListener;
    }

    public com.nisovin.shopkeepers.pkshopkeepers.editor.MainMenuGUI getMainMenuGUI() {
        return mainMenuGUI;
    }

    public com.nisovin.shopkeepers.pkshopkeepers.editor.EntitySelectorGUI getEntitySelectorGUI() {
        return entitySelectorGUI;
    }

    public com.nisovin.shopkeepers.pkshopkeepers.editor.ProfessionSelectorGUI getProfessionSelectorGUI() {
        return professionSelectorGUI;
    }

    public com.nisovin.shopkeepers.pkshopkeepers.utils.ConfigManager getConfigManager() {
        return configManager;
    }

    public com.nisovin.shopkeepers.pkshopkeepers.entity.ShopEntityListener getShopEntityListener() {
        return shopEntityListener;
    }
}
