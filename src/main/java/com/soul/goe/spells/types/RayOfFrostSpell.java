package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.RayOfFrostEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class RayOfFrostSpell extends SpellEffect {
    private static final float RAY_SPEED = 1.8F;
    private static final float RAY_DAMAGE_MIN = 1.0F;
    private static final float RAY_DAMAGE_MAX = 8.0F;
    private static final int SLOW_DURATION = 20;
    private static final int CAST_PARTICLE_COUNT = 15;
    private static final double CAST_PARTICLE_SPREAD = 0.3;
    private static final float SOUND_VOLUME = 0.7F;
    private static final float SOUND_PITCH = 1.3F;
    private static final int MAX_RANGE = 120;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 lookDirection = caster.getLookAngle();
        Vec3 startPos = caster.getEyePosition().add(lookDirection.scale(0.8));

        RayOfFrostEntity rayOfFrost = new RayOfFrostEntity(ModEntityRegistry.RAY_OF_FROST_PROJECTILE.get(), level, caster);
        rayOfFrost.setPos(startPos.x, startPos.y, startPos.z);
        rayOfFrost.setDeltaMovement(lookDirection.scale(RAY_SPEED));
        rayOfFrost.setDamageRange(RAY_DAMAGE_MIN, RAY_DAMAGE_MAX);
        rayOfFrost.setSlowDuration(SLOW_DURATION);
        rayOfFrost.setMaxRange(MAX_RANGE);

        level.addFreshEntity(rayOfFrost);
        playCastEffects(level, caster);
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.SNOW_BREAK, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = caster.getEyePosition();
            Vec3 lookDir = caster.getLookAngle();
            Vec3 particlePos = eyePos.add(lookDir.scale(0.5));

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.02);

            serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT / 3,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.01);
        }
    }

    @Override
    public String getName() {
        return "Ray of Frost";
    }
}