package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class CreateCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public CreateCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            context.getSource().getSender().sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return 0;
        }

        String typeStr = "VILLAGER";
        try {
            typeStr = context.getArgument("type", String.class);
        } catch (IllegalArgumentException e) {
            // Optional argument not provided
        }

        PkShop newShop = new PkShop(UUID.randomUUID().toString());
        newShop.setName("New Shop");
        newShop.setLocation(player.getLocation());

        try {
            org.bukkit.entity.EntityType type = EntityType.valueOf(typeStr.toUpperCase());
            
            java.util.List<String> allowed = plugin.getConfigManager().getStringList("settings.enabled-living-shops");
            if (!allowed.contains(type.name())) {
                player.sendMessage(plugin.getConfigManager().getMessage("invalid-entity"));
                newShop.setEntityType(EntityType.VILLAGER);
            } else {
                newShop.setEntityType(type);
            }
        } catch (Exception e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-entity"));
            newShop.setEntityType(EntityType.VILLAGER);
        }

        plugin.getShopManager().addShop(newShop);
        plugin.getShopManager().saveShops();

        // Spawn it on the region thread
        com.pumpkings.pkshopkeepers.utils.FoliaScheduler.runRegionTask(plugin, player.getLocation(), () -> {
            new com.pumpkings.pkshopkeepers.entity.ShopEntityListener(plugin, plugin.getShopManager()).spawnShop(newShop);
        });

        player.sendMessage(plugin.getConfigManager().getMessage("shop-created"));
        return Command.SINGLE_SUCCESS;
    }
}
