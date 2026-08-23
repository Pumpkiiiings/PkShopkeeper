package com.nisovin.shopkeepers.pkshopkeepers.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import com.nisovin.shopkeepers.pkshopkeepers.PkShopkeepers;
import com.nisovin.shopkeepers.pkshopkeepers.commands.sub.*;

@SuppressWarnings("UnstableApiUsage")
public class CommandManager {

    private final PkShopkeepers plugin;

    public CommandManager(PkShopkeepers plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    private void registerCommands() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();

            var rootCommand = Commands.literal("shopkeeper")
                    .requires(source -> source.getSender().hasPermission("pkshopkeepers.admin"))
                    .executes(new HelpCommand(plugin));
            
            var createSub = Commands.literal("create")
                    .executes(new CreateCommand(plugin))
                    .then(Commands.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (org.bukkit.entity.EntityType type : org.bukkit.entity.EntityType.values()) {
                                if (type.isSpawnable()) builder.suggest(type.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes(new CreateCommand(plugin)));

            var openSub = Commands.literal("open")
                    .then(Commands.argument("id", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop shop : plugin.getShopManager().getShops()) {
                                builder.suggest(shop.getId());
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("player", com.mojang.brigadier.arguments.StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                                    builder.suggest(p.getName());
                                }
                                return builder.buildFuture();
                            })
                            .executes(new OpenCommand(plugin))));
            
            var listSub = Commands.literal("list")
                    .executes(new ListCommand(plugin));

            var removeSub = Commands.literal("remove")
                    .executes(new RemoveCommand(plugin));
                    
            var reloadSub = Commands.literal("reload")
                    .executes(new ReloadCommand(plugin));

            var migrateScanSub = Commands.literal("scan")
                    .executes(new MigrateScanCommand(plugin));
            var migrateStartSub = Commands.literal("start")
                    .executes(new MigrateStartCommand(plugin));
            var migrateSub = Commands.literal("migrate")
                    .then(migrateScanSub)
                    .then(migrateStartSub);
                    
            var giveSub = Commands.literal("give")
                    .executes(new GiveCommand(plugin));

            var linkSub = Commands.literal("link")
                    .then(Commands.argument("shop", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop shop : plugin.getShopManager().getShops()) {
                                builder.suggest(shop.getId());
                            }
                            return builder.buildFuture();
                        })
                    .then(Commands.argument("npc", StringArgumentType.word())
                    .executes(new LinkCommand(plugin))));
                    
            var unlinkSub = Commands.literal("unlink")
                    .then(Commands.argument("shop", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop shop : plugin.getShopManager().getShops()) {
                                builder.suggest(shop.getId());
                            }
                            return builder.buildFuture();
                        })
                    .executes(new UnlinkCommand(plugin)));

            var compareSub = Commands.literal("compare")
                    .then(Commands.argument("id", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop shop : plugin.getShopManager().getShops()) {
                                builder.suggest(shop.getId());
                            }
                            return builder.buildFuture();
                        })
                    .executes(new CompareCommand(plugin)));

            var fixSub = Commands.literal("fix")
                    .then(Commands.argument("id", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (com.nisovin.shopkeepers.pkshopkeepers.shop.PkShop shop : plugin.getShopManager().getShops()) {
                                builder.suggest(shop.getId());
                            }
                            return builder.buildFuture();
                        })
                    .then(Commands.argument("tradeIndex", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                    .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("item1");
                            builder.suggest("item2");
                            builder.suggest("result");
                            return builder.buildFuture();
                        })
                    .executes(new FixCommand(plugin)))));
                    
            var simplifyIdsSub = Commands.literal("simplifyids")
                    .executes(new SimplifyIdsCommand(plugin));
                    
            var whoisSub = Commands.literal("whois")
                    .executes(new WhoisCommand(plugin));

            rootCommand.then(createSub);
            rootCommand.then(openSub);
            rootCommand.then(listSub);
            rootCommand.then(removeSub);
            rootCommand.then(reloadSub);
            rootCommand.then(migrateSub);
            rootCommand.then(giveSub);
            rootCommand.then(linkSub);
            rootCommand.then(unlinkSub);
            rootCommand.then(compareSub);
            rootCommand.then(fixSub);
            rootCommand.then(simplifyIdsSub);
            rootCommand.then(whoisSub);

            commands.register(rootCommand.build(), "PkShopkeepers main command", java.util.List.of("pks"));
        });
    }
}
