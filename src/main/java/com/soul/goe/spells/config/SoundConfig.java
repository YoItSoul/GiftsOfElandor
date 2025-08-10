package com.soul.goe.spells.config;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public record SoundConfig(
        SoundEvent primarySound,
        SoundEvent secondarySound,
        SoundEvent criticalSound,
        float baseVolume,
        float basePitch,
        float maxVolume,
        float minPitch,
        float maxPitch,
        float primaryVolumeMultiplier,
        float secondaryVolumeMultiplier,
        float secondaryPitchOffset,
        float criticalVolumeMultiplier,
        float criticalPitchMultiplier,
        float criticalSoundPitch
) implements SoundConfigInterface {

    public static SoundConfig flame() {
        return new SoundConfig(
                SoundEvents.FIRE_EXTINGUISH,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundEvents.PLAYER_ATTACK_CRIT,
                0.9F, 1.3F, 1.5F, 0.8F, 2.0F,
                0.6F, 0.5F, 0.3F,
                1.3f, 1.1f, 2.0F
        );
    }

    public static SoundConfig frost() {
        return new SoundConfig(
                SoundEvents.SNOW_PLACE,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundEvents.PLAYER_ATTACK_CRIT,
                1.0F, 1.6F, 1.5F, 1.0F, 2.0F,
                1.0F, 0.6F, 0.4F,
                1.2f, 1.1f, 2.2F
        );
    }

    public static SoundConfig arc() {
        return new SoundConfig(
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundEvents.PLAYER_ATTACK_CRIT,
                0.8F, 1.4F, 1.5F, 0.8F, 2.0F,
                0.4F, 0.5F, 0.6F,
                1.2f, 1.3f, 2.0F
        );
    }

    public static SoundConfig dragon() {
        return new SoundConfig(
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundEvents.ENDER_DRAGON_DEATH,
                1.2F, 0.8F, 2.0F, 0.6F, 2.0F,
                0.5F, 0.4F, 0.8F,
                1.4f, 0.9f, 1.5F
        );
    }
}