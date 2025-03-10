package com.soul.goe.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.soul.goe.Goe;
import com.soul.goe.api.aspects.Aspect;
import com.soul.goe.api.aspects.AspectList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ItemAspectRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(Goe.MODID + "/ItemAspects");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<ResourceLocation, AspectList> itemAspects = new HashMap<>();
    private final AspectRegistry aspectRegistry;

    public ItemAspectRegistry(AspectRegistry aspectRegistry) {
        this.aspectRegistry = aspectRegistry;
    }

    public void loadItemAspects(String path) {
        LOGGER.info("Loading item aspects from: {}", path);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                LOGGER.error("Could not find item aspect file: {}", path);
                throw new RuntimeException("Could not find item aspect file: " + path);
            }

            JsonObject json = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
            validateJsonStructure(json);
            processItemEntries(json);

            LOGGER.info("Successfully loaded aspects for {} items", itemAspects.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load item aspects", e);
            throw new RuntimeException("Failed to load item aspects", e);
        }
    }

    private void validateJsonStructure(JsonObject json) {
        if (!json.has("items")) {
            LOGGER.error("Invalid item aspect file format: missing 'items' array");
            throw new RuntimeException("Invalid item aspect file format: missing 'items' array");
        }
    }

    private void processItemEntries(JsonObject json) {
        json.getAsJsonArray("items").forEach(element -> {
            JsonObject itemObj = element.getAsJsonObject();
            String itemId = itemObj.get("id").getAsString();
            processItemEntry(itemId, itemObj);
        });
    }

    private void processItemEntry(String itemId, JsonObject itemObj) {
        ResourceLocation itemKey = ResourceLocation.tryParse(itemId);
        if (itemKey == null) {
            LOGGER.error("Invalid item ID format: {}", itemId);
            throw new RuntimeException("Invalid item ID format: " + itemId);
        }

        AspectList aspects = new AspectList();
        JsonObject aspectsObj = itemObj.getAsJsonObject("aspects");

        processAspects(itemId, aspects, aspectsObj);

        itemAspects.put(itemKey, aspects);
        LOGGER.debug("Registered aspects for item {}: {}", itemId, aspectListToString(aspects));
    }

    private void processAspects(String itemId, AspectList aspects, JsonObject aspectsObj) {
        aspectsObj.entrySet().forEach(entry -> {
            String aspectName = entry.getKey();
            int amount = entry.getValue().getAsInt();
            Aspect aspect = aspectRegistry.getAspect(aspectName);

            if (aspect != null) {
                aspects.add(aspect, amount);
            } else {
                LOGGER.warn("Unknown aspect '{}' for item {}", aspectName, itemId);
            }
        });
    }

    private String aspectListToString(AspectList aspects) {
        StringBuilder sb = new StringBuilder();
        aspects.getAspects().forEach((aspect, amount) ->
                sb.append(aspect.getName()).append("=").append(amount).append(", ")
        );
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "none";
    }

    // Getter methods
    public AspectList getAspects(ResourceLocation itemId) {
        return itemAspects.getOrDefault(itemId, new AspectList());
    }

    public AspectList getAspects(Item item) {
        return getAspects(BuiltInRegistries.ITEM.getKey(item));
    }

    public boolean hasAspects(ResourceLocation itemId) {
        return itemAspects.containsKey(itemId);
    }

    public boolean hasAspects(Item item) {
        return hasAspects(BuiltInRegistries.ITEM.getKey(item));
    }
}