package com.pumpkings.pkshopkeepers.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

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

    public static CompletableFuture<Void> runRegionTask(Plugin plugin, Location location, Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Runnable guarded = () -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
                plugin.getLogger().severe("Region task failed: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        };
        if (IS_FOLIA) {
            Bukkit.getServer().getRegionScheduler().execute(plugin, location, guarded);
        } else {
            Bukkit.getScheduler().runTask(plugin, guarded);
        }
        return future;
    }

    public static CompletableFuture<Void> runRegionTaskLater(Plugin plugin, Location location, Runnable runnable, long delayTicks) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Consumer<ScheduledTask> guarded = task -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
                plugin.getLogger().severe("Delayed region task failed: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        };
        if (IS_FOLIA) {
            Bukkit.getServer().getRegionScheduler().runDelayed(plugin, location, guarded, Math.max(1L, delayTicks));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> guarded.accept(null), delayTicks);
        }
        return future;
    }

    public static CompletableFuture<Void> runEntityTask(Plugin plugin, Entity entity, Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Runnable guarded = () -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
                plugin.getLogger().severe("Entity task failed: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        };
        if (IS_FOLIA) {
            boolean scheduled = entity.getScheduler().execute(plugin, guarded,
                    () -> future.completeExceptionally(new IllegalStateException("Entity was retired before task execution")), 1L);
            if (!scheduled) {
                future.completeExceptionally(new IllegalStateException("Entity task could not be scheduled"));
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, guarded);
        }
        return future;
    }

    public static void runEntityTaskLater(Plugin plugin, Entity entity, Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            entity.getScheduler().runDelayed(plugin, task -> runnable.run(), null, Math.max(1L, delayTicks));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }

    public static ScheduledTask runEntityTaskTimer(Plugin plugin, Entity entity, Consumer<ScheduledTask> runnable,
                                                   Runnable retired, long initialDelay, long period) {
        if (IS_FOLIA) {
            return entity.getScheduler().runAtFixedRate(plugin, runnable, retired,
                    Math.max(1L, initialDelay), Math.max(1L, period));
        } else {
            final org.bukkit.scheduler.BukkitTask[] holder = new org.bukkit.scheduler.BukkitTask[1];
            holder[0] = Bukkit.getScheduler().runTaskTimer(plugin,
                    () -> runnable.accept(new BukkitScheduledTask(holder[0], plugin)), initialDelay, period);
            return new BukkitScheduledTask(holder[0], plugin);
        }
    }

    public static boolean isFoliaServer() {
        return IS_FOLIA;
    }

    private record BukkitScheduledTask(org.bukkit.scheduler.BukkitTask delegate, Plugin plugin) implements ScheduledTask {
        @Override
        public Plugin getOwningPlugin() {
            return plugin;
        }

        @Override
        public boolean isRepeatingTask() {
            return true;
        }

        @Override
        public CancelledState cancel() {
            boolean alreadyCancelled = delegate.isCancelled();
            delegate.cancel();
            return alreadyCancelled ? CancelledState.CANCELLED_ALREADY : CancelledState.CANCELLED_BY_CALLER;
        }

        @Override
        public ExecutionState getExecutionState() {
            return delegate.isCancelled() ? ExecutionState.CANCELLED : ExecutionState.IDLE;
        }
    }
}
