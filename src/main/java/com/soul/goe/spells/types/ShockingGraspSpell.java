package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.ShockingGraspEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class ShockingGraspSpell extends SpellEffect {
    private static final float SHOCK_SPEED = 2.0F;
    private static final float SHOCK_DAMAGE_MIN = 1.0F;
    private static final float SHOCK_DAMAGE_MAX = 8.0F;
    private static final int CAST_PARTICLE_COUNT = 12;
    private static final double CAST_PARTICLE_SPREAD = 0.4;
    private static final float SOUND_VOLUME = 0.6F;
    private static final float SOUND_PITCH = 1.6F;
    private static final int MAX_RANGE = 5;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 lookDirection = caster.getLookAngle();
        Vec3 startPos = caster.getEyePosition().add(lookDirection.scale(0.8));

        ShockingGraspEntity shockingGrasp = new ShockingGraspEntity(ModEntityRegistry.SHOCKING_GRASP_PROJECTILE.get(), level, caster);
        shockingGrasp.setPos(startPos.x, startPos.y, startPos.z);
        shockingGrasp.setDeltaMovement(lookDirection.scale(SHOCK_SPEED));
        shockingGrasp.setDamageRange(SHOCK_DAMAGE_MIN, SHOCK_DAMAGE_MAX);
        shockingGrasp.setMaxRange(MAX_RANGE);

        level.addFreshEntity(shockingGrasp);
        playCastEffects(level, caster);
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = caster.getEyePosition();
            Vec3 lookDir = caster.getLookAngle();
            Vec3 particlePos = eyePos.add(lookDir.scale(0.5));

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.1);

            serverLevel.sendParticles(ParticleTypes.CRIT,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT / 2,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.05);
        }
    }

    @Override
    public String getName() {
        return "Shocking Grasp";
    }
}