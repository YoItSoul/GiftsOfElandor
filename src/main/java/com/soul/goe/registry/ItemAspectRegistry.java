package com.soul.goe.registry;

import com.soul.goe.Config;
import com.soul.goe.Goe;
import com.soul.goe.api.aspects.Aspect;
import com.soul.goe.api.aspects.AspectList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ItemAspectRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(Goe.MODID + "/ItemAspects");
    private final Map<ResourceLocation, AspectList> itemAspects = new HashMap<>();
    private final AspectRegistry aspectRegistry;

    public ItemAspectRegistry(AspectRegistry aspectRegistry) {
        this.aspectRegistry = aspectRegistry;
    }

    public void loadItemAspects() {
        LOGGER.info("Loading item aspects from config");
        itemAspects.clear();

        List<? extends String> configAspects = Config.ITEM_ASPECTS.get();
        Set<ResourceLocation> processedItems = new HashSet<>();
        List<String> uniqueEntries = new ArrayList<>();

        for (String entry : configAspects) {
            try {
                String[] parts = entry.split("->");
                ResourceLocation itemId = ResourceLocation.tryParse(parts[0].trim());

                if (itemId == null) {
                    LOGGER.error("Invalid item ID: {}", parts[0].trim());
                    continue;
                }

                if (processedItems.add(itemId)) {
                    processItemAspectEntry(entry);
                    uniqueEntries.add(entry);
                } else {
                    LOGGER.info("Removing duplicate aspect entry for item: {}", itemId);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to process item aspect entry: {}", entry, e);
            }
        }

        Config.ITEM_ASPECTS.set(uniqueEntries);
        LOGGER.info("Successfully loaded aspects for {} items", itemAspects.size());

        printItemsWithoutAspects();
    }

    private void printItemsWithoutAspects() {
        List<ResourceLocation> itemsWithoutAspects = new ArrayList<>();

        for (ResourceLocation itemId : BuiltInRegistries.ITEM.keySet()) {
            if (!itemAspects.containsKey(itemId)) {
                itemsWithoutAspects.add(itemId);
            }
        }

        if (!itemsWithoutAspects.isEmpty()) {
            LOGGER.info("=== Items Without Aspects ({} items) ===", itemsWithoutAspects.size());

            itemsWithoutAspects.sort(Comparator.comparing(ResourceLocation::toString));

            // Join all items with commas
            String itemList = String.join(", ", itemsWithoutAspects.stream().map(ResourceLocation::toString).toList());

            LOGGER.info(itemList);
            LOGGER.info("=== End of List ===");
        }
    }

    private void processItemAspectEntry(String entry) {
        String[] parts = entry.split("->");
        ResourceLocation itemId = ResourceLocation.tryParse(parts[0].trim());
        if (itemId == null) {
            LOGGER.error("Invalid item ID: {}", parts[0].trim());
            return;
        }

        AspectList aspects = new AspectList();
        String[] aspectEntries = parts[1].split(",");

        for (String aspectEntry : aspectEntries) {
            String[] aspectParts = aspectEntry.trim().split(":");
            String aspectName = aspectParts[0];
            int amount = Integer.parseInt(aspectParts[1]);

            Aspect aspect = aspectRegistry.getAspect(aspectName);
            if (aspect != null) {
                aspects.add(aspect, amount);
            } else {
                LOGGER.warn("Unknown aspect '{}' for item {}", aspectName, itemId);
            }
        }

        itemAspects.put(itemId, aspects);
        LOGGER.debug("Registered aspects for item {}: {}", itemId, aspectListToString(aspects));
    }

    private String aspectListToString(AspectList aspects) {
        StringBuilder sb = new StringBuilder();
        aspects.getAspects().forEach((aspect, amount) -> sb.append(aspect.getName()).append("=").append(amount).append(", "));
        return !sb.isEmpty() ? sb.substring(0, sb.length() - 2) : "none";
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
