package com.soul.goe.spells.types;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class FlySpell extends SpellEffect {
    private static final int DURATION_TICKS = 12000;
    private static final float FLY_SPEED = 0.1F;
    private static final int CAST_PARTICLE_COUNT = 40;
    private static final double CAST_PARTICLE_SPREAD = 1.2;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.5F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        applyFlyEffect(caster);
        playCastEffects(level, caster);
    }

    private void applyFlyEffect(Player player) {
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();

        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.SLOW_FALLING,
                DURATION_TICKS + 200,
                0,
                false,
                false,
                false
        ));

        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                DURATION_TICKS,
                1,
                false,
                true,
                true
        ));

        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0) {
            player.setDeltaMovement(motion.x, 0, motion.z);
        }

        createRemovalTask(player, DURATION_TICKS);
    }

    private void createRemovalTask(Player player, int duration) {
        player.level().scheduleTick(player.blockPosition(), net.minecraft.world.level.block.Blocks.AIR, duration);

        new Thread(() -> {
            try {
                Thread.sleep(duration * 50L);
                if (player.isAlive()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 playerPos = caster.position();

            for (int i = 0; i < CAST_PARTICLE_COUNT; i++) {
                double angle = 2 * Math.PI * i / CAST_PARTICLE_COUNT;
                double radius = 2.0 + Math.random() * 1.0;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + Math.random() * 3.0;

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        x, y, z, 1,
                        0, 0.1, 0, 0.05);

                if (Math.random() < 0.6) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            x, y, z, 1,
                            CAST_PARTICLE_SPREAD * 0.2, CAST_PARTICLE_SPREAD * 0.2, CAST_PARTICLE_SPREAD * 0.2,
                            0.02);
                }
            }

            for (int i = 0; i < 8; i++) {
                double wingX = playerPos.x + (Math.random() - 0.5) * 4;
                double wingY = playerPos.y + 1 + Math.random() * 2;
                double wingZ = playerPos.z + (Math.random() - 0.5) * 4;

                serverLevel.sendParticles(ParticleTypes.FIREWORK,
                        wingX, wingY, wingZ, 3,
                        0.5, 0.5, 0.5, 0.1);
            }

            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    playerPos.x, playerPos.y + 1, playerPos.z, 30,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.2);
        }
    }

    @Override
    public String getName() {
        return "Fly";
    }
}