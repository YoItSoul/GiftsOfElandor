package com.soul.goe.spells.types.self;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.util.SpellEffect;

public class LevitateSpell extends SpellEffect {
    private static final int DURATION_TICKS = 200;
    private static final float LEVITATE_SPEED = 0.02F;
    private static final float MAX_HEIGHT = 20.0F;
    private static final int CAST_PARTICLE_COUNT = 25;
    private static final double CAST_PARTICLE_SPREAD = 0.8;
    private static final float SOUND_VOLUME = 0.8F;
    private static final float SOUND_PITCH = 1.2F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        applyLevitateEffect(caster);
        playCastEffects(level, caster);
    }

    private void applyLevitateEffect(Player player) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.LEVITATION,
                DURATION_TICKS,
                0,
                false,
                true,
                true
        ));

        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.SLOW_FALLING,
                DURATION_TICKS * 2,
                0,
                false,
                false,
                false
        ));

        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0) {
            player.setDeltaMovement(motion.x, 0, motion.z);
        }
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 playerPos = caster.position();

            for (int i = 0; i < CAST_PARTICLE_COUNT; i++) {
                double angle = 2 * Math.PI * i / CAST_PARTICLE_COUNT;
                double radius = 1.5 + Math.random() * 0.5;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;
                double y = playerPos.y + Math.random() * 2.5;

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        x, y, z, 1,
                        0, 0.05, 0, 0.02);

                if (Math.random() < 0.4) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANT,
                            x, y, z, 1,
                            CAST_PARTICLE_SPREAD * 0.3, CAST_PARTICLE_SPREAD * 0.3, CAST_PARTICLE_SPREAD * 0.3,
                            0.01);
                }
            }

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    playerPos.x, playerPos.y + 1, playerPos.z, 15,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.1);
        }
    }

    @Override
    public String getName() {
        return "Levitate";
    }
}