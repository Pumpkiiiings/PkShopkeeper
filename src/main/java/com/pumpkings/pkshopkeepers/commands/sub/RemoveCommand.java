package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import java.util.ArrayList;

@SuppressWarnings("UnstableApiUsage")
public class RemoveCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public RemoveCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            context.getSource().getSender().sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return 0;
        }

        int count = 0;
        for (PkShop shop : new ArrayList<>(plugin.getShopManager().getShops())) {
            if (shop.getLocation() != null && shop.getLocation().getWorld().equals(player.getWorld())) {
                if (shop.getLocation().distance(player.getLocation()) <= 3.0) {
                    plugin.getShopManager().removeShop(shop.getId());
                    plugin.getServer().getPluginManager().callEvent(new org.bukkit.event.world.ChunkUnloadEvent(shop.getLocation().getChunk()));
                    count++;
                }
            }
        }
        
        player.sendMessage(plugin.getConfigManager().getMessage("shops-deleted", "%count%", String.valueOf(count)));
        return Command.SINGLE_SUCCESS;
    }
}
