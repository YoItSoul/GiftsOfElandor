package com.soul.goe.util.wands;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

// Wand part data - links an item to its material name and stats
public record WandMaterialData(String materialName, Item item, WandMaterialStats stats) {

    public static WandMaterialData parse(String material) {
        // Parse the new intuitive format: "name=oak,item=minecraft:oak_planks,power=1.2,stability=0.8,..."
        Map<String, String> params = parseParameters(material);

        // Required parameters
        String materialName = getRequiredParam(params, "name", material);
        String itemString = getRequiredParam(params, "item", material);

        // Parse item
        ResourceLocation itemId = ResourceLocation.tryParse(itemString);
        if (itemId == null) {
            throw new IllegalArgumentException("Invalid item ID: " + itemString + " in material: " + material);
        }

        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Unknown item for material " + materialName + ": " + itemId);
        }

        // Parse stats with defaults of 1.0
        float power = parseFloatParam(params, "power", 1.0f);
        float stability = parseFloatParam(params, "stability", 1.0f);
        float durability = parseFloatParam(params, "durability", 1.0f);
        float critical = parseFloatParam(params, "critical", 1.0f);
        String affinity = params.getOrDefault("affinity", "neutral");

        WandMaterialStats stats = new WandMaterialStats(
                power, stability, durability, critical, affinity
        );

        return new WandMaterialData(materialName, item, stats);
    }

    private static Map<String, String> parseParameters(String material) {
        Map<String, String> params = new HashMap<>();

        // Split by commas, but be careful of commas in item IDs
        String[] parts = material.split(",");

        for (String part : parts) {
            String trimmed = part.trim();
            int equalsIndex = trimmed.indexOf('=');

            if (equalsIndex == -1) {
                throw new IllegalArgumentException("Invalid parameter format (missing =): " + trimmed + " in material: " + material);
            }

            String key = trimmed.substring(0, equalsIndex).trim();
            String value = trimmed.substring(equalsIndex + 1).trim();

            params.put(key, value);
        }

        return params;
    }

    private static String getRequiredParam(Map<String, String> params, String key, String originalMaterial) {
        String value = params.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter '" + key + "' in material: " + originalMaterial);
        }
        return value;
    }

    private static float parseFloatParam(Map<String, String> params, String key, float defaultValue) {
        String value = params.get(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid float value for " + key + ": " + value);
        }
    }
}