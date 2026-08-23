package com.nisovin.shopkeepers.pkshopkeepers.shop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;

public class ShopManager {

    private final PkShopkeepers plugin;
    private final File shopsFile;
    private FileConfiguration shopsConfig;
    private final Map<String, PkShop> shops = new HashMap<>();
    private final Map<java.util.UUID, PkShop> editingPlayers = new HashMap<>();
    private final Map<java.util.UUID, PkShop> renamingPlayers = new HashMap<>();

    public ShopManager(PkShopkeepers plugin) {
        this.plugin = plugin;
        this.shopsFile = new File(plugin.getDataFolder(), "shops.yml");
    }

    public void loadShops() {
        if (!shopsFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                shopsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        shopsConfig = YamlConfiguration.loadConfiguration(shopsFile);
        shops.clear();

        if (shopsConfig.contains("shops")) {
            for (String key : shopsConfig.getConfigurationSection("shops").getKeys(false)) {
                ConfigurationSection config = shopsConfig.getConfigurationSection("shops." + key);
                PkShop shop = new PkShop(key, config);
                shops.put(key, shop);
            }
        }
        plugin.getLogger().info("Loaded " + shops.size() + " shops.");
    }

    public void saveShops() {
        shopsConfig.set("shops", null);
        ConfigurationSection shopsSection = shopsConfig.createSection("shops");
        for (PkShop shop : shops.values()) {
            shop.saveToConfig(shopsSection.createSection(shop.getId()));
        }
        
        try {
            shopsConfig.save(shopsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addShop(PkShop shop) {
        shops.put(shop.getId(), shop);
    }
    
    public String getNextId() {
        int maxId = 0;
        for (String key : shops.keySet()) {
            try {
                int id = Integer.parseInt(key);
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException ignored) {}
        }
        return String.valueOf(maxId + 1);
    }

    public PkShop getShop(String id) {
        return shops.get(id);
    }

    public java.util.Collection<PkShop> getShops() {
        return shops.values();
    }

    public void removeShop(String id) {
        removeShop(id, true);
    }
    
    public void removeShop(String id, boolean save) {
        shops.remove(id);
        if (save) saveShops();
    }

    public Map<java.util.UUID, PkShop> getEditingPlayers() {
        return editingPlayers;
    }

    public Map<java.util.UUID, PkShop> getRenamingPlayers() {
        return renamingPlayers;
    }
}
