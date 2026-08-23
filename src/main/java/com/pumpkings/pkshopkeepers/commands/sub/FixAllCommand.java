package com.pumpkings.pkshopkeepers.commands.sub;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import com.pumpkings.pkshopkeepers.shop.PkTradeOffer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@SuppressWarnings("UnstableApiUsage")
public class FixAllCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public FixAllCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return 0;
        }

        String id = context.getArgument("id", String.class);
        PkShop shop = plugin.getShopManager().getShop(id);

        if (shop == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("shop-not-found"));
            return 0;
        }

        int fixedCount = 0;
        for (PkTradeOffer offer : shop.getOffers()) {
            if (offer.getItem1() != null) {
                if (tryFix(player, offer.getItem1())) fixedCount++;
            }
            if (offer.getItem2() != null) {
                if (tryFix(player, offer.getItem2())) fixedCount++;
            }
            if (offer.getResult() != null) {
                if (tryFix(player, offer.getResult())) fixedCount++;
            }
        }

        if (fixedCount > 0) {
            plugin.getShopManager().saveShops();
            player.sendMessage("§a[PkShopkeepers] §fSe han actualizado §e" + fixedCount + " §fítems de la tienda §a" + shop.getName() + " §fcoincidiendo con los de tu inventario.");
        } else {
            player.sendMessage("§c[PkShopkeepers] No se encontraron ítems en tu inventario con el mismo Material y Nombre Exacto que los de la tienda para actualizar.");
        }

        return Command.SINGLE_SUCCESS;
    }

    private boolean tryFix(Player player, ItemStack shopItem) {
        if (shopItem.getType() == Material.AIR) return false;
        
        ItemMeta shopMeta = shopItem.getItemMeta();
        String shopName = (shopMeta != null && shopMeta.hasDisplayName()) ? LegacyComponentSerializer.legacyAmpersand().serialize(shopMeta.displayName()) : null;

        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null || invItem.getType() == Material.AIR) continue;
            
            if (invItem.getType() == shopItem.getType()) {
                if (invItem.isSimilar(shopItem)) continue; // Already perfect match, no need to fix
                
                ItemMeta invMeta = invItem.getItemMeta();
                String invName = (invMeta != null && invMeta.hasDisplayName()) ? LegacyComponentSerializer.legacyAmpersand().serialize(invMeta.displayName()) : null;
                
                // If both have no name, or both have same name
                if ((shopName == null && invName == null) || (shopName != null && shopName.equals(invName))) {
                    shopItem.setItemMeta(invMeta.clone());
                    return true;
                }
            }
        }
        return false;
    }
}
