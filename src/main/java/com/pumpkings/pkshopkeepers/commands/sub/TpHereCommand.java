package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TpHereCommand implements Command<CommandSourceStack> {
    private final PkShopkeepers plugin;

    public TpHereCommand(PkShopkeepers plugin) {
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

        targetShop.setLocation(player.getLocation());
        plugin.getShopManager().saveShops();
        
        plugin.getShopEntityListener().removeEntity(targetShop);
        plugin.getShopEntityListener().spawnShop(targetShop);
        
        player.sendMessage(plugin.getConfigManager().getMessage("tphere-success", "%name%", targetShop.getName()));
        
        if (targetShop.getNpcId() != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("tphere-warning", "%npc%", targetShop.getNpcId()));
        }

        return 1;
    }
}
