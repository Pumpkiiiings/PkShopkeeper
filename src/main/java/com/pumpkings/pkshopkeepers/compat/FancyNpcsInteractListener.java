package com.pumpkings.pkshopkeepers.compat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import com.pumpkings.pkshopkeepers.shop.PkShop;
import de.oliver.fancynpcs.api.events.NpcInteractEvent;

public class FancyNpcsInteractListener implements Listener {

    private final PkShopkeepers plugin;

    public FancyNpcsInteractListener(PkShopkeepers plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onNpcInteract(NpcInteractEvent event) {
        String npcId = event.getNpc().getData().getName(); // or getId()
        Player player = event.getPlayer();

        for (PkShop shop : plugin.getShopManager().getShops()) {
            if (npcId.equals(shop.getFancyNpcId())) {
                event.setCancelled(true);
                
                if (player.isSneaking() && player.hasPermission("pkshopkeepers.admin")) {
                    plugin.getMainMenuGUI().openMenu(player, shop);
                } else {
                    plugin.getShopEntityListener().openShop(player, shop);
                }
                return;
            }
        }
    }
}
