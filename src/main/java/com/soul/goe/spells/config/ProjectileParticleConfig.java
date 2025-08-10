package com.soul.goe.spells.config;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.BiFunction;

public record ProjectileParticleConfig(
        SimpleParticleType primaryParticle,
        SimpleParticleType secondaryParticle,
        SimpleParticleType flashParticle,
        SimpleParticleType affinityParticle,
        int baseParticleCount,
        double baseDistance,
        double baseSpread,
        double baseSpeed,
        float criticalParticleMultiplier,
        int affinityParticleCount,
        double affinityRadius,
        double affinityRadiusIncrement,
        BiFunction<Double, Integer, Double> affinityHeightFunction
) {
    public static ProjectileParticleConfig fireball() {
        return new ProjectileParticleConfig(
                ParticleTypes.FLAME, ParticleTypes.LAVA, ParticleTypes.FLASH, ParticleTypes.SOUL_FIRE_FLAME,
                20, 0.5, 0.5, 0.05, 1.8f,
                8, 1.0, 0.15, (angle, i) -> Math.sin(angle * 2) * 0.3
        );
    }

    public static ProjectileParticleConfig firebolt() {
        return new ProjectileParticleConfig(
                ParticleTypes.FLAME, ParticleTypes.SMOKE, ParticleTypes.FLASH, ParticleTypes.SOUL_FIRE_FLAME,
                10, 0.5, 0.2, 0.02, 1.5f,
                5, 0.8, 0.1, (angle, i) -> 0.0
        );
    }

    public static ProjectileParticleConfig coneOfCold() {
        return new ProjectileParticleConfig(
                ParticleTypes.SNOWFLAKE, ParticleTypes.ITEM_SNOWBALL, ParticleTypes.FLASH, ParticleTypes.SOUL_FIRE_FLAME,
                30, 0.0, 1.0, 0.05, 1.6f,
                12, 1.5, 0.2, (angle, i) -> Math.sin(angle * 2) * 0.5
        );
    }

    public static ProjectileParticleConfig lightningBolt() {
        return new ProjectileParticleConfig(
                ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FLASH, ParticleTypes.FLASH, ParticleTypes.SOUL_FIRE_FLAME,
                40, 0.0, 0.8, 0.2, 2.0f,
                10, 2.0, 0.3, (angle, i) -> Math.sin(angle * 3) * 1.0
        );
    }

    public static ProjectileParticleConfig rayOfFrost() {
        return new ProjectileParticleConfig(
                ParticleTypes.SNOWFLAKE, ParticleTypes.ITEM_SNOWBALL, ParticleTypes.FLASH, ParticleTypes.ITEM_SNOWBALL,
                15, 0.5, 0.3, 0.02, 1.5f,
                6, 1.0, 0.15, (angle, i) -> 0.0
        );
    }

    public static ProjectileParticleConfig shockingGrasp() {
        return new ProjectileParticleConfig(
                ParticleTypes.ELECTRIC_SPARK, ParticleTypes.CRIT, ParticleTypes.FLASH, ParticleTypes.FLASH,
                12, 0.5, 0.4, 0.1, 1.5f,
                3, 0.8, 0.2, (angle, i) -> 0.0
        );
    }

    public static ProjectileParticleConfig magicMissile() {
        return new ProjectileParticleConfig(
                ParticleTypes.END_ROD, ParticleTypes.ENCHANT, ParticleTypes.FLASH, ParticleTypes.SOUL_FIRE_FLAME,
                20, 0.5, 0.8, 0.1, 1.3f,
                8, 1.5, 0.15, (angle, i) -> Math.sin(angle * 2) * 0.3
        );
    }
}