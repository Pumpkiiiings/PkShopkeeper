# PkShopkeepers

> **v1.6** — Modern, lightweight Admin Shop plugin for Paper/Folia 1.21+

PkShopkeepers is a high-performance, fully data-driven Shopkeeper plugin for Paper 1.21+ servers. It is a drop-in replacement for the original Shopkeepers plugin, focused on Admin Shops, with zero NMS hooks and full Folia compatibility.

---

## ✨ Features

- **Modern Paper API** — MiniMessage formatting, Brigadier commands, Folia-friendly schedulers. No NMS, no legacy `§` codes.
- **Fully Configurable** — Every message, GUI label, button name, and lore line is in `config.yml` / `guis.yml`. Nothing is hardcoded.
- **GUI-Based Management** — Intuitive menus to manage shop name, entity type, villager profession, biome, and level — no config editing required.
- **Villager Customization** — Change profession, biome type (Desert, Jungle, Plains, Savanna, Snow, Swamp, Taiga), and badge level from the GUI.
- **Baby Mode Toggle** — Toggle baby state for any ageable entity directly from the editor.
- **FancyNpcs & AxoNPCs Integration** — Natively bind shops to external NPC plugins.
- **Custom Item Troubleshooting** — Built-in `/pks compare` and `/pks fix` tools to diagnose and repair NBT mismatches in trades.
- **Legacy Migration Tool** — One-command migration from the original Shopkeepers plugin.
- **Folia Support** — All schedulers are Folia-safe via `FoliaScheduler`.
- **Per-Player Shop Limits** — Configurable `max-shops-per-player` in `config.yml`.
- **Nameplates, Gravity, Silence** — Fine-grained control over entity behavior per-shop via settings.

---

## 🛠 Commands

All commands require `pkshopkeepers.admin` permission.

| Command | Description |
|---|---|
| `/pks create [type]` | Create a new shop at your location |
| `/pks list` | List all active shops |
| `/pks open <id> <player>` | Open a shop for a specific player |
| `/pks remove` | Remove the nearest shop (radius 3) |
| `/pks give` | Give yourself a shop creation egg |
| `/pks link <shop> <npc>` | Link a shop to a FancyNpcs/AxoNPCs NPC |
| `/pks unlink <shop>` | Unlink a shop from an external NPC |
| `/pks compare <id>` | Compare inventory items against a shop's trades |
| `/pks fix <id> <tradeIndex> <action>` | Fix an NBT mismatch in a specific trade slot |
| `/pks fixall <id\|all>` | Fix all matching trades across one or all shops |
| `/pks fixtraders` | Re-register all shop traders |
| `/pks whois` | Identify the nearest shop (≤5 blocks) and copy its ID |
| `/pks migrate scan` | Scan original Shopkeepers data and report migrateable shops |
| `/pks migrate start` | Start full migration from original Shopkeepers plugin |
| `/pks reload` | Reload `config.yml`, `guis.yml`, and shop data |

> **Aliases:** `/pks` and `/shopkeeper`

---

## ⚙️ Configuration

### `config.yml`

All plugin messages and behavior settings live here. Messages use full **MiniMessage** syntax.

**Color palette used by default:**

| Purpose | Color |
|---|---|
| Success | `<color:#55DA50>` |
| Error | `<color:#FF4C4C>` |
| Warning / Prompt | `<color:#F4D03F>` |
| Neutral / Info | `<color:#AAAAAA>` |
| Highlighted Values | `<color:#FFFFFF>` |

**Key settings:**

```yaml
settings:
  silence-shop-entities: true        # Mute shop entity sounds
  always-show-nameplates: false      # Show nametags through walls
  disable-gravity: false             # Float entities
  max-shops-per-player: -1           # -1 = unlimited
  shop-interaction-radius: 5         # Shift+Right-click detection radius
  allow-baby-shops: true             # Allow baby mode toggle
  allow-profession-change: true      # Allow profession change from GUI
  allow-villager-type-change: true   # Allow biome change from GUI
  auto-save-on-close: true           # Auto-save when closing editor
  enabled-living-shops:              # Allowed entity types
    - VILLAGER
    - ZOMBIE
    - SKELETON
    # ...
```

---

### `guis.yml`

Every GUI title, button name, and lore line is configurable. Uses **legacy `&` color codes** for button names and **MiniMessage** for lore lines.

**Button naming convention:**
```
<dark_gray>(<color:#XXXXXX>✪</color><dark_gray>) <white>Menu <dark_gray>► <color:#XXXXXX>Action</color>
```

**Lore convention:**
```
⌜ Section Name ⌟
(blank)
ⓘ Information:
  Detail line 1
  Detail line 2
(blank)
♫ Click to action!
```

---

## 🔧 Troubleshooting Custom Items

When a shop rejects an item that looks identical in-game, use the built-in diagnosis tools:

**1. Diagnose:**
```
/pks compare <id>
```
Hold the item in your inventory. The plugin prints a detailed report: mismatching lore, different CustomModelData, enchant differences, etc.

**2. Fix:**
```
/pks fix <id> <tradeIndex> <action>
```
Hold the correct item in your main hand.

| Argument | Options |
|---|---|
| `action` | `item1`, `item2`, `result` — which trade slot to overwrite |

*Example:* `/pks fix 1 3 item1` — overwrites the first required item of trade #3 in shop `1`.

**3. Bulk Fix:**
```
/pks fixall <id|all>
```
Scans your entire inventory and fixes all matching trades in one or all shops at once.

---

## 📦 Migration from Original Shopkeepers

1. Install both `Shopkeepers` (original) and `PkShopkeepers` on the same server.
2. Run `/pks migrate scan` — shows how many Admin Shops are ready.
3. Run `/pks migrate start` — migrates all compatible shops.
4. Stop the server, remove the original `Shopkeepers` jar, restart.

All shops spawn natively via PkShopkeepers. No UUID headaches — shops use simple incremental IDs.

---

## 📋 Permissions

| Permission | Description |
|---|---|
| `pkshopkeepers.admin` | Full access to all commands and shop editing |

---

## 📌 Requirements

- **Paper** or **Folia** 1.21.1 or higher (**Spigot is NOT supported**)
- **Java 21** or higher

### Optional Dependencies

| Plugin | Purpose |
|---|---|
| [FancyNpcs](https://modrinth.com/plugin/fancynpcs) | Link shops to FancyNpcs NPCs |
| AxoNPCs | Link shops to AxoNPCs NPCs |
| Shopkeepers (original) | Required only for migration |

---

## 📝 Changelog

### v1.6
- Full English translation of all messages and GUI labels
- GUI redesign matching modern plugin aesthetics (justTeams-style):
  - `⌜ Section ⌟` lore headers
  - `ⓘ Information:` lore blocks
  - `♫ Click to action!` lore footers
  - `(←) Back` / `(✖) Close` button pill style
- Message redesign with consistent color palette (`#55DA50` / `#FF4C4C` / `#F4D03F`)
- Fixed lore rendering: was using `Component.text()` (plain), now parses MiniMessage
- Added `ConfigManager.getLoreComponents()` helper
- New settings: `max-shops-per-player`, `shop-interaction-radius`, `allow-baby-shops`, `allow-profession-change`, `allow-villager-type-change`, `auto-save-on-close`
- Biome type buttons each have a distinct color in the GUI

### v1.5
- De-hardcoded all GUI strings and messages to `config.yml` / `guis.yml`
- Added `ConfigManager.getRawString()` helper
- Fixed `MainMenuGUI.onClose` wrong config key
- Fixed `ProfessionSelectorGUI` hardcoded `§a` message
- Fixed `EntitySelectorGUI` hardcoded Yes/No baby state strings
- Added lore to all GUI buttons
- New config keys: `villager-level-changed`, `villager-type-changed`, `profession-changed`, `baby-state-on/off`, `max-shops-reached`

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
