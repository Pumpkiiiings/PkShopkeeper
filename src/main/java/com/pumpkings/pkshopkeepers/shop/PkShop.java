package com.pumpkings.pkshopkeepers.shop;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Villager.Profession;

public class PkShop {

    private volatile String id;
    private volatile String name = "Shop";
    private volatile Location location;
    private volatile UUID entityUUID;
    private volatile UUID ownerUUID;
    private volatile org.bukkit.entity.EntityType entityType = org.bukkit.entity.EntityType.VILLAGER;
    private volatile boolean baby = false;
    private volatile org.bukkit.entity.Villager.Profession villagerProfession = org.bukkit.entity.Villager.Profession.NONE;
    private volatile org.bukkit.entity.Villager.Type villagerType = org.bukkit.entity.Villager.Type.PLAINS;
    private volatile int villagerLevel = 1;
    private volatile String npcId = null;
    private List<PkTradeOffer> offers = new CopyOnWriteArrayList<>();

    public PkShop(String id) {
        this.id = id;
    }

    public PkShop(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name", "Shop");
        this.entityUUID = parseUuid(section.getString("entityUUID"));
        this.ownerUUID = parseUuid(section.getString("ownerUUID"));
        
        try {
            this.entityType = org.bukkit.entity.EntityType.valueOf(section.getString("entityType", "VILLAGER"));
        } catch (Exception e) {
            this.entityType = org.bukkit.entity.EntityType.VILLAGER;
        }
        this.baby = section.getBoolean("baby", false);
        
        try {
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(section.getString("villagerProfession", "none").toLowerCase());
            Profession loadedProf = org.bukkit.Registry.VILLAGER_PROFESSION.get(key);
            this.villagerProfession = loadedProf != null ? loadedProf : org.bukkit.entity.Villager.Profession.NONE;
        } catch (Exception e) {
            this.villagerProfession = org.bukkit.entity.Villager.Profession.NONE;
        }

        try {
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(section.getString("villagerType", "plains").toLowerCase());
            org.bukkit.entity.Villager.Type loadedType = org.bukkit.Registry.VILLAGER_TYPE.get(key);
            this.villagerType = loadedType != null ? loadedType : org.bukkit.entity.Villager.Type.PLAINS;
        } catch (Exception e) {
            this.villagerType = org.bukkit.entity.Villager.Type.PLAINS;
        }

        this.villagerLevel = section.getInt("villagerLevel", 1);

        if (section.contains("npc-id")) {
            this.npcId = section.getString("npc-id");
        } else if (section.contains("fancy-npc-id")) {
            this.npcId = section.getString("fancy-npc-id");
        }
        
        String worldName = section.getString("location.world");
        if (worldName != null) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                double x = section.getDouble("location.x");
                double y = section.getDouble("location.y");
                double z = section.getDouble("location.z");
                float yaw = (float) section.getDouble("location.yaw");
                float pitch = (float) section.getDouble("location.pitch");
                this.location = new Location(world, x, y, z, yaw, pitch);
            }
        }

        ConfigurationSection offersSection = section.getConfigurationSection("offers");
        if (offersSection != null) {
            for (String key : offersSection.getKeys(false)) {
                ConfigurationSection offerSec = offersSection.getConfigurationSection(key);
                if (offerSec == null) continue;
                ItemStack item1 = offerSec.getItemStack("item1");
                ItemStack item2 = offerSec.getItemStack("item2");
                ItemStack result = offerSec.getItemStack("result");
                if (item1 != null && result != null) {
                    offers.add(new PkTradeOffer(item1, item2, result));
                }
            }
        }
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public void saveToConfig(ConfigurationSection section) {
        section.set("name", name);
        if (entityUUID != null) {
            section.set("entityUUID", entityUUID.toString());
        } else {
            section.set("entityUUID", null);
        }
        if (ownerUUID != null) {
            section.set("ownerUUID", ownerUUID.toString());
        } else {
            section.set("ownerUUID", null);
        }
        section.set("entityType", entityType.name());
        section.set("baby", baby);
        section.set("villagerProfession", villagerProfession.getKey().getKey());
        section.set("villagerType", villagerType.getKey().getKey());
        section.set("villagerLevel", villagerLevel);
        
        if (npcId != null) {
            section.set("npc-id", npcId);
        } else {
            section.set("npc-id", null);
        }
        
        if (location != null) {
            section.set("location.world", location.getWorld().getName());
            section.set("location.x", location.getX());
            section.set("location.y", location.getY());
            section.set("location.z", location.getZ());
            section.set("location.yaw", location.getYaw());
            section.set("location.pitch", location.getPitch());
        }
        
        section.set("offers", null);
        ConfigurationSection offersSection = section.createSection("offers");
        for (int i = 0; i < offers.size(); i++) {
            PkTradeOffer offer = offers.get(i);
            ConfigurationSection offerSec = offersSection.createSection(String.valueOf(i));
            offerSec.set("item1", offer.getItem1());
            if (offer.getItem2() != null) {
                offerSec.set("item2", offer.getItem2());
            }
            offerSec.set("result", offer.getResult());
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNpcId() { return npcId; }
    public void setNpcId(String npcId) { this.npcId = npcId; }
    public Location getLocation() { return location == null ? null : location.clone(); }
    public void setLocation(Location location) { this.location = location == null ? null : location.clone(); }
    public UUID getEntityUUID() { return entityUUID; }
    public void setEntityUUID(UUID entityUUID) { this.entityUUID = entityUUID; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
    public org.bukkit.entity.EntityType getEntityType() { return entityType; }
    public void setEntityType(org.bukkit.entity.EntityType entityType) { this.entityType = entityType; }
    public boolean isBaby() { return baby; }
    public void setBaby(boolean baby) { this.baby = baby; }
    public org.bukkit.entity.Villager.Profession getVillagerProfession() { return villagerProfession; }
    public void setVillagerProfession(org.bukkit.entity.Villager.Profession villagerProfession) { this.villagerProfession = villagerProfession; }
    public org.bukkit.entity.Villager.Type getVillagerType() { return villagerType; }
    public void setVillagerType(org.bukkit.entity.Villager.Type villagerType) { this.villagerType = villagerType; }
    public int getVillagerLevel() { return villagerLevel; }
    public void setVillagerLevel(int villagerLevel) { this.villagerLevel = villagerLevel; }
    public List<PkTradeOffer> getOffers() { return offers; }
    public void setOffers(List<PkTradeOffer> offers) { this.offers = new CopyOnWriteArrayList<>(offers); }
}
