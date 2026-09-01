package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;

@SuppressWarnings("UnstableApiUsage")
public class WhoisCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public WhoisCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            context.getSource().getSender().sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return 0;
        }

        if (!player.hasPermission("pkshopkeepers.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return 0;
        }

        PkShop closestShop = null;
        double closestDist = Double.MAX_VALUE;
        double radius = Math.max(0.0, plugin.getConfigManager().getDouble("settings.whois-radius", 5.0));

        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (shop.getLocation() != null && shop.getLocation().getWorld().equals(player.getWorld())) {
                double dist = shop.getLocation().distanceSquared(player.getLocation());
                if (dist <= radius * radius && dist < closestDist) {
                    closestDist = dist;
                    closestShop = shop;
                }
            }
        }

        if (closestShop != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("whois-nearest"));
            player.sendMessage(plugin.getConfigManager().getMessage("whois-name", "%name%", closestShop.getName()));
            player.sendMessage(plugin.getConfigManager().getMessage("whois-id", "%id%", closestShop.getId()));
            net.kyori.adventure.text.Component msg = plugin.getConfigManager().getMessageRaw("whois-copy", "%id%", closestShop.getId())
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(closestShop.getId()))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(plugin.getConfigManager().getMessageRaw("whois-hover")));
            player.sendMessage(msg);
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("whois-not-found", "%radius%", String.valueOf(radius)));
        }

        return Command.SINGLE_SUCCESS;
    }
}
