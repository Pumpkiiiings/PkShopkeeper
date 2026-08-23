package com.nisovin.shopkeepers.pkshopkeepers.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Villager.Profession;

public class PkShop {

    private String id;
    private String name;
    private Location location;
    private UUID entityUUID;
    private org.bukkit.entity.EntityType entityType = org.bukkit.entity.EntityType.VILLAGER;
    private boolean baby = false;
    private org.bukkit.entity.Villager.Profession villagerProfession = org.bukkit.entity.Villager.Profession.NONE;
    private String fancyNpcId = null;
    private List<PkTradeOffer> offers = new ArrayList<>();

    public PkShop(String id) {
        this.id = id;
    }

    public PkShop(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name", "Shop");
        if (section.contains("entityUUID")) {
            this.entityUUID = UUID.fromString(section.getString("entityUUID"));
        }
        
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

        if (section.contains("fancy-npc-id")) {
            this.fancyNpcId = section.getString("fancy-npc-id");
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
                ItemStack item1 = offerSec.getItemStack("item1");
                ItemStack item2 = offerSec.getItemStack("item2");
                ItemStack result = offerSec.getItemStack("result");
                if (item1 != null && result != null) {
                    offers.add(new PkTradeOffer(item1, item2, result));
                }
            }
        }
    }

    public void saveToConfig(ConfigurationSection section) {
        section.set("name", name);
        if (entityUUID != null) {
            section.set("entityUUID", entityUUID.toString());
        }
        section.set("entityType", entityType.name());
        section.set("baby", baby);
        section.set("villagerProfession", villagerProfession.name());
        
        if (fancyNpcId != null) {
            section.set("fancy-npc-id", fancyNpcId);
        } else {
            section.set("fancy-npc-id", null);
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
    public String getFancyNpcId() { return fancyNpcId; }
    public void setFancyNpcId(String fancyNpcId) { this.fancyNpcId = fancyNpcId; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public UUID getEntityUUID() { return entityUUID; }
    public void setEntityUUID(UUID entityUUID) { this.entityUUID = entityUUID; }
    public org.bukkit.entity.EntityType getEntityType() { return entityType; }
    public void setEntityType(org.bukkit.entity.EntityType entityType) { this.entityType = entityType; }
    public boolean isBaby() { return baby; }
    public void setBaby(boolean baby) { this.baby = baby; }
    public org.bukkit.entity.Villager.Profession getVillagerProfession() { return villagerProfession; }
    public void setVillagerProfession(org.bukkit.entity.Villager.Profession villagerProfession) { this.villagerProfession = villagerProfession; }
    public List<PkTradeOffer> getOffers() { return offers; }
    public void setOffers(List<PkTradeOffer> offers) { this.offers = offers; }
}
