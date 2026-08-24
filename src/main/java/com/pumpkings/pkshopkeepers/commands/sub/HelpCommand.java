package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnstableApiUsage")
public class HelpCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public HelpCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        for (String line : plugin.getConfig().getStringList("messages.help-message")) {
            sender.sendMessage(plugin.getConfigManager().parseString(line));
        }
        return Command.SINGLE_SUCCESS;
    }
}
