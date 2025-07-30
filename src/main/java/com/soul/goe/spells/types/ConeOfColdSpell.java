package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.ConeOfColdEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class ConeOfColdSpell extends SpellEffect {
    private static final float CONE_DAMAGE = 28.0F;
    private static final int CONE_RANGE = 15;
    private static final float CONE_WIDTH = 60.0F;
    private static final int CAST_PARTICLE_COUNT = 30;
    private static final double CAST_PARTICLE_SPREAD = 1.0;
    private static final float SOUND_VOLUME = 1.5F;
    private static final float SOUND_PITCH = 0.8F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 lookDirection = caster.getLookAngle();
        Vec3 startPos = caster.getEyePosition().add(lookDirection.scale(0.5));

        ConeOfColdEntity coneOfCold = new ConeOfColdEntity(ModEntityRegistry.CONE_OF_COLD_PROJECTILE.get(), level, caster);
        coneOfCold.setPos(startPos.x, startPos.y, startPos.z);
        coneOfCold.setDamage(CONE_DAMAGE);
        coneOfCold.setConeRange(CONE_RANGE);
        coneOfCold.setConeWidth(CONE_WIDTH);

        level.addFreshEntity(coneOfCold);
        playCastEffects(level, caster);
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.POWDER_SNOW_PLACE, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = caster.getEyePosition();
            Vec3 lookDir = caster.getLookAngle();

            for (int i = 0; i < CAST_PARTICLE_COUNT; i++) {
                double angle = (Math.random() - 0.5) * Math.toRadians(CONE_WIDTH);
                double distance = Math.random() * 3.0;

                Vec3 right = lookDir.cross(new Vec3(0, 1, 0)).normalize();
                Vec3 rotatedDir = lookDir.scale(Math.cos(angle)).add(right.scale(Math.sin(angle)));
                Vec3 particlePos = eyePos.add(rotatedDir.scale(distance));

                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                        0.05);

                if (Math.random() < 0.4) {
                    serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                            particlePos.x, particlePos.y, particlePos.z, 1,
                            CAST_PARTICLE_SPREAD * 0.5, CAST_PARTICLE_SPREAD * 0.5, CAST_PARTICLE_SPREAD * 0.5,
                            0.02);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Cone of Cold";
    }
}