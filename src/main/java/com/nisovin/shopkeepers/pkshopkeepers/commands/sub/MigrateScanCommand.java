package com.nisovin.shopkeepers.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;
import com.nisovin.shopkeepers.pkshopkeepers.migration.LegacyMigrator;

@SuppressWarnings("UnstableApiUsage")
public class MigrateScanCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public MigrateScanCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("Shopkeepers") == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("scan-no-plugin"));
            return 0;
        }
        LegacyMigrator.scanMigration(plugin, sender);
        return Command.SINGLE_SUCCESS;
    }
}
