package com.soul.goe.spells.config;

import net.minecraft.sounds.SoundEvent;

public interface SoundConfigInterface {
    SoundEvent primarySound();
    SoundEvent secondarySound();
    SoundEvent criticalSound();
    float baseVolume();
    float basePitch();
    float maxVolume();
    float minPitch();
    float maxPitch();
    float primaryVolumeMultiplier();
    float secondaryVolumeMultiplier();
    float secondaryPitchOffset();
    float criticalVolumeMultiplier();
    float criticalPitchMultiplier();
    float criticalSoundPitch();

    default float powerPitchModifier(float powerModifier) {
        return 0.8f + powerModifier * 0.2f;
    }
}