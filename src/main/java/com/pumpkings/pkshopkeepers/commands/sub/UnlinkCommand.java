package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;

@SuppressWarnings("UnstableApiUsage")
public class UnlinkCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public UnlinkCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return 0;
        }

        String shopId = StringArgumentType.getString(context, "shop");

        PkShop shop = plugin.getShopManager().getShop(shopId);
        if (shop == null) {
            sender.sendMessage(plugin.getConfigManager().getMessageRaw("shop-not-found"));
            return 0;
        }

        shop.setNpcId(null);
        plugin.getShopManager().saveShops();
        
        com.pumpkings.pkshopkeepers.utils.FoliaScheduler.runRegionTask(plugin, shop.getLocation(), () -> {
            new com.pumpkings.pkshopkeepers.entity.ShopEntityListener(plugin, plugin.getShopManager()).spawnShop(shop);
        });

        sender.sendMessage(plugin.getConfigManager().getMessage("unlink-success", "%id%", shopId));
        
        return Command.SINGLE_SUCCESS;
    }
}
