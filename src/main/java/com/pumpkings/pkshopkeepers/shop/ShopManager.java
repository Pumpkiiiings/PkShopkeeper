package com.pumpkings.pkshopkeepers.shop;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.api.events.ShopCreateEvent;
import com.pumpkings.pkshopkeepers.api.events.ShopDeleteEvent;

public class ShopManager {

    private final PkShopkeepers plugin;
    private final File shopsFile;
    private final Map<String, PkShop> shops = new ConcurrentHashMap<>();
    private final Map<java.util.UUID, PkShop> editingPlayers = new ConcurrentHashMap<>();
    private final Map<java.util.UUID, PkShop> renamingPlayers = new ConcurrentHashMap<>();
    private final AtomicInteger nextNumericId = new AtomicInteger(1);
    private final Object persistenceLock = new Object();
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "PkShopkeepers-ShopSaver");
        thread.setDaemon(true);
        return thread;
    });
    private CompletableFuture<Void> pendingSave = CompletableFuture.completedFuture(null);

    public ShopManager(PkShopkeepers plugin) {
        this.plugin = plugin;
        this.shopsFile = new File(plugin.getDataFolder(), "shops.yml");
    }

    public void loadShops() {
        flushPendingSaves();
        if (!shopsFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                shopsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create shops.yml: " + e.getMessage());
                return;
            }
        }
        FileConfiguration shopsConfig = YamlConfiguration.loadConfiguration(shopsFile);
        shops.clear();
        editingPlayers.clear();
        renamingPlayers.clear();

        int maxNumericId = 0;

        ConfigurationSection root = shopsConfig.getConfigurationSection("shops");
        if (root != null) {
            for (String key : root.getKeys(false)) {
                ConfigurationSection config = shopsConfig.getConfigurationSection("shops." + key);
                if (config == null) continue;
                PkShop shop = new PkShop(key, config);
                shops.put(key, shop);
                try {
                    maxNumericId = Math.max(maxNumericId, Integer.parseInt(key));
                } catch (NumberFormatException ignored) {
                    // Legacy UUID/string IDs stay readable, but all new shops use numeric IDs.
                }
            }
        }
        nextNumericId.set(maxNumericId + 1);
        plugin.getLogger().info("Loaded " + shops.size() + " shops.");
    }

    public void saveShops() {
        String serialized;
        synchronized (persistenceLock) {
            YamlConfiguration snapshot = new YamlConfiguration();
            ConfigurationSection shopsSection = snapshot.createSection("shops");
            for (PkShop shop : shops.values()) {
                shop.saveToConfig(shopsSection.createSection(shop.getId()));
            }
            serialized = snapshot.saveToString();
            pendingSave = pendingSave.handle((ignored, error) -> null)
                    .thenRunAsync(() -> writeSnapshot(serialized), saveExecutor);
        }
    }

    private void writeSnapshot(String serialized) {
        File tempFile = new File(shopsFile.getParentFile(), shopsFile.getName() + ".tmp");
        try {
            Files.createDirectories(shopsFile.getParentFile().toPath());
            Files.writeString(tempFile.toPath(), serialized, StandardCharsets.UTF_8);
            try {
                Files.move(tempFile.toPath(), shopsFile.toPath(),
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempFile.toPath(), shopsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save shops.yml: " + e.getMessage());
        }
    }

    public void flushPendingSaves() {
        CompletableFuture<Void> save;
        synchronized (persistenceLock) {
            save = pendingSave;
        }
        try {
            save.join();
        } catch (RuntimeException e) {
            plugin.getLogger().severe("Could not finish saving shops.yml: " + e.getMessage());
        }
    }

    public void shutdown() {
        saveShops();
        flushPendingSaves();
        saveExecutor.shutdown();
    }

    public void addShop(PkShop shop) {
        shops.put(shop.getId(), shop);
        try {
            nextNumericId.accumulateAndGet(Integer.parseInt(shop.getId()) + 1, Math::max);
        } catch (NumberFormatException ignored) {
            // String IDs from older data do not affect the numeric sequence.
        }
        Bukkit.getPluginManager().callEvent(new ShopCreateEvent(shop));
    }
    
    public String getNextId() {
        return String.valueOf(nextNumericId.getAndIncrement());
    }

    public PkShop getShop(String id) {
        return shops.get(id);
    }

    public Collection<PkShop> getShops() {
        return List.copyOf(shops.values());
    }

    public long countShopsOwnedBy(UUID ownerUuid) {
        if (ownerUuid == null) return 0L;
        return shops.values().stream().filter(shop -> ownerUuid.equals(shop.getOwnerUUID())).count();
    }

    public void removeShop(String id) {
        removeShop(id, true);
    }
    
    public void removeShop(String id, boolean save) {
        PkShop shop = shops.remove(id);
        if (shop != null) {
            Bukkit.getPluginManager().callEvent(new ShopDeleteEvent(shop));
        }
        if (save) saveShops();
    }

    public Map<java.util.UUID, PkShop> getEditingPlayers() {
        return editingPlayers;
    }

    public Map<java.util.UUID, PkShop> getRenamingPlayers() {
        return renamingPlayers;
    }
}
