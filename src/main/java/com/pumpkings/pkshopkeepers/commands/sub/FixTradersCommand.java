package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;

@SuppressWarnings("UnstableApiUsage")
public class FixTradersCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public FixTradersCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!sender.hasPermission("pkshopkeepers.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return 0;
        }

        int removed = 0;
        int fixed = 0;

        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (shop.getLocation() == null || shop.getLocation().getWorld() == null || shop.getNpcId() != null) continue;
            
            if (!shop.getLocation().getChunk().isLoaded()) continue;

            // Encontrar cualquier entidad aldeano en un radio corto
            for (Entity e : shop.getLocation().getWorld().getNearbyEntities(shop.getLocation(), 2.0, 2.0, 2.0)) {
                if (e.getType() == org.bukkit.entity.EntityType.VILLAGER) {
                    // Si no es el actual del shop, lo removemos
                    if (shop.getEntityUUID() == null || !e.getUniqueId().equals(shop.getEntityUUID())) {
                        e.remove();
                        removed++;
                    }
                }
            }
            
            // Removemos nuestra entidad y la volvemos a spawnear limpiamente
            plugin.getShopEntityListener().removeEntity(shop);
            plugin.getShopEntityListener().spawnShop(shop);
            fixed++;
        }

        if (sender instanceof Player player) {
            player.sendMessage(plugin.getConfigManager().getMessage("fixtraders-success", "%removed%", String.valueOf(removed), "%fixed%", String.valueOf(fixed)));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("fixtraders-success", "%removed%", String.valueOf(removed), "%fixed%", String.valueOf(fixed)));
        }

        return Command.SINGLE_SUCCESS;
    }
}
