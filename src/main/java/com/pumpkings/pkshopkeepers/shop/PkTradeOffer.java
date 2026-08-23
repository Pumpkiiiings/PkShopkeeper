package com.pumpkings.pkshopkeepers.shop;

import org.bukkit.inventory.ItemStack;

public class PkTradeOffer {

    private ItemStack item1;
    private ItemStack item2;
    private ItemStack result;

    public PkTradeOffer(ItemStack item1, ItemStack item2, ItemStack result) {
        this.item1 = item1 != null ? item1.clone() : null;
        this.item2 = item2 != null ? item2.clone() : null;
        this.result = result != null ? result.clone() : null;
    }

    public ItemStack getItem1() {
        return item1;
    }

    public void setItem1(ItemStack item1) {
        this.item1 = item1;
    }

    public ItemStack getItem2() {
        return item2;
    }

    public void setItem2(ItemStack item2) {
        this.item2 = item2;
    }

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }
}
