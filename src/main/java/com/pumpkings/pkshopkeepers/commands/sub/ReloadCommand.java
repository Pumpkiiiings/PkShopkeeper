package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import com.pumpkings.pkshopkeepers.PkShopkeepers;

@SuppressWarnings("UnstableApiUsage")
public class ReloadCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public ReloadCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        plugin.getShopEntityListener().prepareReload();
        plugin.getConfigManager().loadConfig();
        plugin.getShopManager().loadShops();
        plugin.getShopEntityListener().spawnAllShops();
        sender.sendMessage(plugin.getConfigManager().getMessage("reloaded"));
        return Command.SINGLE_SUCCESS;
    }
}
