package com.pumpkings.pkshopkeepers.api.events;

import com.pumpkings.pkshopkeepers.shop.PkShop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopTradeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final PkShop shop;
    private final ItemStack resultItem;
    private final int tradeIndex;

    public ShopTradeEvent(Player player, PkShop shop, ItemStack resultItem, int tradeIndex) {
        this.player = player;
        this.shop = shop;
        this.resultItem = resultItem;
        this.tradeIndex = tradeIndex;
    }

    public Player getPlayer() {
        return player;
    }

    public PkShop getShop() {
        return shop;
    }

    public ItemStack getResultItem() {
        return resultItem;
    }

    public int getTradeIndex() {
        return tradeIndex;
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
