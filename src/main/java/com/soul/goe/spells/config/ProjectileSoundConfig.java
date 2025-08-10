package com.soul.goe.spells.config;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public record ProjectileSoundConfig(
        SoundEvent primarySound,
        SoundEvent criticalSound,
        float baseVolume,
        float basePitch,
        float maxVolume,
        float minPitch,
        float maxPitch,
        float criticalVolumeMultiplier,
        float criticalPitchMultiplier,
        float criticalSoundPitch
) implements SoundConfigInterface {

    public SoundEvent secondarySound() {
        return null;
    }

    public float primaryVolumeMultiplier() {
        return 1.0f;
    }

    public float secondaryVolumeMultiplier() {
        return 0.0f;
    }

    public float secondaryPitchOffset() {
        return 0.0f;
    }

    public static ProjectileSoundConfig fireball() {
        return new ProjectileSoundConfig(
                SoundEvents.FIRECHARGE_USE, SoundEvents.PLAYER_ATTACK_CRIT,
                1.2F, 0.9F, 2.0F, 0.6F, 2.0F,
                1.3f, 0.9f, 1.8F
        );
    }

    public static ProjectileSoundConfig firebolt() {
        return new ProjectileSoundConfig(
                SoundEvents.FIRECHARGE_USE, SoundEvents.PLAYER_ATTACK_CRIT,
                0.8F, 1.4F, 2.0F, 0.8F, 2.0F,
                1.1f, 1.2f, 1.8F
        );
    }

    public static ProjectileSoundConfig coneOfCold() {
        return new ProjectileSoundConfig(
                SoundEvents.POWDER_SNOW_PLACE, SoundEvents.GLASS_BREAK,
                1.5F, 0.8F, 2.0F, 0.5F, 2.0F,
                1.3f, 0.8f, 0.6F
        );
    }

    public static ProjectileSoundConfig lightningBolt() {
        return new ProjectileSoundConfig(
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundEvents.PLAYER_ATTACK_CRIT,
                1.8F, 1.0F, 3.0F, 0.7F, 2.0F,
                1.4f, 1.2f, 1.8F
        );
    }

    public static ProjectileSoundConfig rayOfFrost() {
        return new ProjectileSoundConfig(
                SoundEvents.SNOW_BREAK, SoundEvents.PLAYER_ATTACK_CRIT,
                0.7F, 1.3F, 2.0F, 0.8F, 2.0F,
                1.1f, 1.2f, 1.8F
        );
    }

    public static ProjectileSoundConfig shockingGrasp() {
        return new ProjectileSoundConfig(
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundEvents.PLAYER_ATTACK_CRIT,
                0.6F, 1.6F, 2.0F, 0.8F, 2.0F,
                1.1f, 1.2f, 1.8F
        );
    }

    public static ProjectileSoundConfig magicMissile() {
        return new ProjectileSoundConfig(
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundEvents.PLAYER_ATTACK_CRIT,
                0.9F, 1.3F, 1.5F, 0.8F, 2.0F,
                1.2f, 1.15f, 1.8F
        );
    }
}