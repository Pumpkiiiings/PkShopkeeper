package com.pumpkings.pkshopkeepers.compat;

import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import org.axostudio.axonpcs.api.event.AxoNPCInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;

public class AxoNpcsInteractListener implements Listener {

    private final PkShopkeepers plugin;

    public AxoNpcsInteractListener(PkShopkeepers plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onNpcInteract(AxoNPCInteractEvent event) {
        String npcId = event.getNPC().getId();
        Player player = event.getPlayer();

        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (npcId.equals(shop.getNpcId())) {
                event.setCancelled(true);
                
                if (player.isSneaking() && player.hasPermission("pkshopkeepers.admin")) {
                    plugin.getMainMenuGUI().openMenu(player, shop);
                } else {
                    plugin.getShopEntityListener().openShop(player, shop);
                }
                break;
            }
        }
    }
}
