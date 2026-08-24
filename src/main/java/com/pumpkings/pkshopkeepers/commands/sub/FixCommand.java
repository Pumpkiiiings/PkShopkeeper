package com.pumpkings.pkshopkeepers.commands.sub;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import com.pumpkings.pkshopkeepers.shop.PkTradeOffer;
import io.papermc.paper.command.brigadier.CommandSourceStack;

@SuppressWarnings("UnstableApiUsage")
public class FixCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public FixCommand(PkShopkeepers plugin) {
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
        int tradeIndex = context.getArgument("tradeIndex", Integer.class) - 1;
        String action = context.getArgument("action", String.class); // "to_shop" or "to_inv"

        PkShop shop = plugin.getShopManager().getShop(id);

        if (shop == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("shop-not-found"));
            return 0;
        }

        if (tradeIndex < 0 || tradeIndex >= shop.getOffers().size()) {
            player.sendMessage(plugin.getConfigManager().getMessage("fix-invalid-index"));
            return 0;
        }

        PkTradeOffer offer = shop.getOffers().get(tradeIndex);

        if (action.equalsIgnoreCase("to_inv")) {
            if (offer.getItem1() != null) player.getInventory().addItem(offer.getItem1().clone());
            if (offer.getItem2() != null) player.getInventory().addItem(offer.getItem2().clone());
            player.sendMessage(plugin.getConfigManager().getMessage("fix-to-inv", "%trade%", String.valueOf(tradeIndex + 1)));
        } else if (action.equalsIgnoreCase("to_shop")) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            if (mainHand.getType() == Material.AIR) {
                player.sendMessage(plugin.getConfigManager().getMessage("fix-no-main-hand"));
                return 0;
            }

            offer.setItem1(mainHand.clone());
            if (offHand.getType() != Material.AIR) {
                offer.setItem2(offHand.clone());
            } else {
                offer.setItem2(null);
            }
            
            plugin.getShopManager().saveShops();
            player.sendMessage(plugin.getConfigManager().getMessage("fix-to-shop", "%trade%", String.valueOf(tradeIndex + 1)));
        } else if (action.equalsIgnoreCase("item1")) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getType() == Material.AIR) {
                player.sendMessage(plugin.getConfigManager().getMessage("fix-no-main-hand"));
                return 0;
            }
            offer.setItem1(mainHand.clone());
            plugin.getShopManager().saveShops();
            player.sendMessage(plugin.getConfigManager().getMessage("fix-item1", "%trade%", String.valueOf(tradeIndex + 1)));
        } else if (action.equalsIgnoreCase("item2")) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getType() == Material.AIR) {
                player.sendMessage(plugin.getConfigManager().getMessage("fix-no-main-hand"));
                return 0;
            }
            offer.setItem2(mainHand.clone());
            plugin.getShopManager().saveShops();
            player.sendMessage(plugin.getConfigManager().getMessage("fix-item2", "%trade%", String.valueOf(tradeIndex + 1)));
        } else if (action.equalsIgnoreCase("result")) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getType() == Material.AIR) {
                player.sendMessage(plugin.getConfigManager().getMessage("fix-no-main-hand"));
                return 0;
            }
            offer.setResult(mainHand.clone());
            plugin.getShopManager().saveShops();
            player.sendMessage(plugin.getConfigManager().getMessage("fix-result", "%trade%", String.valueOf(tradeIndex + 1)));
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("fix-invalid-action"));
        }

        return Command.SINGLE_SUCCESS;
    }
}
