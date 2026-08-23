package com.nisovin.shopkeepers.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;
import com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop;

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

        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (shop.getLocation() != null && shop.getLocation().getWorld().equals(player.getWorld())) {
                double dist = shop.getLocation().distanceSquared(player.getLocation());
                if (dist <= 25.0 && dist < closestDist) { // 5 blocks radius (5*5=25)
                    closestDist = dist;
                    closestShop = shop;
                }
            }
        }

        if (closestShop != null) {
            player.sendMessage("§a[PkShopkeepers] §fEl Shopkeeper más cercano es:");
            player.sendMessage("§7Nombre: §f" + closestShop.getName());
            player.sendMessage("§7ID: §e" + closestShop.getId());
            
            net.kyori.adventure.text.Component msg = net.kyori.adventure.text.Component.text("§7Haz click para copiar: §n§b" + closestShop.getId())
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(closestShop.getId()))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.Component.text("§aCopiar ID al portapapeles")));
            player.sendMessage(msg);
        } else {
            player.sendMessage("§cNo se encontraron tiendas en un radio de 5 bloques.");
        }

        return Command.SINGLE_SUCCESS;
    }
}
