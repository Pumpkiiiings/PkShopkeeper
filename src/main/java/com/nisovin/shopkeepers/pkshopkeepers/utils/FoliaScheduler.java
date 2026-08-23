package com.nisovin.shopkeepers.pkshopkeepers.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class FoliaScheduler {

    private static final boolean IS_FOLIA = isFolia();

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runGlobalTask(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getServer().getGlobalRegionScheduler().execute(plugin, runnable);
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runGlobalTaskLater(Plugin plugin, Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }

    public static void runRegionTask(Plugin plugin, Location location, Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getServer().getRegionScheduler().execute(plugin, location, runnable);
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runEntityTask(Plugin plugin, Entity entity, Runnable runnable) {
        if (IS_FOLIA) {
            entity.getScheduler().execute(plugin, runnable, null, 1L);
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runEntityTaskTimer(Plugin plugin, Entity entity, Runnable runnable, long initialDelay, long period) {
        if (IS_FOLIA) {
            entity.getScheduler().runAtFixedRate(plugin, task -> runnable.run(), null, initialDelay < 1 ? 1 : initialDelay, period);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, runnable, initialDelay, period);
        }
    }
}
