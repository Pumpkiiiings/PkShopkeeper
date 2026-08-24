package com.pumpkings.pkshopkeepers.utils;

import org.bukkit.configuration.file.FileConfiguration;
import com.pumpkings.pkshopkeepers.PkShopkeepers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final PkShopkeepers plugin;
    private FileConfiguration config;
    private org.bukkit.configuration.file.FileConfiguration guiConfig;
    private java.io.File guiFile;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public ConfigManager(PkShopkeepers plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        guiFile = new java.io.File(plugin.getDataFolder(), "guis.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("guis.yml", false);
        }
        guiConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(guiFile);
    }

    public Component getMessage(String path, String... placeholders) {
        String prefix = config.getString("messages.prefix", "&8[&ePkShopkeepers&8] ");
        String message = config.getString("messages." + path, "");
        if (message.isEmpty()) return Component.empty();
        
        String full = prefix + message;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                full = full.replace(placeholders[i], placeholders[i+1]);
            }
        }
        return parseString(full);
    }
    
    public Component getMessageRaw(String path, String... placeholders) {
        String message = config.getString("messages." + path, "");
        if (message.isEmpty()) return Component.empty();
        
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i+1]);
            }
        }
        return parseString(message);
    }

    public Component getMenuComponent(String path) {
        String message = guiConfig.getString(path, path);
        // By default Adventure components are italic if not explicitly stated, we want to remove italic in menus
        return parseString(message).decoration(TextDecoration.ITALIC, false);
    }

    public String getGuiString(String path, String def) {
        return guiConfig.getString(path, def);
    }
    
    public List<String> getGuiStringList(String path) {
        return guiConfig.getStringList(path);
    }

    public Component parseString(String text) {
        if (text == null) return Component.empty();
        
        // If string contains MiniMessage tags, prefer MiniMessage
        if (text.contains("<") && text.contains(">")) {
            // MiniMessage no permite el uso de códigos legacy (como §), así que no los reemplazamos.
            // Si el usuario usa <tags>, debe usar MiniMessage completamente en ese mensaje.
            return miniMessage.deserialize(text);
        } else {
            // Legacy + Hex support
            return legacySerializer.deserialize(text);
        }
    }
    
    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }
    
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }
}
