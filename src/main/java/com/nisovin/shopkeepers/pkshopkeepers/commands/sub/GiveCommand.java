package com.nisovin.shopkeepers.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;

@SuppressWarnings("UnstableApiUsage")
public class GiveCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public GiveCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            context.getSource().getSender().sendMessage(plugin.getConfigManager().getMessage("only-players"));
            return 0;
        }

        ItemStack egg = new ItemStack(Material.VILLAGER_SPAWN_EGG);
        egg.editMeta(meta -> {
            meta.displayName(plugin.getConfigManager().parseString("&aShopkeeper (Click Derecho)"));
            meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "shop_creation_item"), 
                org.bukkit.persistence.PersistentDataType.BYTE, 
                (byte) 1
            );
        });

        player.getInventory().addItem(egg);
        player.sendMessage(plugin.getConfigManager().parseString("&aSe te ha dado un huevo para crear tiendas."));
        
        return Command.SINGLE_SUCCESS;
    }
}
