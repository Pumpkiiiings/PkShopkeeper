package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.migration.LegacyMigrator;

@SuppressWarnings("UnstableApiUsage")
public class MigrateStartCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public MigrateStartCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("Shopkeepers") == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("migrate-no-plugin"));
            return 0;
        }
        LegacyMigrator.runMigration(plugin, sender);
        return Command.SINGLE_SUCCESS;
    }
}
