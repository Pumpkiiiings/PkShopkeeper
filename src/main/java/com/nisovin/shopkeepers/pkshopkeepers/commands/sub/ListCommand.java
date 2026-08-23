package com.nisovin.shopkeepers.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;
import com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop;

@SuppressWarnings("UnstableApiUsage")
public class ListCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public ListCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        sender.sendMessage(plugin.getConfigManager().parseString("&e--- Lista de Tiendas ---"));
        for (PkShop shop : plugin.getShopManager().getShops()) {
            sender.sendMessage(plugin.getConfigManager().parseString("&7- &a" + shop.getId() + " &8(&7" + shop.getName() + "&8)"));
        }
        return Command.SINGLE_SUCCESS;
    }
}
