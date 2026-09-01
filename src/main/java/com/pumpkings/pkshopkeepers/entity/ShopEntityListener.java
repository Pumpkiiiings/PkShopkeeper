package com.pumpkings.pkshopkeepers.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.api.events.ShopOpenEvent;
import com.pumpkings.pkshopkeepers.api.events.ShopTradeEvent;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import com.pumpkings.pkshopkeepers.shop.PkTradeOffer;
import com.pumpkings.pkshopkeepers.shop.ShopManager;
import com.pumpkings.pkshopkeepers.utils.FoliaScheduler;

public class ShopEntityListener implements Listener {

    private final PkShopkeepers plugin;
    private final ShopManager shopManager;
    private final NamespacedKey shopIdKey;
    private final Map<UUID, PkShop> activeShopViewers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> lookingTasks = new ConcurrentHashMap<>();

    public ShopEntityListener(PkShopkeepers plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopIdKey = new NamespacedKey(plugin, "shop_id");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        spawnAllShops();
    }

    public void spawnAllShops() {
        for (PkShop shop : shopManager.getShops()) {
            Location location = shop.getLocation();
            if (location != null && location.getWorld() != null) {
                FoliaScheduler.runRegionTaskLater(plugin, location, () -> spawnShopNow(shop), 20L);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        for (Entity entity : chunk.getEntities()) {
            String shopId = getManagedShopId(entity);
            if (shopId == null) continue;
            PkShop shop = shopManager.getShop(shopId);
            if (shop == null || shop.getNpcId() != null || !isShopInChunk(shop, chunk)) {
                cancelLookTask(entity.getUniqueId());
                entity.remove();
            }
        }

        for (PkShop shop : shopManager.getShops()) {
            if (isShopInChunk(shop, chunk)) spawnShopNow(shop);
        }
    }

    private boolean isShopInChunk(PkShop shop, Chunk chunk) {
        Location location = shop.getLocation();
        return location != null && location.getWorld() != null
                && location.getWorld().equals(chunk.getWorld())
                && (location.getBlockX() >> 4) == chunk.getX()
                && (location.getBlockZ() >> 4) == chunk.getZ();
    }

    public CompletableFuture<Void> spawnShop(PkShop shop) {
        Location location = shop.getLocation();
        if (location == null || location.getWorld() == null || shop.getNpcId() != null) {
            return CompletableFuture.completedFuture(null);
        }
        return FoliaScheduler.runRegionTask(plugin, location, () -> spawnShopNow(shop));
    }

    public CompletableFuture<Void> removeEntity(PkShop shop) {
        Location location = shop.getLocation();
        UUID entityUuid = shop.getEntityUUID();
        shop.setEntityUUID(null);
        if (location == null || location.getWorld() == null) return CompletableFuture.completedFuture(null);
        return FoliaScheduler.runRegionTask(plugin, location,
                () -> removeEntityNow(shop.getId(), entityUuid, location));
    }

    public CompletableFuture<Void> respawnShop(PkShop shop) {
        Location location = shop.getLocation();
        UUID entityUuid = shop.getEntityUUID();
        shop.setEntityUUID(null);
        if (location == null || location.getWorld() == null) return CompletableFuture.completedFuture(null);
        return FoliaScheduler.runRegionTask(plugin, location, () -> {
            removeEntityNow(shop.getId(), entityUuid, location);
            spawnShopNow(shop);
        });
    }

    public void moveShop(PkShop shop, Location destination) {
        Location oldLocation = shop.getLocation();
        UUID oldEntityUuid = shop.getEntityUUID();
        shop.setEntityUUID(null);
        shop.setLocation(destination);
        shopManager.saveShops();
        if (oldLocation != null && oldLocation.getWorld() != null) {
            FoliaScheduler.runRegionTask(plugin, oldLocation,
                    () -> removeEntityNow(shop.getId(), oldEntityUuid, oldLocation));
        }
        spawnShop(shop);
    }

    public CompletableFuture<Integer> repairShop(PkShop shop) {
        Location location = shop.getLocation();
        if (location == null || location.getWorld() == null || shop.getNpcId() != null) {
            return CompletableFuture.completedFuture(0);
        }
        AtomicInteger removed = new AtomicInteger();
        return FoliaScheduler.runRegionTask(plugin, location, () -> {
            removed.set(Math.max(0, removeEntityNow(shop.getId(), shop.getEntityUUID(), location) - 1));
            shop.setEntityUUID(null);
            spawnShopNow(shop);
        }).thenApply(ignored -> removed.get());
    }

    private int removeEntityNow(String shopId, UUID entityUuid, Location location) {
        Chunk chunk = getLoadedChunk(location);
        if (chunk == null) return 0;
        int removed = 0;
        for (Entity entity : chunk.getEntities()) {
            if ((entityUuid != null && entityUuid.equals(entity.getUniqueId()))
                    || shopId.equals(getManagedShopId(entity))) {
                cancelLookTask(entity.getUniqueId());
                entity.remove();
                removed++;
            }
        }
        return removed;
    }

    private void spawnShopNow(PkShop shop) {
        if (shop.getNpcId() != null) return;
        Location location = shop.getLocation();
        if (location == null || location.getWorld() == null) return;
        Chunk chunk = getLoadedChunk(location);
        if (chunk == null) return;
        if (!shop.getEntityType().isSpawnable()) {
            plugin.getLogger().warning("Cannot spawn shop " + shop.getId() + ": " + shop.getEntityType() + " is not spawnable.");
            return;
        }

        Entity managedEntity = null;
        for (Entity entity : chunk.getEntities()) {
            boolean matchesId = shop.getId().equals(getManagedShopId(entity));
            boolean matchesUuid = shop.getEntityUUID() != null && shop.getEntityUUID().equals(entity.getUniqueId());
            if (!matchesId && !matchesUuid) continue;
            if (managedEntity == null && entity.getType() == shop.getEntityType()) {
                managedEntity = entity;
            } else {
                cancelLookTask(entity.getUniqueId());
                entity.remove();
            }
        }

        if (managedEntity == null) managedEntity = location.getWorld().spawnEntity(location, shop.getEntityType());
        managedEntity.getPersistentDataContainer().set(shopIdKey, PersistentDataType.STRING, shop.getId());
        configureEntity(managedEntity, shop);
        shop.setEntityUUID(managedEntity.getUniqueId());
        startLookAtTask(managedEntity);
    }

    private Chunk getLoadedChunk(Location location) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        if (!location.getWorld().isChunkLoaded(chunkX, chunkZ)) return null;
        return location.getWorld().getChunkAt(chunkX, chunkZ);
    }

    private void configureEntity(Entity entity, PkShop shop) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.setAI(false);
            livingEntity.setCollidable(false);
            livingEntity.setInvulnerable(true);
            var movementSpeed = org.bukkit.Registry.ATTRIBUTE.get(
                    org.bukkit.NamespacedKey.minecraft("generic.movement_speed"));
            if (movementSpeed != null && livingEntity.getAttribute(movementSpeed) != null) {
                livingEntity.getAttribute(movementSpeed).setBaseValue(0.0);
            }
        }
        entity.customName(plugin.getConfigManager().parseString(shop.getName()));
        entity.setCustomNameVisible(plugin.getConfigManager().getBoolean("settings.always-show-nameplates", false));
        entity.setSilent(plugin.getConfigManager().getBoolean("settings.silence-shop-entities", true));
        entity.setGravity(!plugin.getConfigManager().getBoolean("settings.disable-gravity", false));
        entity.setPersistent(true);

        if (entity instanceof org.bukkit.entity.Ageable ageable) {
            if (shop.isBaby()) ageable.setBaby(); else ageable.setAdult();
        }
        if (entity instanceof Villager villager) {
            villager.setProfession(shop.getVillagerProfession());
            villager.setVillagerType(shop.getVillagerType());
            villager.setVillagerLevel(Math.max(1, Math.min(5, shop.getVillagerLevel())));
        }
    }

    private void startLookAtTask(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;
        if (!plugin.getConfigManager().getBoolean("settings.look-at-players", true)) return;
        if (lookingTasks.containsKey(entity.getUniqueId())) return;

        double radius = Math.max(0.0, plugin.getConfigManager().getDouble("settings.look-at-radius", 5.0));
        ScheduledTask scheduledTask = FoliaScheduler.runEntityTaskTimer(plugin, entity, task -> {
            if (!entity.isValid() || entity.isDead()) {
                lookingTasks.remove(entity.getUniqueId(), task);
                task.cancel();
                return;
            }
            Player nearest = null;
            double nearestDistance = radius * radius;
            for (Entity nearby : entity.getNearbyEntities(radius, radius, radius)) {
                if (!(nearby instanceof Player player) || player.getGameMode() == GameMode.SPECTATOR) continue;
                double distance = player.getLocation().distanceSquared(entity.getLocation());
                if (distance < nearestDistance) {
                    nearest = player;
                    nearestDistance = distance;
                }
            }
            if (nearest != null) {
                Location entityLocation = entity.getLocation();
                Location eyeLocation = nearest.getEyeLocation();
                double dx = eyeLocation.getX() - entityLocation.getX();
                double dy = eyeLocation.getY() - (entityLocation.getY() + entity.getHeight() * 0.85);
                double dz = eyeLocation.getZ() - entityLocation.getZ();
                double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                float pitch = (float) Math.toDegrees(Math.atan2(-dy, horizontalDistance));
                entity.setRotation(yaw, pitch);
            }
        }, () -> lookingTasks.remove(entity.getUniqueId()), 20L, 3L);
        if (scheduledTask == null) return;
        ScheduledTask previous = lookingTasks.putIfAbsent(entity.getUniqueId(), scheduledTask);
        if (previous != null && scheduledTask != null) scheduledTask.cancel();
    }

    private void cancelLookTask(UUID entityUuid) {
        ScheduledTask task = lookingTasks.remove(entityUuid);
        if (task != null) task.cancel();
    }

    public boolean isWithinInteractionRadius(Player player, PkShop shop) {
        Location shopLocation = shop.getLocation();
        if (shopLocation == null || shopLocation.getWorld() == null
                || !shopLocation.getWorld().equals(player.getWorld())) return false;
        double radius = Math.max(0.0,
                plugin.getConfigManager().getDouble("settings.shop-interaction-radius", 5.0));
        return player.getLocation().distanceSquared(shopLocation) <= radius * radius;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        PkShop shop = getManagedShop(event.getRightClicked());
        if (shop == null || !isWithinInteractionRadius(event.getPlayer(), shop)) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (player.isSneaking() && player.hasPermission("pkshopkeepers.admin")) {
            plugin.getMainMenuGUI().openMenu(player, shop);
        } else {
            openShop(player, shop);
        }
    }

    @EventHandler
    public void onEntityInteractAt(PlayerInteractAtEntityEvent event) {
        if (getManagedShop(event.getRightClicked()) != null) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (getManagedShop(event.getEntity()) != null) event.setCancelled(true);
    }

    private PkShop getManagedShop(Entity entity) {
        String id = getManagedShopId(entity);
        if (id != null) return shopManager.getShop(id);
        UUID uuid = entity.getUniqueId();
        for (PkShop shop : shopManager.getShops()) {
            if (uuid.equals(shop.getEntityUUID())) return shop;
        }
        return null;
    }

    private String getManagedShopId(Entity entity) {
        return entity.getPersistentDataContainer().get(shopIdKey, PersistentDataType.STRING);
    }

    public void openShop(Player player, PkShop shop) {
        FoliaScheduler.runEntityTask(plugin, player, () -> openShopNow(player, shop));
    }

    private void openShopNow(Player player, PkShop shop) {
        if (!player.isValid()) return;
        ShopOpenEvent openEvent = new ShopOpenEvent(player, shop);
        Bukkit.getPluginManager().callEvent(openEvent);
        if (openEvent.isCancelled()) return;

        Merchant merchant = Bukkit.createMerchant(plugin.getConfigManager().parseString(shop.getName()));
        List<MerchantRecipe> recipes = new ArrayList<>();
        for (PkTradeOffer offer : shop.getOffers()) {
            if (offer.getItem1() == null || offer.getResult() == null
                    || offer.getItem1().getType() == Material.AIR || offer.getResult().getType() == Material.AIR) continue;
            MerchantRecipe recipe = new MerchantRecipe(offer.getResult().clone(), Integer.MAX_VALUE);
            recipe.addIngredient(offer.getItem1().clone());
            if (offer.getItem2() != null && offer.getItem2().getType() != Material.AIR) {
                recipe.addIngredient(offer.getItem2().clone());
            }
            recipe.setExperienceReward(false);
            recipes.add(recipe);
        }
        merchant.setRecipes(recipes);
        player.openMerchant(merchant, true);
        activeShopViewers.put(player.getUniqueId(), shop);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        activeShopViewers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTradeClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory() instanceof MerchantInventory merchantInventory)) return;
        PkShop shop = activeShopViewers.get(player.getUniqueId());
        if (shop == null || event.getRawSlot() != 2 || event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR) return;

        MerchantRecipe selectedRecipe = merchantInventory.getSelectedRecipe();
        int index = selectedRecipe == null ? -1 : merchantInventory.getMerchant().getRecipes().indexOf(selectedRecipe);
        ShopTradeEvent tradeEvent = new ShopTradeEvent(player, shop, event.getCurrentItem().clone(), index);
        Bukkit.getPluginManager().callEvent(tradeEvent);
        if (tradeEvent.isCancelled()) event.setCancelled(true);
    }

    public void clearRuntimeState() {
        activeShopViewers.clear();
        lookingTasks.values().forEach(ScheduledTask::cancel);
        lookingTasks.clear();
    }

    public void prepareReload() {
        clearRuntimeState();
    }
}
