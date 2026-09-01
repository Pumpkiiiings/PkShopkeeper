package com.pumpkings.pkshopkeepers.commands.sub;

import java.util.List;

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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@SuppressWarnings("UnstableApiUsage")
public class CompareCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public CompareCommand(PkShopkeepers plugin) {
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

        player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-title", "%name%", shop.getName()));

        List<PkTradeOffer> offers = shop.getOffers();
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null || invItem.getType() == Material.AIR) continue;

            for (int i = 0; i < offers.size(); i++) {
                PkTradeOffer offer = offers.get(i);
                
                checkMatch(player, invItem, offer.getItem1(), i, "Item 1", shop.getId());
                checkMatch(player, invItem, offer.getItem2(), i, "Item 2", shop.getId());
            }
        }

        player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-footer"));
        return Command.SINGLE_SUCCESS;
    }

    private void checkMatch(Player player, ItemStack invItem, ItemStack shopItem, int tradeIndex, String slotName, String shopId) {
        if (shopItem == null || shopItem.getType() == Material.AIR) return;
        
        if (invItem.getType() != shopItem.getType()) return;
        
        if (invItem.isSimilar(shopItem)) {
            if (invItem.getAmount() >= shopItem.getAmount()) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-exact", "%trade%", String.valueOf(tradeIndex + 1), "%slot%", slotName));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-amount", "%trade%", String.valueOf(tradeIndex + 1), "%slot%", slotName, "%has%", String.valueOf(invItem.getAmount()), "%needs%", String.valueOf(shopItem.getAmount())));
            }
            return;
        }

        player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-reason-header", "%trade%", String.valueOf(tradeIndex + 1), "%slot%", slotName, "%item%", invItem.getType().name()));
        
        ItemMeta invMeta = invItem.getItemMeta();
        ItemMeta shopMeta = shopItem.getItemMeta();
        
        if (invMeta == null && shopMeta != null) {
            player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-no-meta"));
            return;
        }
        if (invMeta != null && shopMeta == null) {
            player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-has-meta"));
            return;
        }
        
        if (invMeta != null && shopMeta != null) {
            boolean foundDifference = false;
            String invName = invMeta.hasDisplayName() ? LegacyComponentSerializer.legacyAmpersand().serialize(invMeta.displayName()) : "Ninguno";
            String shopName = shopMeta.hasDisplayName() ? LegacyComponentSerializer.legacyAmpersand().serialize(shopMeta.displayName()) : "Ninguno";
            if (!invName.equals(shopName)) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-name"));
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-name-you", "%you%", invName));
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-name-shop", "%shop%", shopName));
                foundDifference = true;
            }
            
            if (invMeta.hasCustomModelData() != shopMeta.hasCustomModelData()) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-cmd"));
                foundDifference = true;
            } else if (invMeta.hasCustomModelData() && invMeta.getCustomModelData() != shopMeta.getCustomModelData()) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-cmd-diff", "%you%", String.valueOf(invMeta.getCustomModelData()), "%shop%", String.valueOf(shopMeta.getCustomModelData())));
                foundDifference = true;
            }

            boolean invHasLore = invMeta.hasLore();
            boolean shopHasLore = shopMeta.hasLore();
            if (invHasLore != shopHasLore) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-lore-exists"));
                foundDifference = true;
            } else if (invHasLore) {
                List<Component> invLore = invMeta.lore();
                List<Component> shopLore = shopMeta.lore();
                if (invLore.size() != shopLore.size()) {
                    player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-lore-size", "%you%", String.valueOf(invLore.size()), "%shop%", String.valueOf(shopLore.size())));
                    foundDifference = true;
                } else {
                    for (int i = 0; i < invLore.size(); i++) {
                        String invLine = LegacyComponentSerializer.legacyAmpersand().serialize(invLore.get(i));
                        String shopLine = LegacyComponentSerializer.legacyAmpersand().serialize(shopLore.get(i));
                        if (!invLine.equals(shopLine)) {
                            player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-lore-line", "%line%", String.valueOf(i + 1)));
                            player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-lore-you", "%you%", invLine));
                            player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-lore-shop", "%shop%", shopLine));
                            foundDifference = true;
                        }
                    }
                }
            }
            
            if (!invMeta.getEnchants().equals(shopMeta.getEnchants())) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-enchants"));
                foundDifference = true;
            }
            
            if (invMeta.isUnbreakable() != shopMeta.isUnbreakable()) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-unbreakable"));
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-unbreakable-values",
                        "%you%", String.valueOf(invMeta.isUnbreakable()), "%shop%", String.valueOf(shopMeta.isUnbreakable())));
                foundDifference = true;
            }
            
            if (!invMeta.getItemFlags().equals(shopMeta.getItemFlags())) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-item-flags"));
                foundDifference = true;
            }
            
            if (invMeta.hasAttributeModifiers() != shopMeta.hasAttributeModifiers()) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-attribute-presence"));
                foundDifference = true;
            } else if (invMeta.hasAttributeModifiers() && shopMeta.hasAttributeModifiers() && !invMeta.getAttributeModifiers().equals(shopMeta.getAttributeModifiers())) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-attribute-values"));
                foundDifference = true;
            }
            
            if (!invMeta.getPersistentDataContainer().getKeys().equals(shopMeta.getPersistentDataContainer().getKeys())) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-pdc"));
                foundDifference = true;
            }

            if (!foundDifference) {
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-components"));
                player.sendMessage(plugin.getConfigManager().getMessageRaw("compare-fix-hint",
                        "%id%", shopId, "%trade%", String.valueOf(tradeIndex + 1),
                        "%slot%", slotName.toLowerCase().replace(" ", "")));
            }
        }
    }
}
