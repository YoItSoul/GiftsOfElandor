package com.soul.goe.util.wands;

// Final calculated wand stats record - simplified to 5 core stats
public record WandStats(
        float power,        // PWR - affects damage and velocity of spells
        float stability,    // STB - affects if spells occur/backfire and accuracy of spell effects
        float durability,   // DUR - affects how much durability damage the wand takes and spell max range
        float critical,     // CRIT - critical hit chance
        String affinity     // AFF - determines what spell types can be empowered
) {}