package com.pumpkings.pkshopkeepers.api.events;

import com.pumpkings.pkshopkeepers.shop.PkShop;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ShopCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final PkShop shop;

    public ShopCreateEvent(PkShop shop) {
        this.shop = shop;
    }

    public PkShop getShop() {
        return shop;
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
