package com.pumpkings.pkshopkeepers.api;

import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PkShopkeepersAPI {
    private static PkShopkeepersAPI instance;
    private final PkShopkeepers plugin;

    public PkShopkeepersAPI(PkShopkeepers plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static PkShopkeepersAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PkShopkeepersAPI no ha sido inicializado aún.");
        }
        return instance;
    }

    public PkShop getShop(String id) {
        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (shop.getId().equals(id)) {
                return shop;
            }
        }
        return null;
    }

    public PkShop getShopByName(String name) {
        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (shop.getName().equalsIgnoreCase(name)) {
                return shop;
            }
        }
        return null;
    }

    public PkShop getShopByNpcId(String npcId) {
        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (shop.getNpcId() != null && shop.getNpcId().equalsIgnoreCase(npcId)) {
                return shop;
            }
        }
        return null;
    }

    public Collection<PkShop> getShops() {
        return plugin.getShopManager().getShops();
    }

    public void openShop(Player player, String shopId) {
        PkShop shop = getShop(shopId);
        if (shop != null) {
            plugin.getShopEntityListener().openShop(player, shop);
        }
    }

    public boolean createShop(Location loc, String name, EntityType type) {
        PkShop newShop = new PkShop(plugin.getShopManager().getNextId());
        newShop.setLocation(loc);
        newShop.setName(name);
        newShop.setEntityType(type);
        plugin.getShopManager().addShop(newShop);
        plugin.getShopManager().saveShops();
        return true;
    }

    public boolean deleteShop(String shopId) {
        PkShop shop = getShop(shopId);
        if (shop != null) {
            plugin.getShopManager().removeShop(shopId);
            return true;
        }
        return false;
    }
}
