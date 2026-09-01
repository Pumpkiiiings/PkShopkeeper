package com.pumpkings.pkshopkeepers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ArchitectureGuardTest {

    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");

    @Test
    void registersExactlyOneShopEntityListener() throws IOException {
        String sources = allJavaSources();
        Matcher matcher = Pattern.compile("new\\s+(?:[\\w.]+\\.)?ShopEntityListener\\s*\\(").matcher(sources);
        int registrations = 0;
        while (matcher.find()) registrations++;
        assertEquals(1, registrations, "ShopEntityListener must only be constructed by the plugin bootstrap");
    }

    @Test
    void keepsFoliaSensitiveSchedulingBehindTheAdapter() throws IOException {
        try (Stream<Path> paths = Files.walk(SOURCE_ROOT)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                if (path.getFileName().toString().equals("FoliaScheduler.java")) continue;
                String source = Files.readString(path);
                assertFalse(source.contains("Bukkit.getScheduler()"), path + " bypasses FoliaScheduler");
                assertFalse(source.contains("getServer().getScheduler()"), path + " bypasses FoliaScheduler");
                assertFalse(source.contains("new org.bukkit.event.world.ChunkUnloadEvent"), path + " creates a synthetic chunk event");
                assertFalse(source.contains("new org.bukkit.event.world.ChunkLoadEvent"), path + " creates a synthetic chunk event");
                assertFalse(source.contains("getWorld().getEntities()"), path + " scans an entire world from one region");
            }
        }
    }

    @Test
    void everyAdvertisedBehaviorSettingIsUsed() throws IOException {
        String sources = allJavaSources();
        for (String setting : new String[] {
                "max-shops-per-player", "shop-interaction-radius", "allow-baby-shops",
                "allow-profession-change", "allow-villager-type-change", "auto-save-on-close"
        }) {
            assertTrue(sources.contains("settings." + setting), setting + " is advertised but not implemented");
        }
    }

    private String allJavaSources() throws IOException {
        StringBuilder result = new StringBuilder();
        try (Stream<Path> paths = Files.walk(SOURCE_ROOT)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                result.append(Files.readString(path)).append('\n');
            }
        }
        return result.toString();
    }
}
