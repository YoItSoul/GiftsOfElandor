package com.soul.goe.spells.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SpellData {
    private final String spellId;
    private final SpellEffect effect;
    private final int castTime;
    private final SpellCost cost;

    public SpellData(String spellId, SpellEffect effect, int castTime, SpellCost cost) {
        this.spellId = spellId;
        this.effect = effect;
        this.castTime = castTime;
        this.cost = cost != null ? cost : SpellCost.none();
    }

    public SpellData(String spellId, SpellEffect effect, int castTime) {
        this(spellId, effect, castTime, SpellCost.none());
    }

    public String getSpellId() { return spellId; }
    public SpellEffect getEffect() { return effect; }
    public int getCastTime() { return castTime; }
    public SpellCost getCost() { return cost; }

    public boolean canCast(Player player) {
        return cost.isEmpty() || cost.canAfford(player);
    }

    public void cast(Level level, Player player) {
        if (canCast(player)) {
            if (!cost.isEmpty()) {
                cost.consume(player);
            }
            effect.cast(level, player);
        }
    }
}