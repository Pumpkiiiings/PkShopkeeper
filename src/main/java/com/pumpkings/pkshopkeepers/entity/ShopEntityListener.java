package com.pumpkings.pkshopkeepers.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

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
    private final Map<UUID, PkShop> activeShopViewers = new HashMap<>();
    private final java.util.Set<UUID> lookingEntities = new java.util.HashSet<>();

    public ShopEntityListener(PkShopkeepers plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Retrasar el spawn inicial para asegurar que los mundos estén cargados
        FoliaScheduler.runGlobalTaskLater(plugin, this::spawnAllShops, 20L);
    }

    @EventHandler
    public void onChunkLoad(org.bukkit.event.world.ChunkLoadEvent event) {
        String worldName = event.getChunk().getWorld().getName();
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        
        for (PkShop shop : shopManager.getShops()) {
            if (shop.getLocation() != null && shop.getLocation().getWorld().getName().equals(worldName)) {
                int shopChunkX = shop.getLocation().getBlockX() >> 4;
                int shopChunkZ = shop.getLocation().getBlockZ() >> 4;
                if (shopChunkX == chunkX && shopChunkZ == chunkZ) {
                    spawnShop(shop);
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnload(org.bukkit.event.world.ChunkUnloadEvent event) {
        String worldName = event.getChunk().getWorld().getName();
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        
        for (PkShop shop : shopManager.getShops()) {
            if (shop.getLocation() != null && shop.getLocation().getWorld().getName().equals(worldName)) {
                int shopChunkX = shop.getLocation().getBlockX() >> 4;
                int shopChunkZ = shop.getLocation().getBlockZ() >> 4;
                if (shopChunkX == chunkX && shopChunkZ == chunkZ) {
                    removeEntity(shop);
                }
            }
        }
    }

    public void removeEntity(PkShop shop) {
        if (shop.getLocation() != null && shop.getLocation().getWorld() != null) {
            for (Entity e : shop.getLocation().getWorld().getEntities()) {
                if (e.getUniqueId().equals(shop.getEntityUUID())) {
                    e.remove();
                    break;
                }
            }
        }
        shop.setEntityUUID(null);
    }

    public void spawnShop(PkShop shop) {
        if (shop.getNpcId() != null) return;
        
        Location loc = shop.getLocation();
        if (loc == null || loc.getWorld() == null) return;
        
        boolean found = false;
        for (Entity e : shop.getLocation().getWorld().getEntities()) {
            if (e.getUniqueId().equals(shop.getEntityUUID())) {
                found = true;
                if (e instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) e;
                    le.setAI(false);
                    le.setCollidable(false);
                    org.bukkit.attribute.Attribute movSpeedAttr = org.bukkit.Registry.ATTRIBUTE.get(org.bukkit.NamespacedKey.minecraft("generic.movement_speed"));
                    if (movSpeedAttr != null) { org.bukkit.attribute.AttributeInstance speed = le.getAttribute(movSpeedAttr); if (speed != null) speed.setBaseValue(0.0); }
                    le.setInvulnerable(true);
                    le.setSilent(true);
                    e.customName(plugin.getConfigManager().parseString(shop.getName()));
                    e.setCustomNameVisible(plugin.getConfigManager().getBoolean("settings.always-show-nameplates", false));
                }
                startLookAtTask(e);
                break;
            }
        }
        
        if (!found && shop.getLocation().getChunk().isLoaded()) {
            Entity entity = shop.getLocation().getWorld().spawnEntity(shop.getLocation(), shop.getEntityType());
            if (entity instanceof org.bukkit.entity.LivingEntity) {
                org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) entity;
                le.setAI(false);
                le.setCollidable(false);
                org.bukkit.attribute.Attribute movSpeedAttr = org.bukkit.Registry.ATTRIBUTE.get(org.bukkit.NamespacedKey.minecraft("generic.movement_speed"));
                if (movSpeedAttr != null) { org.bukkit.attribute.AttributeInstance speed = le.getAttribute(movSpeedAttr); if (speed != null) speed.setBaseValue(0.0); }
                le.setInvulnerable(true);
                entity.setCustomNameVisible(plugin.getConfigManager().getBoolean("settings.always-show-nameplates", false));
            }
            entity.setSilent(plugin.getConfigManager().getBoolean("settings.silence-shop-entities", true));
            entity.setGravity(!plugin.getConfigManager().getBoolean("settings.disable-gravity", false));
            
            // Set name to component using ConfigManager
            entity.customName(plugin.getConfigManager().parseString(shop.getName()));
            entity.setPersistent(true);
            
            if (entity instanceof org.bukkit.entity.Ageable) {
                if (shop.isBaby()) {
                    ((org.bukkit.entity.Ageable) entity).setBaby();
                } else {
                    ((org.bukkit.entity.Ageable) entity).setAdult();
                }
            } else if (entity instanceof org.bukkit.entity.Zombie) {
                ((org.bukkit.entity.Zombie) entity).setBaby(shop.isBaby());
            }

            if (entity instanceof org.bukkit.entity.Villager) {
                org.bukkit.entity.Villager v = (org.bukkit.entity.Villager) entity;
                v.setProfession(shop.getVillagerProfession());
                v.setVillagerType(shop.getVillagerType());
                v.setVillagerLevel(shop.getVillagerLevel());
            }
            
            shop.setEntityUUID(entity.getUniqueId());
            startLookAtTask(entity);
        }
    }

    private void startLookAtTask(Entity entity) {
        if (!plugin.getConfigManager().getBoolean("settings.look-at-players", true)) return;
        if (lookingEntities.contains(entity.getUniqueId())) return;
        
        lookingEntities.add(entity.getUniqueId());
        double radius = plugin.getConfig().getDouble("settings.look-at-radius", 5.0);
        
        FoliaScheduler.runEntityTaskTimer(plugin, entity, () -> {
            if (!entity.isValid() || entity.isDead()) {
                lookingEntities.remove(entity.getUniqueId());
                return;
            }
            
            Player nearest = null;
            double nearestDist = radius * radius;
            
            for (Entity nearby : entity.getNearbyEntities(radius, radius, radius)) {
                if (nearby instanceof Player p) {
                    if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
                    double dist = p.getLocation().distanceSquared(entity.getLocation());
                    if (dist < nearestDist) {
                        nearest = p;
                        nearestDist = dist;
                    }
                }
            }
            
            if (nearest != null) {
                Location loc = entity.getLocation();
                Location pLoc = nearest.getEyeLocation();
                double dx = pLoc.getX() - loc.getX();
                double dy = pLoc.getY() - (loc.getY() + entity.getHeight() * 0.85);
                double dz = pLoc.getZ() - loc.getZ();
                
                double distanceXZ = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                float pitch = (float) Math.toDegrees(Math.atan2(-dy, distanceXZ));
                
                entity.setRotation(yaw, pitch);
            }
        }, 20L, 3L);
    }

    public void spawnAllShops() {
        for (PkShop shop : shopManager.getShops()) {
            if (shop.getLocation() != null && shop.getLocation().getChunk().isLoaded()) {
                spawnShop(shop);
            }
        }
        shopManager.saveShops();
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        UUID uuid = entity.getUniqueId();
        
        PkShop foundShop = null;
        for (PkShop shop : shopManager.getShops()) {
            if (shop.getEntityUUID() != null && shop.getEntityUUID().equals(uuid)) {
                foundShop = shop;
                break;
            }
        }

        if (foundShop != null) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            
            if (player.isSneaking() && player.hasPermission("pkshopkeepers.admin")) {
                plugin.getMainMenuGUI().openMenu(player, foundShop);
            } else {
                openShop(player, foundShop);
            }
        }
    }

    @EventHandler
    public void onEntityInteractAt(org.bukkit.event.player.PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        UUID uuid = entity.getUniqueId();
        
        for (PkShop shop : shopManager.getShops()) {
            if (shop.getEntityUUID() != null && shop.getEntityUUID().equals(uuid)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        for (PkShop shop : shopManager.getShops()) {
            if (shop.getEntityUUID().equals(entity.getUniqueId())) {
                event.setCancelled(true);
                break;
            }
        }
    }

    public void openShop(Player player, PkShop shop) {
        ShopOpenEvent openEvent = new ShopOpenEvent(player, shop);
        Bukkit.getPluginManager().callEvent(openEvent);
        if (openEvent.isCancelled()) return;

        Merchant merchant = Bukkit.createMerchant(shop.getName());
        List<MerchantRecipe> recipes = new ArrayList<>();
        
        for (PkTradeOffer offer : shop.getOffers()) {
            MerchantRecipe recipe = new MerchantRecipe(offer.getResult(), Integer.MAX_VALUE);
            recipe.addIngredient(offer.getItem1());
            if (offer.getItem2() != null) {
                recipe.addIngredient(offer.getItem2());
            }
            recipe.setExperienceReward(false);
            recipes.add(recipe);
        }
        
        merchant.setRecipes(recipes);
        player.openMerchant(merchant, true);
        activeShopViewers.put(player.getUniqueId(), shop);
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        activeShopViewers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTradeClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        if (event.getInventory() instanceof org.bukkit.inventory.MerchantInventory merchantInv) {
            PkShop shop = activeShopViewers.get(player.getUniqueId());
            if (shop == null) return;
            
            if (event.getRawSlot() == 2 && event.getCurrentItem() != null && event.getCurrentItem().getType() != org.bukkit.Material.AIR) {
                // Determine which trade is selected
                org.bukkit.inventory.MerchantRecipe recipe = merchantInv.getSelectedRecipe();
                int index = -1;
                if (recipe != null) {
                    for (int i = 0; i < merchantInv.getMerchant().getRecipes().size(); i++) {
                        if (merchantInv.getMerchant().getRecipes().get(i).equals(recipe)) {
                            index = i;
                            break;
                        }
                    }
                }
                
                ShopTradeEvent tradeEvent = new ShopTradeEvent(player, shop, event.getCurrentItem(), index);
                Bukkit.getPluginManager().callEvent(tradeEvent);
                
                if (tradeEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
