package com.nisovin.shopkeepers.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;
import com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop;
import com.nisovin.shopkeepers.pkshopkeepers.shop.PkTradeOffer;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class OpenCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public OpenCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        
        String id = context.getArgument("id", String.class);
        String playerName = context.getArgument("player", String.class);

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return 0;
        }

        PkShop shop = plugin.getShopManager().getShop(id);
        if (shop == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("shop-not-found"));
            return 0;
        }

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
        target.openMerchant(merchant, true);

        sender.sendMessage(plugin.getConfigManager().getMessage("opening-shop", 
                "%id%", id, 
                "%player%", target.getName()));
                
        return Command.SINGLE_SUCCESS;
    }
}
