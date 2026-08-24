package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import org.bukkit.Bukkit;

@SuppressWarnings("UnstableApiUsage")
public class LinkCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public LinkCommand(PkShopkeepers plugin) {
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
        String npcId = StringArgumentType.getString(context, "npc");

        PkShop shop = plugin.getShopManager().getShop(shopId);
        if (shop == null) {
            sender.sendMessage(plugin.getConfigManager().getMessageRaw("shop-not-found"));
            return 0;
        }

        if (shop.getEntityUUID() != null) {
            org.bukkit.entity.Entity oldEntity = Bukkit.getEntity(shop.getEntityUUID());
            if (oldEntity != null) {
                oldEntity.remove();
            }
            shop.setEntityUUID(null);
        }

        shop.setNpcId(npcId);
        plugin.getShopManager().saveShops();

        sender.sendMessage(plugin.getConfigManager().getMessage("link-success", "%id%", shopId, "%npc%", npcId));
        
        return Command.SINGLE_SUCCESS;
    }
}
