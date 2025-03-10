package com.soul.goe.api.aspects;

import com.soul.goe.api.aspects.Aspect;

import java.util.HashMap;
import java.util.Map;

public class AspectList {
    protected final Map<Aspect, Integer> aspects;

    public AspectList() {
        this.aspects = new HashMap<>();
    }

    /**
     * Add an aspect with an amount to the list
     * @param aspect The aspect to add
     * @param amount The amount to add
     * @return This AspectList for chaining
     */
    public AspectList add(Aspect aspect, int amount) {
        if (aspect != null && amount > 0) {
            aspects.merge(aspect, amount, Integer::sum);
        }
        return this;
    }

    /**
     * Add another AspectList to this one
     * @param other The AspectList to add
     * @return This AspectList for chaining
     */
    public AspectList add(AspectList other) {
        if (other != null) {
            other.aspects.forEach(this::add);
        }
        return this;
    }

    /**
     * Get the amount of a specific aspect in the list
     * @param aspect The aspect to check
     * @return The amount of the aspect, or 0 if not present
     */
    public int getAmount(Aspect aspect) {
        return aspects.getOrDefault(aspect, 0);
    }

    /**
     * Remove an aspect from the list
     * @param aspect The aspect to remove
     */
    public void remove(Aspect aspect) {
        aspects.remove(aspect);
    }

    /**
     * Clear all aspects from the list
     */
    public void clear() {
        aspects.clear();
    }

    /**
     * Check if the list contains an aspect
     * @param aspect The aspect to check for
     * @return true if the aspect is present
     */
    public boolean contains(Aspect aspect) {
        return aspects.containsKey(aspect);
    }

    /**
     * Get the number of different aspects in the list
     * @return The size of the aspect list
     */
    public int size() {
        return aspects.size();
    }

    /**
     * Check if the list is empty
     * @return true if the list contains no aspects
     */
    public boolean isEmpty() {
        return aspects.isEmpty();
    }

    /**
     * Get a copy of the aspects map
     * @return A new Map containing all aspects and their amounts
     */
    public Map<Aspect, Integer> getAspects() {
        return new HashMap<>(aspects);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        aspects.forEach((aspect, amount) ->
                sb.append(aspect.getTag()).append(": ").append(amount).append(", "));
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2); // Remove last ", "
        }
        return sb.toString();
    }
}

