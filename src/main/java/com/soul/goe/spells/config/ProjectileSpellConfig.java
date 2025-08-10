package com.soul.goe.spells.config;

public record ProjectileSpellConfig(
        String spellAffinity,
        float baseSpeed,
        float baseDamage,
        float baseRange,
        float affinityPowerBonus,
        float criticalDamageMultiplier,
        float criticalRangeMultiplier,
        float maxAccuracyOffset,
        ProjectileSoundConfig sounds,
        ProjectileParticleConfig particles
) {
    public static ProjectileSpellConfig fireball() {
        return new ProjectileSpellConfig(
                "fire",
                1.5F,
                24.0F,
                50.0F,
                1.0f,
                2.2f,
                1.5f,
                0.2f,
                ProjectileSoundConfig.fireball(),
                ProjectileParticleConfig.fireball()
        );
    }

    public static ProjectileSpellConfig firebolt() {
        return new ProjectileSpellConfig(
                "fire",
                1.2F,
                10.0F,
                120.0F,
                1.0f,
                2.0f,
                1.0f,
                0.15f,
                ProjectileSoundConfig.firebolt(),
                ProjectileParticleConfig.firebolt()
        );
    }

    public static ProjectileSpellConfig coneOfCold() {
        return new ProjectileSpellConfig(
                "frost",
                0.0F,
                28.0F,
                15.0F,
                1.0f,
                1.8f,
                1.4f,
                0.1f,
                ProjectileSoundConfig.coneOfCold(),
                ProjectileParticleConfig.coneOfCold()
        );
    }

    public static ProjectileSpellConfig lightningBolt() {
        return new ProjectileSpellConfig(
                "arc",
                0.0F,
                28.0F,
                25.0F,
                1.0f,
                2.0f,
                1.0f,
                0.0f,
                ProjectileSoundConfig.lightningBolt(),
                ProjectileParticleConfig.lightningBolt()
        );
    }

    public static ProjectileSpellConfig rayOfFrost() {
        return new ProjectileSpellConfig(
                "ice",
                1.8F,
                8.0F,
                120.0F,
                1.0f,
                1.5f,
                1.0f,
                0.12f,
                ProjectileSoundConfig.rayOfFrost(),
                ProjectileParticleConfig.rayOfFrost()
        );
    }

    public static ProjectileSpellConfig shockingGrasp() {
        return new ProjectileSpellConfig(
                "lightning",
                2.0F,
                8.0F,
                5.0F,
                1.0f,
                1.8f,
                1.0f,
                0.08f,
                ProjectileSoundConfig.shockingGrasp(),
                ProjectileParticleConfig.shockingGrasp()
        );
    }

    public static ProjectileSpellConfig magicMissile() {
        return new ProjectileSpellConfig(
                "void",
                0.8F,
                3.5F,
                120.0F,
                1.0f,
                1.5f,
                1.0f,
                0.0f,
                ProjectileSoundConfig.magicMissile(),
                ProjectileParticleConfig.magicMissile()
        );
    }
}