package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;

@SuppressWarnings("UnstableApiUsage")
public class ListCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public ListCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        sender.sendMessage(plugin.getConfigManager().getMessage("list-header"));
        for (PkShop shop : plugin.getShopManager().getShops()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("list-format", "%id%", shop.getId(), "%name%", shop.getName()));
        }
        return Command.SINGLE_SUCCESS;
    }
}
