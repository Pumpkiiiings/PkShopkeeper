package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TpCommand implements Command<CommandSourceStack> {
    private final PkShopkeepers plugin;

    public TpCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(plugin.getConfigManager().getMessageRaw("only-players"));
            return 0;
        }

        String shopName = context.getArgument("name", String.class);
        PkShop targetShop = null;

        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (shop.getName().equalsIgnoreCase(shopName)) {
                targetShop = shop;
                break;
            }
        }

        if (targetShop == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop-not-found"));
            return 0;
        }

        if (targetShop.getLocation() == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("tp-no-location"));
            return 0;
        }

        PkShop resolvedShop = targetShop;
        player.teleportAsync(targetShop.getLocation()).thenAccept(success -> {
            if (success) {
                com.pumpkings.pkshopkeepers.utils.FoliaScheduler.runEntityTask(plugin, player,
                        () -> player.sendMessage(plugin.getConfigManager().getMessage("tp-success", "%name%", resolvedShop.getName())));
            }
        });

        return 1;
    }
}
