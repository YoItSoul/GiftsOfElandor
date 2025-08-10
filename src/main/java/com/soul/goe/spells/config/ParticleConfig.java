package com.soul.goe.spells.config;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.BiFunction;

public record ParticleConfig(
        SimpleParticleType primaryParticle,
        SimpleParticleType secondaryParticle,
        SimpleParticleType enchantParticle,
        SimpleParticleType flashParticle,
        SimpleParticleType affinityParticle,
        SimpleParticleType specialParticle,
        int baseParticleCount,
        double baseRadius,
        double radiusVariation,
        double heightVariation,
        double baseSpread,
        double baseVerticalSpeed,
        double baseSpeed,
        float secondaryParticleChance,
        int enchantCount,
        double enchantSpeed,
        int flashCount,
        float criticalParticleMultiplier,
        int affinityParticleCount,
        double affinityRadius,
        double affinityRadiusIncrement,
        BiFunction<Double, Integer, Double> affinityHeightFunction,
        int specialParticleCount,
        double specialRadius,
        double specialRadiusIncrement,
        double specialHeightIncrement,
        double specialSpeed
) {
    public static ParticleConfig flame() {
        return new ParticleConfig(
                ParticleTypes.FLAME,
                ParticleTypes.SMALL_FLAME,
                ParticleTypes.ENCHANT,
                ParticleTypes.FLASH,
                ParticleTypes.SOUL_FIRE_FLAME,
                ParticleTypes.LAVA,
                28, 1.3, 1.2, 1.4, 1.1, 0.1, 0.02, 0.4f,
                18, 0.1, 1, 1.6f,
                15, 2.5, 0.2, (angle, i) -> Math.sin(angle * 2) * 0.8,
                10, 0.6, 0.15, 0.2, 0.01
        );
    }

    public static ParticleConfig frost() {
        return new ParticleConfig(
                ParticleTypes.SNOWFLAKE,
                ParticleTypes.ITEM_SNOWBALL,
                ParticleTypes.ENCHANT,
                ParticleTypes.FLASH,
                ParticleTypes.SOUL_FIRE_FLAME,
                ParticleTypes.END_ROD,
                30, 1.5, 1.0, 1.5, 1.2, 0.1, 0.02, 0.4f,
                15, 0.1, 1, 1.4f,
                12, 2.0, 0.2, (angle, i) -> Math.sin(angle * 2) * 0.6,
                8, 0.5, 0.2, 0.3, 0.02
        );
    }

    public static ParticleConfig arc() {
        return new ParticleConfig(
                ParticleTypes.ELECTRIC_SPARK,
                ParticleTypes.END_ROD,
                ParticleTypes.ENCHANT,
                ParticleTypes.FLASH,
                ParticleTypes.SOUL_FIRE_FLAME,
                null,
                25, 1.2, 0.8, 1.2, 1.0, 0.1, 0.03, 0.3f,
                12, 0.08, 1, 1.4f,
                8, 2.5, 0.2, (angle, i) -> i * 0.3,
                0, 0, 0, 0, 0
        );
    }

    public static ParticleConfig dragon() {
        return new ParticleConfig(
                ParticleTypes.DRAGON_BREATH,
                ParticleTypes.END_ROD,
                ParticleTypes.ENCHANT,
                ParticleTypes.FLASH,
                ParticleTypes.PORTAL,
                null,
                40, 2.0, 1.5, 2.0, 1.5, 0.15, 0.03, 0.5f,
                25, 0.12, 2, 2.0f,
                20, 3.0, 0.15, (angle, i) -> Math.sin(angle * 3) * 1.5,
                0, 0, 0, 0, 0
        );
    }
}
