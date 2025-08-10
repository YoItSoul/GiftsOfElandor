package com.soul.goe.spells.types.self;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.util.SpellEffect;

public class LungeSpell extends SpellEffect {
    private static final float GROUNDED_LUNGE_SPEED = 3.5F;
    private static final float ELYTRA_BOOST_SPEED = 4.0F;
    private static final int CAST_PARTICLE_COUNT = 30;
    private static final double CAST_PARTICLE_SPREAD = 0.8;
    private static final float SOUND_VOLUME = 1.2F;
    private static final float SOUND_PITCH = 0.8F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 lookDirection = caster.getLookAngle();
        boolean isUsingElytra = caster.isFallFlying();

        Vec3 lungeVelocity;

        if (isUsingElytra) {
            lungeVelocity = lookDirection.scale(ELYTRA_BOOST_SPEED);
        } else {
            double minVertical = 0.5;
            double verticalComponent = Math.max(minVertical, Math.abs(lookDirection.y) + 0.3);

            if (lookDirection.y < 0) {
                verticalComponent = minVertical;
            }

            lungeVelocity = new Vec3(
                    lookDirection.x * GROUNDED_LUNGE_SPEED,
                    verticalComponent * GROUNDED_LUNGE_SPEED,
                    lookDirection.z * GROUNDED_LUNGE_SPEED
            );
        }

        if (!isUsingElytra && caster.onGround()) {
            caster.setOnGround(false);
            caster.fallDistance = 0;
        }

        caster.setDeltaMovement(lungeVelocity);
        caster.hasImpulse = true;
        caster.hurtMarked = true;

        playCastEffects(level, caster, isUsingElytra);
    }

    private void playCastEffects(Level level, Player caster, boolean isUsingElytra) {
        if (isUsingElytra) {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS,
                    SOUND_VOLUME, SOUND_PITCH + 0.3F);
        } else {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS,
                    SOUND_VOLUME * 0.8F, SOUND_PITCH + 0.5F);
        }

        if (level instanceof ServerLevel serverLevel) {
            Vec3 playerPos = caster.position();
            Vec3 lookDir = caster.getLookAngle();

            if (isUsingElytra) {
                for (int i = 0; i < CAST_PARTICLE_COUNT; i++) {
                    Vec3 backwardOffset = lookDir.scale(-1.0 - Math.random() * 2.0);
                    double x = playerPos.x + backwardOffset.x + (Math.random() - 0.5) * 1.5;
                    double y = playerPos.y + backwardOffset.y + (Math.random() - 0.5) * 1.5;
                    double z = playerPos.z + backwardOffset.z + (Math.random() - 0.5) * 1.5;

                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            x, y, z, 1,
                            0.2, 0.2, 0.2, 0.1);

                    if (Math.random() < 0.3) {
                        serverLevel.sendParticles(ParticleTypes.FIREWORK,
                                x, y, z, 1,
                                0.3, 0.3, 0.3, 0.05);
                    }
                }
            } else {
                for (int i = 0; i < CAST_PARTICLE_COUNT / 2; i++) {
                    double angle = 2 * Math.PI * i / (CAST_PARTICLE_COUNT / 2);
                    double radius = 0.8 + Math.random() * 0.4;
                    double x = playerPos.x + Math.cos(angle) * radius;
                    double z = playerPos.z + Math.sin(angle) * radius;
                    double y = playerPos.y + Math.random() * 0.5;

                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            x, y, z, 1,
                            0, 0.1, 0, 0.02);

                    if (Math.random() < 0.4) {
                        serverLevel.sendParticles(ParticleTypes.CRIT,
                                x, y + 0.5, z, 1,
                                0.1, 0.1, 0.1, 0.05);
                    }
                }
            }

            Vec3 trailStart = playerPos.add(lookDir.scale(-0.5));
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    trailStart.x, trailStart.y + 1, trailStart.z, 10,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.1);
        }
    }

    @Override
    public String getName() {
        return "Lunge";
    }
}