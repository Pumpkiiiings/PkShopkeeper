package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;

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

        plugin.getShopEntityListener().openShop(target, shop);

        sender.sendMessage(plugin.getConfigManager().getMessage("opening-shop", 
                "%id%", id, 
                "%player%", target.getName()));
                
        return Command.SINGLE_SUCCESS;
    }
}
