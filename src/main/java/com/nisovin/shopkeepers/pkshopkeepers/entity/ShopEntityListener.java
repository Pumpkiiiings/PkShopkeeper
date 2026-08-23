package com.nisovin.shopkeepers.pkshopkeepers.entity;

import java.util.ArrayList;
import java.util.List;
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

import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;
import com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop;
import com.nisovin.shopkeepers.pkshopkeepers.shop.PkTradeOffer;
import com.nisovin.shopkeepers.pkshopkeepers.shop.ShopManager;

import com.nisovin.shopkeepers.pkshopkeepers.utils.FoliaScheduler;

public class ShopEntityListener implements Listener {

    private final PkShopkeepers plugin;
    private final ShopManager shopManager;

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
    }

    public void spawnShop(PkShop shop) {
        if (shop.getFancyNpcId() != null) return;
        
        Location loc = shop.getLocation();
        if (loc == null || loc.getWorld() == null) return;
        
        boolean found = false;
        for (Entity e : shop.getLocation().getWorld().getEntities()) {
            if (e.getUniqueId().equals(shop.getEntityUUID())) {
                found = true;
                if (e instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) e;
                    le.setAI(false);
                    le.setInvulnerable(true);
                    le.setSilent(true);
                    e.setCustomName(shop.getName());
                    e.setCustomNameVisible(true);
                }
                break;
            }
        }
        
        if (!found && shop.getLocation().getChunk().isLoaded()) {
            Entity entity = shop.getLocation().getWorld().spawnEntity(shop.getLocation(), shop.getEntityType());
            if (entity instanceof org.bukkit.entity.LivingEntity) {
                org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) entity;
                le.setAI(false);
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
                ((org.bukkit.entity.Villager) entity).setProfession(shop.getVillagerProfession());
            }
            
            shop.setEntityUUID(entity.getUniqueId());
            
            // Look At Player Logic
            FoliaScheduler.runEntityTaskTimer(plugin, entity, () -> {
                if (!entity.isValid() || entity.isDead()) return;
                
                org.bukkit.entity.Player nearest = null;
                double closest = 25.0; // 5 blocks squared
                for (org.bukkit.entity.Player p : entity.getWorld().getPlayers()) {
                    if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
                    double dist = p.getLocation().distanceSquared(entity.getLocation());
                    if (dist < closest) {
                        closest = dist;
                        nearest = p;
                    }
                }
                
                if (nearest != null) {
                    Location eLoc = entity.getLocation();
                    Location pLoc = nearest.getLocation().add(0, nearest.getEyeHeight(), 0);
                    org.bukkit.util.Vector dir = pLoc.toVector().subtract(eLoc.clone().add(0, entity.getHeight(), 0).toVector());
                    eLoc.setDirection(dir);
                    entity.setRotation(eLoc.getYaw(), eLoc.getPitch());
                }
            }, 10L, 5L); // Every 5 ticks
        }
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
    }
}
