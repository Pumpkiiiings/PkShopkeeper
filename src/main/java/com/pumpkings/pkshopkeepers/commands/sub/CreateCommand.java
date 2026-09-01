package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;

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

        int maxShops = plugin.getConfigManager().getInt("settings.max-shops-per-player", -1);
        if (maxShops >= 0 && plugin.getShopManager().countShopsOwnedBy(player.getUniqueId()) >= maxShops) {
            player.sendMessage(plugin.getConfigManager().getMessage("max-shops-reached", "%max%", String.valueOf(maxShops)));
            return 0;
        }

        try {
            org.bukkit.entity.EntityType type = EntityType.valueOf(typeStr.toUpperCase());
            
            java.util.List<String> allowed = plugin.getConfigManager().getStringList("settings.enabled-living-shops");
            boolean configured = allowed.isEmpty() || allowed.stream().anyMatch(value -> value.equalsIgnoreCase(type.name()));
            if (!configured || !type.isAlive() || !type.isSpawnable()) {
                player.sendMessage(plugin.getConfigManager().getMessage("invalid-entity"));
                return 0;
            }
            PkShop newShop = new PkShop(plugin.getShopManager().getNextId());
            newShop.setName(plugin.getConfigManager().getRawString("settings.default-shop-name", "New Shop"));
            newShop.setLocation(player.getLocation());
            newShop.setOwnerUUID(player.getUniqueId());
            newShop.setEntityType(type);

            plugin.getShopManager().addShop(newShop);
            plugin.getShopManager().saveShops();
            plugin.getShopEntityListener().spawnShop(newShop);
        } catch (Exception e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-entity"));
            return 0;
        }

        player.sendMessage(plugin.getConfigManager().getMessage("shop-created"));
        return Command.SINGLE_SUCCESS;
    }
}
