package com.pumpkings.pkshopkeepers.api.events;

import com.pumpkings.pkshopkeepers.shop.PkShop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ShopOpenEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final PkShop shop;

    public ShopOpenEvent(Player player, PkShop shop) {
        this.player = player;
        this.shop = shop;
    }

    public Player getPlayer() {
        return player;
    }

    public PkShop getShop() {
        return shop;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
