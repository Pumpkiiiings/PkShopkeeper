package com.pumpkings.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        List<CompletableFuture<Integer>> repairs = new ArrayList<>();
        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (shop.getLocation() == null || shop.getLocation().getWorld() == null || shop.getNpcId() != null) continue;
            repairs.add(plugin.getShopEntityListener().repairShop(shop).exceptionally(error -> {
                plugin.getLogger().warning("Could not repair shop " + shop.getId() + ": " + error.getMessage());
                return 0;
            }));
        }

        CompletableFuture.allOf(repairs.toArray(new CompletableFuture[0])).thenRun(() -> {
            int removed = repairs.stream().mapToInt(CompletableFuture::join).sum();
            Runnable notify = () -> sender.sendMessage(plugin.getConfigManager().getMessage("fixtraders-success",
                    "%removed%", String.valueOf(removed), "%fixed%", String.valueOf(repairs.size())));
            if (sender instanceof Player player) {
                com.pumpkings.pkshopkeepers.utils.FoliaScheduler.runEntityTask(plugin, player, notify);
            } else {
                com.pumpkings.pkshopkeepers.utils.FoliaScheduler.runGlobalTask(plugin, notify);
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
