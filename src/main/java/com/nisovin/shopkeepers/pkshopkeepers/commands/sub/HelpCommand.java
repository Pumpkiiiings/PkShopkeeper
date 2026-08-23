package com.nisovin.shopkeepers.pkshopkeepers.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnstableApiUsage")
public class HelpCommand implements Command<CommandSourceStack> {

    private final PkShopkeepers plugin;

    public HelpCommand(PkShopkeepers plugin) {
        this.plugin = plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        sender.sendMessage(plugin.getConfigManager().parseString("&e--- PkShopkeepers Help ---"));
        sender.sendMessage(plugin.getConfigManager().parseString("&7/pks create [tipo] &8- Crear tienda"));
        sender.sendMessage(plugin.getConfigManager().parseString("&7/pks open [id] [player] &8- Abrir menú a jugador"));
        sender.sendMessage(plugin.getConfigManager().parseString("&7/pks list &8- Listar tiendas"));
        sender.sendMessage(plugin.getConfigManager().parseString("&7/pks reload &8- Recargar tiendas"));
        sender.sendMessage(plugin.getConfigManager().parseString("&7/pks remove &8- Elimina las tiendas cercanas (radio 3)"));
        sender.sendMessage(plugin.getConfigManager().parseString("&7/pks migrate &8- Migrar del antiguo plugin"));
        sender.sendMessage(plugin.getConfigManager().parseString("&7/pks link [id] [npc] &8- Vincular tienda a FancyNPC"));
        sender.sendMessage(plugin.getConfigManager().parseString("&7/pks unlink [id] &8- Desvincular tienda de FancyNPC"));
        return Command.SINGLE_SUCCESS;
    }
}
