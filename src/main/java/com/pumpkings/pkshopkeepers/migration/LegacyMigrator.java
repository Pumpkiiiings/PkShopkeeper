package com.pumpkings.pkshopkeepers.migration;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import com.pumpkings.pkshopkeepers.shop.PkTradeOffer;
import com.pumpkings.pkshopkeepers.shop.ShopManager;

public class LegacyMigrator {

    public static void runMigration(PkShopkeepers plugin, CommandSender sender) {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("Shopkeepers") == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("migrate-no-plugin"));
            return;
        }

        try {
            ShopManager manager = plugin.getShopManager();
            int count = 0;
            for (Shopkeeper sk : ShopkeepersAPI.getShopkeeperRegistry().getAllShopkeepers()) {
                if (sk instanceof RegularAdminShopkeeper) {
                    RegularAdminShopkeeper adminSk = (RegularAdminShopkeeper) sk;
                    
                    String newId = String.valueOf(adminSk.getId());
                    PkShop newShop = new PkShop(newId);
                    newShop.setName(adminSk.getName() != null ? adminSk.getName() : "Shopkeeper");
                    newShop.setLocation(adminSk.getLocation());
                    
                    com.nisovin.shopkeepers.api.shopobjects.ShopObject obj = adminSk.getShopObject();
                    if (obj != null) {
                        try {
                            java.lang.reflect.Method getBaby = obj.getClass().getMethod("isBaby");
                            newShop.setBaby((boolean) getBaby.invoke(obj));
                        } catch (Exception e) {}
                        
                        try {
                            java.lang.reflect.Method getProfession = obj.getClass().getMethod("getProfession");
                            Object prof = getProfession.invoke(obj);
                            if (prof != null) {
                                org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(prof.toString().toLowerCase());
                                org.bukkit.entity.Villager.Profession profession = org.bukkit.Registry.VILLAGER_PROFESSION.get(key);
                                if (profession != null) newShop.setVillagerProfession(profession);
                            }
                        } catch (Exception e) {}
                        
                        try {
                            java.lang.reflect.Method getType = obj.getClass().getMethod("getVillagerType");
                            Object vType = getType.invoke(obj);
                            if (vType != null) {
                                org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(vType.toString().toLowerCase());
                                org.bukkit.entity.Villager.Type type = org.bukkit.Registry.VILLAGER_TYPE.get(key);
                                if (type != null) newShop.setVillagerType(type);
                            }
                        } catch (Exception e) {}
                        
                        try {
                            java.lang.reflect.Method getLevel = obj.getClass().getMethod("getVillagerLevel");
                            newShop.setVillagerLevel((int) getLevel.invoke(obj));
                        } catch (Exception e) {}
                    }

                    List<PkTradeOffer> pkOffers = new ArrayList<>();
                    for (TradeOffer offer : adminSk.getOffers()) {
                        org.bukkit.inventory.ItemStack item1 = offer.getItem1().copy();
                        org.bukkit.inventory.ItemStack item2 = offer.getItem2() != null ? offer.getItem2().copy() : null;
                        org.bukkit.inventory.ItemStack result = offer.getResultItem().copy();
                        
                        pkOffers.add(new PkTradeOffer(item1, item2, result));
                    }
                    newShop.setOffers(pkOffers);
                    manager.addShop(newShop);
                    count++;
                }
            }
            
            manager.saveShops();
            sender.sendMessage(plugin.getConfigManager().getMessage("migrate-success", "%count%", String.valueOf(count)));
            sender.sendMessage(plugin.getConfigManager().getMessage("migrate-instruction"));
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("parse-error", "%error%", e.getMessage()));
            e.printStackTrace();
        }
    }

    public static void scanMigration(PkShopkeepers plugin, CommandSender sender) {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("Shopkeepers") == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("scan-no-plugin"));
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
            sender.sendMessage(plugin.getConfigManager().getMessage("migrate-scan-title"));
            sender.sendMessage(plugin.getConfigManager().getMessage("migrate-scan-ready", "%count%", String.valueOf(count)));
            sender.sendMessage(plugin.getConfigManager().getMessage("migrate-scan-skipped", "%skipped%", String.valueOf(skipped)));
            if (count > 0) {
                sender.sendMessage(plugin.getConfigManager().getMessage("migrate-scan-cmd"));
            }
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("parse-error", "%error%", e.getMessage()));
            e.printStackTrace();
        }
    }
}
