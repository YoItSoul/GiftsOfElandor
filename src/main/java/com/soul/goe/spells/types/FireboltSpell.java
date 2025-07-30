// FireboltSpell.java
package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.FireboltEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class FireboltSpell extends SpellEffect {
    private static final float FIREBOLT_SPEED = 1.2F; // Faster projectile
    private static final float FIREBOLT_DAMAGE_MIN = 1.0F; // 1d10 damage
    private static final float FIREBOLT_DAMAGE_MAX = 10.0F;
    private static final int FIRE_DURATION = 40; // Shorter fire duration (4 seconds)
    private static final int CAST_PARTICLE_COUNT = 10;
    private static final double CAST_PARTICLE_SPREAD = 0.2;
    private static final float SOUND_VOLUME = 0.8F;
    private static final float SOUND_PITCH = 1.4F; // Higher pitch sound
    private static final int MAX_RANGE = 120; // 120 feet range (24 blocks)

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 lookDirection = caster.getLookAngle();
        Vec3 startPos = caster.getEyePosition().add(lookDirection.scale(0.8));

        FireboltEntity firebolt = new FireboltEntity(ModEntityRegistry.FIREBOLT_PROJECTILE.get(), level, caster);
        firebolt.setPos(startPos.x, startPos.y, startPos.z);
        firebolt.setDeltaMovement(lookDirection.scale(FIREBOLT_SPEED));
        firebolt.setDamageRange(FIREBOLT_DAMAGE_MIN, FIREBOLT_DAMAGE_MAX);
        firebolt.setFireDuration(FIRE_DURATION);
        firebolt.setMaxRange(MAX_RANGE);

        level.addFreshEntity(firebolt);
        playCastEffects(level, caster);
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = caster.getEyePosition();
            Vec3 lookDir = caster.getLookAngle();
            Vec3 particlePos = eyePos.add(lookDir.scale(0.5));

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.02);

            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT / 3,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.01);
        }
    }

    @Override
    public String getName() {
        return "Fire Bolt"; // D&D uses "Fire Bolt" with space
    }
}