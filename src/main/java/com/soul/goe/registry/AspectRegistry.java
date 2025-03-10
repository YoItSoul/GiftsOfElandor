package com.soul.goe.registry;

import com.google.gson.*;
import com.soul.goe.Goe;
import com.soul.goe.api.aspects.Aspect;
import com.soul.goe.api.aspects.AspectList;
import net.minecraft.resources.ResourceLocation;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Registry for managing and loading aspect definitions from JSON files.
 * Handles both base aspects and custom overrides.
 */
/**
 * Registry for managing and loading aspect definitions from JSON files.
 * Handles both base aspects and custom overrides.
 */
public class AspectRegistry {
    private static final Gson GSON = new GsonBuilder().create();
    private final Map<String, Aspect> aspects = new HashMap<>();
    private static final String FOLDER = "aspects";
    private static final boolean DEBUG_ASPECTS = true; // Can be toggled as needed


    public void registerDefaultAspects() {
        if (DEBUG_ASPECTS) {
            Goe.LOGGER.info("[Aspects Debug] Starting aspect registration...");
        }

        aspects.clear();
        loadBaseAspects();
        loadOverrideAspects();

        if (DEBUG_ASPECTS) {
            Goe.LOGGER.info("[Aspects Debug] Aspect registration complete. Total aspects: {}", aspects.size());
            Goe.LOGGER.info("[Aspects Debug] ---------------");
            aspects.forEach((name, aspect) -> {
                if (aspect.getComponents() == null) {
                    Goe.LOGGER.info("[Aspects Debug] {} (Primal)", name);
                } else {
                    StringBuilder components = new StringBuilder();
                    aspect.getComponents().getAspects().forEach((comp, count) ->
                            components.append(comp.getName()).append(" x").append(count).append(" + "));
                    if (components.length() > 0) {
                        components.setLength(components.length() - 3);
                    }
                    Goe.LOGGER.info("[Aspects Debug] {} = [{}]", name, components);
                }
            });
            Goe.LOGGER.info("[Aspects Debug] ---------------");
        }
    }



private void loadBaseAspects() {
        try (InputStream is = getClass().getResourceAsStream("/data/" + Goe.MODID + "/" + FOLDER + "/base_aspects.json")) {
            if (is != null) {
                JsonObject json = GSON.fromJson(new InputStreamReader(is), JsonObject.class);
                ResourceLocation location = ResourceLocation.tryParse(Goe.MODID + ":base_aspects");
                if (location != null) {
                    loadAspectsFromJson(json, location);
                }
            } else {
                Goe.LOGGER.error("Failed to load base_aspects.json: File not found");
            }
        } catch (IOException | JsonParseException e) {
            Goe.LOGGER.error("Failed to load base aspects: ", e);
        }
    }

    private void loadOverrideAspects() {
        try (InputStream is = getClass().getResourceAsStream("/data/" + Goe.MODID + "/" + FOLDER + "/override_aspects.json")) {
            if (is != null) {
                JsonObject json = GSON.fromJson(new InputStreamReader(is), JsonObject.class);
                ResourceLocation location = ResourceLocation.tryParse(Goe.MODID + ":override_aspects");
                if (location != null) {
                    loadAspectsFromJsonWithErrorHandling(json, location);
                }
            } else {
                Goe.LOGGER.debug("No override_aspects.json found - skipping overrides");
            }
        } catch (IOException | JsonParseException e) {
            Goe.LOGGER.error("Failed to load aspect overrides: ", e);
        }
    }

    private void loadAspectsFromJsonWithErrorHandling(JsonObject json, ResourceLocation location) {
        validateJsonStructure(json);
        JsonObject elements = json.getAsJsonObject("elements");
        Map<String, String> compoundCombinations = new HashMap<>();
        Set<String> processedDependencies = new HashSet<>();

        try {
            handleRemovals(elements);
        } catch (Exception e) {
            Goe.LOGGER.error("Error processing removals in override aspects: ", e);
        }

        try {
            loadPrimalAspects(elements);
        } catch (Exception e) {
            Goe.LOGGER.error("Error processing primal aspects in override: ", e);
        }

        try {
            loadCompoundAspects(elements, compoundCombinations, processedDependencies);
        } catch (Exception e) {
            Goe.LOGGER.error("Error processing compound aspects in override: ", e);
        }
    }

    public Aspect getAspect(String name) {
        return aspects.get(name);
    }

    private void loadAspectsFromJson(JsonObject json, ResourceLocation location) {
        validateJsonStructure(json);
        JsonObject elements = json.getAsJsonObject("elements");
        Map<String, String> compoundCombinations = new HashMap<>();
        Set<String> processedDependencies = new HashSet<>();

        handleRemovals(elements);
        loadPrimalAspects(elements);
        loadCompoundAspects(elements, compoundCombinations, processedDependencies);
    }



    /**
     * Validates the basic JSON structure.
     * @param json The JSON object to validate
     * @throws JsonParseException if the structure is invalid
     */
    private void validateJsonStructure(JsonObject json) {
        if (!json.has("elements")) {
            throw new JsonParseException("Missing required 'elements' object in aspect definition!");
        }

        JsonObject elements = json.getAsJsonObject("elements");
        validateArrayField(elements, "remove");
        validateArrayField(elements, "primal");
        validateArrayField(elements, "compound");
    }

    /**
     * Validates that a field is an array if present.
     * @param elements The JSON object containing the field
     * @param fieldName The name of the field to validate
     */
    private void validateArrayField(JsonObject elements, String fieldName) {
        if (elements.has(fieldName) && !elements.get(fieldName).isJsonArray()) {
            throw new JsonParseException("'" + fieldName + "' must be an array!");
        }
    }

    /**
     * Processes aspect removals.
     * @param elements The JSON elements object
     */
    private void handleRemovals(JsonObject elements) {
        if (!elements.has("remove")) return;

        JsonArray removeArray = elements.getAsJsonArray("remove");
        for (JsonElement element : removeArray) {
            String aspectName = element.getAsString();
            validateRemoval(aspectName);
            aspects.remove(aspectName);

            if (DEBUG_ASPECTS) {
                Goe.LOGGER.info("[Aspects Debug] Removed aspect: {}", aspectName);
            }
        }
    }


    /**
     * Loads primal aspect definitions.
     * @param elements The JSON elements object
     */
    private void loadPrimalAspects(JsonObject elements) {
        if (!elements.has("primal")) return;

        JsonArray primalAspects = elements.getAsJsonArray("primal");
        for (JsonElement element : primalAspects) {
            JsonObject aspectObj = element.getAsJsonObject();
            validateAspectObject(aspectObj, "Primal");

            String name = aspectObj.get("name").getAsString();
            String translation = aspectObj.get("translation").getAsString();

            validateAspectName(name);
            validateTranslation(translation);
            validateNotDuplicate(name);

            aspects.put(name, new Aspect(name, name.hashCode() & 0xFFFFFF, null, translation));

            if (DEBUG_ASPECTS) {
                Goe.LOGGER.info("[Aspects Debug] Registered primal aspect: {} ({})", name, translation);
            }
        }
    }


    /**
     * Loads compound aspect definitions.
     * @param elements The JSON elements object
     * @param compoundCombinations Map to track existing combinations
     * @param processedDependencies Set to track and prevent cyclic dependencies
     */
    private void loadCompoundAspects(JsonObject elements, Map<String, String> compoundCombinations,
                                     Set<String> processedDependencies) {
        if (!elements.has("compound")) return;

        JsonArray compoundAspects = elements.getAsJsonArray("compound");
        for (JsonElement element : compoundAspects) {
            JsonObject aspectObj = element.getAsJsonObject();
            validateCompoundAspectObject(aspectObj);

            String name = aspectObj.get("name").getAsString();
            String translation = aspectObj.get("translation").getAsString();

            validateAspectName(name);
            validateTranslation(translation);
            validateNotDuplicate(name);

            AspectList components = processCompoundComponents(aspectObj, name,
                    compoundCombinations, processedDependencies);

            int color = calculateCompoundColor(components);
            aspects.put(name, new Aspect(name, color, components, translation));

            if (DEBUG_ASPECTS) {
                StringBuilder componentsStr = new StringBuilder();
                components.getAspects().forEach((aspect, count) ->
                        componentsStr.append(aspect.getName()).append(" x").append(count).append(" + "));
                if (componentsStr.length() > 0) {
                    componentsStr.setLength(componentsStr.length() - 3); // Remove last " + "
                }
                Goe.LOGGER.info("[Aspects Debug] Registered compound aspect: {} ({}) = [{}]",
                        name, translation, componentsStr);
            }
        }
    }


    /**
     * Processes and validates compound aspect components.
     * @return The processed AspectList
     */
    private AspectList processCompoundComponents(JsonObject aspectObj, String aspectName,
                                                 Map<String, String> compoundCombinations,
                                                 Set<String> processedDependencies) {
        JsonArray componentArray = aspectObj.getAsJsonArray("components");
        AspectList components = new AspectList();
        String[] componentNames = new String[2];

        for (int i = 0; i < componentArray.size(); i++) {
            String componentName = componentArray.get(i).getAsString();
            componentNames[i] = componentName;

            validateComponentExists(componentName, aspectName);
            validateNoCyclicDependencies(aspectName, componentName, processedDependencies);

            components.add(aspects.get(componentName), 1);
        }

        validateCompoundCombination(componentNames, aspectName, compoundCombinations);
        return components;
    }

    /**
     * Validates an aspect name format.
     */
    private void validateAspectName(String name) {
        if (name == null || name.isEmpty()) {
            throw new JsonParseException("Aspect name cannot be empty!");
        }
        if (!name.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            throw new JsonParseException("Invalid aspect name '" + name +
                    "'. Names must start with a letter and contain only letters, numbers, and underscores!");
        }
    }

    /**
     * Validates an aspect translation.
     */
    private void validateTranslation(String translation) {
        if (translation == null || translation.isEmpty()) {
            throw new JsonParseException("Aspect translation cannot be empty!");
        }
    }

    /**
     * Validates that an aspect name isn't already in use.
     */
    private void validateNotDuplicate(String name) {
        if (aspects.containsKey(name)) {
            throw new JsonParseException("Duplicate aspect name found: '" + name +
                    "'. Aspect names must be unique!");
        }
    }

    /**
     * Validates a removal operation.
     */
    private void validateRemoval(String aspectName) {
        if (aspectName == null || aspectName.isEmpty()) {
            throw new JsonParseException("Remove entry cannot be empty!");
        }
        if (!aspects.containsKey(aspectName)) {
            throw new JsonParseException("Cannot remove non-existent aspect: '" + aspectName + "'!");
        }
    }

    /**
     * Validates no cyclic dependencies exist.
     */
    private void validateNoCyclicDependencies(String aspectName, String componentName,
                                              Set<String> processedDependencies) {
        if (processedDependencies.contains(componentName)) {
            throw new JsonParseException("Cyclic dependency detected for aspect '" + aspectName + "'!");
        }
        processedDependencies.add(aspectName);
    }

    /**
     * Validates compound combinations are unique.
     */
    private void validateCompoundCombination(String[] componentNames, String aspectName,
                                             Map<String, String> compoundCombinations) {
        String[] sortedComponents = componentNames.clone();
        Arrays.sort(sortedComponents);
        String combinationKey = sortedComponents[0] + "+" + sortedComponents[1];

        if (compoundCombinations.containsKey(combinationKey)) {
            throw new JsonParseException("Duplicate aspect combination found: '" + combinationKey +
                    "' in aspects '" + compoundCombinations.get(combinationKey) + "' and '" +
                    aspectName + "'!");
        }
        compoundCombinations.put(combinationKey, aspectName);
    }

    /**
     * Validates that a component aspect exists.
     */
    private void validateComponentExists(String componentName, String aspectName) {
        if (!aspects.containsKey(componentName)) {
            throw new JsonParseException("Invalid component aspect '" + componentName +
                    "' in compound aspect '" + aspectName + "'!");
        }
    }

    /**
     * Calculates the color for a compound aspect based on its components.
     */
    private int calculateCompoundColor(AspectList components) {
        if (components == null || components.isEmpty()) {
            return 0xFFFFFF;
        }

        int r = 0, g = 0, b = 0;
        int count = 0;

        for (Map.Entry<Aspect, Integer> entry : components.getAspects().entrySet()) {
            int color = entry.getKey().getColor();
            r += ((color >> 16) & 0xFF) * entry.getValue();
            g += ((color >> 8) & 0xFF) * entry.getValue();
            b += (color & 0xFF) * entry.getValue();
            count += entry.getValue();
        }

        if (count > 0) {
            r /= count;
            g /= count;
            b /= count;
        }

        return (r << 16) | (g << 8) | b;
    }


    /**
     * Validates the structure of a basic aspect object.
     * @param aspectObj The JSON object representing the aspect
     * @param aspectType The type of aspect being validated (for error messages)
     * @throws JsonParseException if the aspect object is invalid
     */
    private void validateAspectObject(JsonObject aspectObj, String aspectType) {
        if (!aspectObj.has("name")) {
            throw new JsonParseException(aspectType + " aspect missing required 'name' field!");
        }
        if (!aspectObj.has("translation")) {
            throw new JsonParseException(aspectType + " aspect missing required 'translation' field!");
        }

        // Validate that the fields are of the correct type
        if (!aspectObj.get("name").isJsonPrimitive() || !aspectObj.get("name").getAsJsonPrimitive().isString()) {
            throw new JsonParseException(aspectType + " aspect 'name' must be a string!");
        }
        if (!aspectObj.get("translation").isJsonPrimitive() || !aspectObj.get("translation").getAsJsonPrimitive().isString()) {
            throw new JsonParseException(aspectType + " aspect 'translation' must be a string!");
        }
    }

    /**
     * Validates the structure of a compound aspect object.
     * @param aspectObj The JSON object representing the compound aspect
     * @throws JsonParseException if the compound aspect object is invalid
     */
    private void validateCompoundAspectObject(JsonObject aspectObj) {
        // First validate the basic aspect structure
        validateAspectObject(aspectObj, "Compound");

        // Additional validation for compound-specific fields
        if (!aspectObj.has("components")) {
            throw new JsonParseException("Compound aspect missing required 'components' field!");
        }
        if (!aspectObj.get("components").isJsonArray()) {
            throw new JsonParseException("Compound aspect 'components' must be an array!");
        }

        JsonArray components = aspectObj.getAsJsonArray("components");
        if (components.size() != 2) {
            throw new JsonParseException("Compound aspect must have exactly 2 components!");
        }

        // Validate each component is a string
        for (JsonElement component : components) {
            if (!component.isJsonPrimitive() || !component.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("Compound aspect components must be strings!");
            }
        }
    }
}