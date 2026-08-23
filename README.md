# PkShopkeepers

PkShopkeepers is a high-performance, lightweight, and modern Shopkeeper plugin designed specifically for Paper 1.21.1+ servers. It serves as a drop-in replacement for the original Shopkeepers plugin, focused exclusively on Admin Shops, avoiding NMS hooks to ensure future version compatibility and eliminating legacy codebase overhead.

## Features

- **Modern Paper API**: Built from the ground up utilizing the latest Paper features, including MiniMessage for all text formatting, Brigadier commands, and Folia-friendly schedulers.
- **No NMS & Version Independent**: Uses standard Bukkit/Paper API methods (like `Registry`) instead of NMS, ensuring the plugin won't break with every minor Minecraft update.
- **Admin Shops Focused**: Optimized entirely for Admin Shops, removing the bloat from player shop features.
- **GUI-Based Management**: Intuitive and dynamic GUI for managing shops. Change shop names, entity types (e.g., Zombies, Skeletons, Villagers), and even villager professions without touching a config file.
- **Look at Player Support**: Entities smoothly turn their heads to look at nearby players (powered by FoliaScheduler).
- **FancyNpcs Integration**: Native support for binding shops to [FancyNpcs](https://modrinth.com/plugin/fancynpcs).
- **Legacy Migration Tool**: Includes a built-in migrator to easily transfer all your Admin Shops from the original Shopkeepers plugin to PkShopkeepers in seconds.

## Commands

All commands require the `pkshopkeepers.admin` permission.

- `/pks create [type]` - Creates a new shop at your location.
- `/pks list` - Lists all currently active shops.
- `/pks open <id> <player>` - Opens a shop for a specific player (Useful for Citizens/FancyNpcs).
- `/pks remove` - Removes the nearest shop.
- `/pks give` - Gives you a spawn egg to easily place shops.
- `/pks link <shop> <npc>` - Links a shop to a FancyNpcs entity.
- `/pks unlink <shop>` - Unlinks a shop from a FancyNpcs entity.
- `/pks compare <id>` - Compares the items in your inventory with the required items of a shop to help troubleshoot custom items.
- `/pks fix <id> <tradeIndex> <action>` - Advanced command to fix NBT mismatches in specific trades.
- `/pks simplifyids` - Converts long UUID shop IDs to simple, incremental IDs (1, 2, 3...) for easier management.
- `/pks whois` - Identifies the shop nearest to you (up to 5 blocks away) and provides a clickable text to copy its ID.
- `/pks migrate scan` - Scans existing Shopkeepers data and reports how many shops can be migrated.
- `/pks migrate start` - Starts the migration process from the original Shopkeepers plugin.
- `/pks reload` - Reloads the configuration.

## Troubleshooting Custom Items

When using custom items (items with custom lore, enchants, CustomModelData, or 1.21 Data Components), sometimes a shop won't accept an item that looks identical to the player. PkShopkeepers provides built-in tools to diagnose and fix these issues:

1. **Diagnose the issue:**
   Hold the problematic item in your inventory and run `/pks compare <id>`. The plugin will scan your inventory and compare it against the shop's requirements. It will print a detailed report in chat explaining exactly why the item is being rejected (e.g., mismatching Lore line, different CustomModelData, hidden attributes, etc.).

2. **Fix the issue:**
   If you want to update the shop to accept the exact item you are holding, hold the item in your main hand and use the `/pks fix` command:
   ```text
   /pks fix <id> <tradeIndex> <action>
   ```
   - `<id>`: The ID of the shop (e.g., `1`).
   - `<tradeIndex>`: The trade number you want to fix (e.g., `1` for the first trade, `2` for the second).
   - `<action>`: Which slot to overwrite in that trade. Options are `item1` (first requirement), `item2` (second requirement), or `result` (what the player gets).

   *Example:* `/pks fix 1 3 item1` will overwrite the first required item of trade #3 in shop ID 1 with the item currently in your hand.

## Migration Guide

Migrating from the original Shopkeepers plugin is incredibly easy:
1. Ensure both the original `Shopkeepers` plugin and `PkShopkeepers` are installed and running.
2. Run `/pks migrate scan` to verify how many Admin Shops are ready to be migrated.
3. Run `/pks migrate start`.
4. Run `/pks simplifyids` if you want your new shops to have simple numeric IDs instead of long UUIDs.
5. Shut down your server, remove the original `Shopkeepers` jar, and start your server again. All your shops will be seamlessly spawned by PkShopkeepers!

## Requirements

- **Paper / Folia** 1.21.1 or higher (Spigot is NOT supported).
- Java 21 or higher.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
