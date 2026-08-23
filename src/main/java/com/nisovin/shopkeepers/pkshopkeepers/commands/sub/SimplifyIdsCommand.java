package com.nisovin.shopkeepers.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;
import com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop;
import com.nisovin.shopkeepers.pkshopkeepers.shop.ShopManager;

@SuppressWarnings("UnstableApiUsage")
public class SimplifyIdsCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public SimplifyIdsCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        
        ShopManager manager = plugin.getShopManager();
        List<PkShop> shopsToConvert = new ArrayList<>();
        
        for (PkShop shop : manager.getShops()) {
            if (shop.getId().length() > 10) {
                shopsToConvert.add(shop);
            }
        }
        
        if (shopsToConvert.isEmpty()) {
            sender.sendMessage("§cNo se encontraron tiendas con IDs largos para simplificar.");
            return Command.SINGLE_SUCCESS;
        }
        
        int count = 0;
        for (PkShop shop : shopsToConvert) {
            manager.removeShop(shop.getId(), false);
            
            String newId = manager.getNextId();
            shop.setId(newId);
            manager.addShop(shop);
            count++;
        }
        
        manager.saveShops();
        sender.sendMessage("§a¡Se han simplificado " + count + " IDs de tiendas exitosamente!");
        
        return Command.SINGLE_SUCCESS;
    }
}
