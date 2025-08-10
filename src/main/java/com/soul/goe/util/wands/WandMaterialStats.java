package com.soul.goe.util.wands;

// Material stats record - all stats are multipliers (1.0 = baseline)
public record WandMaterialStats(
        float power,        // Spell damage and velocity multiplier
        float stability,    // Reduces spell failure chance and improves accuracy
        float durability,   // How much durability this part contributes and range multiplier
        float critical,     // Critical hit chance multiplier
        String affinity     // Elemental affinity (fire, ice, lightning, nature, dark, void, air, neutral)
) {}