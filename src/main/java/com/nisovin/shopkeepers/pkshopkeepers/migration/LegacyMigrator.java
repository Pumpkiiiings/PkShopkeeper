package com.nisovin.shopkeepers.pkshopkeepers.migration;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;
import com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop;
import com.nisovin.shopkeepers.pkshopkeepers.shop.PkTradeOffer;
import com.nisovin.shopkeepers.pkshopkeepers.shop.ShopManager;
import com.nisovin.shopkeepers.util.inventory.ItemUtils;

public class LegacyMigrator {

    public static void runMigration(PkShopkeepers plugin, CommandSender sender) {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("Shopkeepers") == null) {
            sender.sendMessage("§cEl plugin Shopkeepers original no está instalado o activo. No se puede migrar.");
            return;
        }

        try {
            ShopManager manager = plugin.getShopManager();
            int count = 0;
            for (Shopkeeper sk : ShopkeepersAPI.getShopkeeperRegistry().getAllShopkeepers()) {
                if (sk instanceof RegularAdminShopkeeper) {
                    RegularAdminShopkeeper adminSk = (RegularAdminShopkeeper) sk;
                    
                    String newId = plugin.getShopManager().getNextId();
                    PkShop newShop = new PkShop(newId);
                    newShop.setName(adminSk.getName() != null ? adminSk.getName() : "Shopkeeper");
                    newShop.setLocation(adminSk.getLocation());
                    // newShop.setEntityUUID(...); // Generará un UUID aleatorio para la nueva entidad
                    
                    List<PkTradeOffer> pkOffers = new ArrayList<>();
                    for (TradeOffer offer : adminSk.getOffers()) {
                        org.bukkit.inventory.ItemStack item1 = ItemUtils.asItemStack(offer.getItem1());
                        org.bukkit.inventory.ItemStack item2 = offer.getItem2() != null ? ItemUtils.asItemStack(offer.getItem2()) : null;
                        org.bukkit.inventory.ItemStack result = ItemUtils.asItemStack(offer.getResultItem());
                        
                        pkOffers.add(new PkTradeOffer(item1, item2, result));
                    }
                    newShop.setOffers(pkOffers);
                    manager.addShop(newShop);
                    count++;
                }
            }
            
            manager.saveShops();
            sender.sendMessage("§a¡Migración exitosa! Se han migrado " + count + " shopkeepers (Admin Shops) al nuevo formato.");
            sender.sendMessage("§ePuedes detener el servidor, borrar la carpeta del viejo Shopkeepers, y todo funcionará nativamente con PkShopkeepers.");
        } catch (Exception e) {
            sender.sendMessage("§cOcurrió un error al migrar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void scanMigration(PkShopkeepers plugin, CommandSender sender) {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("Shopkeepers") == null) {
            sender.sendMessage("§cEl plugin Shopkeepers original no está instalado o activo. No se puede escanear.");
            return;
        }
        
        try {
            int count = 0;
            int skipped = 0;
            for (Shopkeeper sk : ShopkeepersAPI.getShopkeeperRegistry().getAllShopkeepers()) {
                if (sk instanceof RegularAdminShopkeeper) {
                    count++;
                } else {
                    skipped++;
                }
            }
            sender.sendMessage("§b--- Escaneo de Migración ---");
            sender.sendMessage("§fAdmin Shops encontrados: §a" + count + " §7(Listos para migrar)");
            sender.sendMessage("§fOtras Tiendas (Jugadores/Límites/etc): §c" + skipped + " §7(Serán ignoradas)");
            if (count > 0) {
                sender.sendMessage("§eSi estás listo, ejecuta: §b/pks migrate start");
            }
        } catch (Exception e) {
            sender.sendMessage("§cOcurrió un error al escanear: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
