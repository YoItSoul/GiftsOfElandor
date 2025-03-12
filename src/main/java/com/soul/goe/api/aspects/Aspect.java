package com.soul.goe.api.aspects;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Set;
import java.util.stream.Collectors;

public class Aspect {
    private final String tag;
    private final int color;
    private final AspectList components;
    private final String chatColor;
    private final String name;
    private final int blend;

    /**
     * Create a new aspect with the given name, color, and components
     *
     * @param tag Unique identifier for this aspect
     * @param color RGB color value of the aspect
     * @param components The component aspects of this aspect, if any
     * @param name Localization key for this aspect
     */
    public Aspect(String tag, int color, AspectList components, String name) {
        this.tag = tag;
        this.color = color;
        this.components = components;
        this.name = name;
        this.chatColor = "§f"; // Default to white
        this.blend = color;
    }

    /**
     * Create a new aspect with the given name, color, and components
     *
     * @param tag Unique identifier for this aspect
     * @param color RGB color value of the aspect
     * @param components The component aspects of this aspect, if any
     */
    public Aspect(String tag, int color, AspectList components) {
        this(tag, color, components, tag);
    }

    /**
     * Get the String ID of this aspect
     */
    public String getTag() {
        return tag;
    }

    /**
     * Get the RGB color of this aspect
     */
    public int getColor() {
        return color;
    }

    /**
     * Get the component aspects of this aspect
     */
    public AspectList getComponents() {
        return components;
    }

    /**
     * Returns true if this aspect is a primal aspect (has no components)
     */
    public boolean isPrimal() {
        return components == null || components.aspects.isEmpty();
    }

    /**
     * Get a formatted chat Component for this aspect
     */
    public MutableComponent getName() {
        return Component.translatable(name);
    }


    /**
     * Checks if this aspect has the same component combination as the provided set.
     * @param otherComponents the component set to compare against
     * @return true if the components match exactly, false otherwise
     */
    public boolean hasSameComponents(Set<String> otherComponents) {
        if (isPrimal() || components == null) {
            return false;
        }

        Set<String> thisComponents = components.getAspects().keySet().stream()
                .map(Aspect::getTag)
                .collect(Collectors.toSet());

        return thisComponents.equals(otherComponents);
    }


    /**
     * Get a formatted chat string for this aspect
     */
    public String getChatColor() {
        return chatColor;
    }

    /**
     * Get the blended RGB color for display
     */
    public int getBlend() {
        return blend;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Aspect aspect) {
            return aspect.tag.equals(tag);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }
}