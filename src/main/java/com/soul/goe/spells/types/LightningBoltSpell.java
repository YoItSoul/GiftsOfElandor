package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.LightningBoltEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class LightningBoltSpell extends SpellEffect {
    private static final float LIGHTNING_DAMAGE = 28.0F;
    private static final int LINE_RANGE = 25;
    private static final float LINE_WIDTH = 5.0F;
    private static final int CAST_PARTICLE_COUNT = 40;
    private static final double CAST_PARTICLE_SPREAD = 0.8;
    private static final float SOUND_VOLUME = 1.8F;
    private static final float SOUND_PITCH = 1.0F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 lookDirection = caster.getLookAngle();
        Vec3 startPos = caster.getEyePosition().add(lookDirection.scale(0.5));

        LightningBoltEntity lightningBolt = new LightningBoltEntity(ModEntityRegistry.LIGHTNING_BOLT_PROJECTILE.get(), level, caster);
        lightningBolt.setPos(startPos.x, startPos.y, startPos.z);
        lightningBolt.setDamage(LIGHTNING_DAMAGE);
        lightningBolt.setLineRange(LINE_RANGE);
        lightningBolt.setLineWidth(LINE_WIDTH);

        level.addFreshEntity(lightningBolt);
        playCastEffects(level, caster);
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = caster.getEyePosition();
            Vec3 lookDir = caster.getLookAngle();

            for (int i = 0; i < CAST_PARTICLE_COUNT; i++) {
                double distance = Math.random() * 5.0;
                Vec3 particlePos = eyePos.add(lookDir.scale(distance));

                double offsetX = (Math.random() - 0.5) * LINE_WIDTH;
                double offsetY = (Math.random() - 0.5) * LINE_WIDTH;
                double offsetZ = (Math.random() - 0.5) * LINE_WIDTH;

                particlePos = particlePos.add(offsetX, offsetY, offsetZ);

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                        0.2);

                if (Math.random() < 0.3) {
                    serverLevel.sendParticles(ParticleTypes.FLASH,
                            particlePos.x, particlePos.y, particlePos.z, 1,
                            0.1, 0.1, 0.1, 0);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Lightning Bolt";
    }
}