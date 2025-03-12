package com.soul.goe.registry;

import com.soul.goe.Config;
import com.soul.goe.Goe;
import com.soul.goe.api.aspects.Aspect;
import com.soul.goe.api.aspects.AspectList;

import java.util.*;

public class AspectRegistry {
    private static final boolean DEBUG_ASPECTS = true;
    private final Map<String, Aspect> aspects = new HashMap<>();

    public void registerAspects() {
        aspects.clear();

        loadPrimalAspects();
        loadBasicCompoundAspects();
        loadAdvancedCompoundAspects();

        if (DEBUG_ASPECTS) {
            logAspectDebugInfo();
        }
    }

    private void loadPrimalAspects() {
        Goe.LOGGER.debug("Starting to load primal aspects...");
        List<? extends String> primalAspects = Config.PRIMAL_ASPECTS.get();
        Goe.LOGGER.debug("Primal aspects from config: " + primalAspects);

        if (primalAspects.isEmpty()) {
            Goe.LOGGER.warn("No primal aspects found in configuration!");
            return;
        }

        primalAspects.forEach(aspectEntry -> {
            try {
                Goe.LOGGER.debug("Processing primal aspect entry: " + aspectEntry);
                String[] parts = aspectEntry.split(":");
                if (parts.length == 3) {
                    String tag = parts[0];
                    String displayName = parts[1];
                    int color = parseHexColor(parts[2]);
                    aspects.put(tag, new Aspect(tag, color, null, displayName));
                    Goe.LOGGER.debug("Successfully registered primal aspect: " + tag);
                } else {
                    Goe.LOGGER.warn("Invalid primal aspect format: " + aspectEntry);
                }
            } catch (Exception e) {
                Goe.LOGGER.error("Failed to load primal aspect: " + aspectEntry, e);
            }
        });
    }

    private void loadBasicCompoundAspects() {
        Config.BASIC_COMPOUND_ASPECTS.get().forEach(aspectEntry -> {
            try {
                String[] mainParts = aspectEntry.split("->");
                if (mainParts.length == 2) {
                    String[] nameData = mainParts[0].split(":");
                    if (nameData.length == 3) {
                        String tag = nameData[0];
                        String displayName = nameData[1];
                        int color = parseHexColor(nameData[2]);
                        String[] componentTags = mainParts[1].split(",");

                        if (componentTags.length == 2) {
                            AspectList components = new AspectList();
                            Aspect component1 = aspects.get(componentTags[0]);
                            Aspect component2 = aspects.get(componentTags[1]);

                            if (component1 != null && component2 != null) {
                                components.add(component1, 1);
                                components.add(component2, 1);
                                aspects.put(tag, new Aspect(tag, color, components, displayName));
                            } else {
                                Goe.LOGGER.error("Could not find component aspects for: " + aspectEntry);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Goe.LOGGER.error("Failed to load basic compound aspect: " + aspectEntry, e);
            }
        });
    }

    private void loadAdvancedCompoundAspects() {
        Config.ADVANCED_COMPOUND_ASPECTS.get().forEach(aspectEntry -> {
            try {
                String[] mainParts = aspectEntry.split("->");
                if (mainParts.length == 2) {
                    String[] nameData = mainParts[0].split(":");
                    if (nameData.length == 3) {
                        String tag = nameData[0];
                        String displayName = nameData[1];
                        int color = parseHexColor(nameData[2]);
                        String[] componentTags = mainParts[1].split(",");

                        if (componentTags.length == 2) {
                            AspectList components = new AspectList();
                            Aspect component1 = aspects.get(componentTags[0]);
                            Aspect component2 = aspects.get(componentTags[1]);

                            if (component1 != null && component2 != null) {
                                components.add(component1, 1);
                                components.add(component2, 1);
                                aspects.put(tag, new Aspect(tag, color, components, displayName));
                            } else {
                                Goe.LOGGER.error("Could not find component aspects for: " + aspectEntry);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Goe.LOGGER.error("Failed to load advanced compound aspect: " + aspectEntry, e);
            }
        });
    }

    private int parseHexColor(String hexColor) {
        try {
            // Remove the '#' if present
            String colorStr = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
            // Parse the hex string to an integer
            return Integer.parseInt(colorStr, 16);
        } catch (NumberFormatException e) {
            Goe.LOGGER.error("Invalid color format: " + hexColor);
            return 0xFFFFFF; // Default to white if parsing fails
        }
    }

    private void logAspectDebugInfo() {
        Goe.LOGGER.debug("Registered Aspects:");
        aspects.forEach((tag, aspect) -> {
            StringBuilder info = new StringBuilder(String.format(" - %s", tag));
            if (!aspect.isPrimal()) {
                info.append(" (Components: ");
                aspect.getComponents().getAspects().forEach((componentAspect, amount) ->
                        info.append(componentAspect.getTag()).append(" x").append(amount).append(", ")
                );
                info.setLength(info.length() - 2); // Remove last ", "
                info.append(")");
            }
            info.append(" [").append(aspect.getName().getString()).append("]");
            Goe.LOGGER.debug(info.toString());
        });
    }

    public Map<String, Aspect> getAspects() {
        return Collections.unmodifiableMap(aspects);
    }

    public Aspect getAspect(String tag) {
        return aspects.get(tag);
    }
}