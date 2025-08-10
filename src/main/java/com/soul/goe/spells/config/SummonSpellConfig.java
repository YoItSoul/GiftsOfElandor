package com.soul.goe.spells.config;

public record SummonSpellConfig(
        String spellAffinity,
        float baseDamage,
        int baseDuration,
        float affinityPowerBonus,
        float criticalDamageMultiplier,
        float criticalDurationMultiplier,
        float maxPositionOffset,
        SoundConfig sounds,
        ParticleConfig particles
) {
    public static SummonSpellConfig flame() {
        return new SummonSpellConfig(
                "fire",
                1.0F,
                2400,
                1.0f,
                1.8f,
                1.4f,
                2.5f,
                SoundConfig.flame(),
                ParticleConfig.flame()
        );
    }

    public static SummonSpellConfig frost() {
        return new SummonSpellConfig(
                "frost",
                1.0F,
                2400,
                1.0f,
                1.6f,
                1.3f,
                2.0f,
                SoundConfig.frost(),
                ParticleConfig.frost()
        );
    }

    public static SummonSpellConfig arc() {
        return new SummonSpellConfig(
                "arc",
                1.0F,
                2400,
                1.0f,
                1.5f,
                1.3f,
                2.0f,
                SoundConfig.arc(),
                ParticleConfig.arc()
        );
    }

    public static SummonSpellConfig dragon() {
        return new SummonSpellConfig(
                "void",
                1.8F,
                3000,
                1.0f,
                2.5f,
                1.8f,
                3.0f,
                SoundConfig.dragon(),
                ParticleConfig.dragon()
        );
    }
}