package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.FlameSpriteEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class FlameSpriteSpell extends SpellEffect {
    private static final int SPRITE_DURATION = 2400;
    private static final float SPRITE_DAMAGE = 1.0F;
    private static final int CAST_PARTICLE_COUNT = 28;
    private static final double CAST_PARTICLE_SPREAD = 1.1;
    private static final float SOUND_VOLUME = 0.9F;
    private static final float SOUND_PITCH = 1.3F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 summonPos = findSummonPosition(caster);

        FlameSpriteEntity flameSprite = new FlameSpriteEntity(ModEntityRegistry.FLAME_SPRITE_PROJECTILE.get(), level, caster);
        flameSprite.setPos(summonPos.x, summonPos.y, summonPos.z);
        flameSprite.setDamage(SPRITE_DAMAGE);
        flameSprite.setDuration(SPRITE_DURATION);

        level.addFreshEntity(flameSprite);
        playCastEffects(level, caster, summonPos);
    }

    private Vec3 findSummonPosition(Player caster) {
        Vec3 casterPos = caster.position();
        Vec3 lookDir = caster.getLookAngle();

        Vec3 leftVector = lookDir.cross(new Vec3(0, -1, 0)).normalize();
        Vec3 offset = leftVector.scale(2.2);

        return new Vec3(
                casterPos.x + offset.x,
                casterPos.y + 1.5,
                casterPos.z + offset.z
        );
    }

    private void playCastEffects(Level level, Player caster, Vec3 summonPos) {
        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                SOUND_VOLUME * 0.6F, SOUND_PITCH);

        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                SOUND_VOLUME * 0.5F, SOUND_PITCH + 0.3F);

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < CAST_PARTICLE_COUNT; i++) {
                double angle = 2 * Math.PI * i / CAST_PARTICLE_COUNT;
                double radius = 1.3 + Math.random() * 1.2;
                double x = summonPos.x + Math.cos(angle) * radius;
                double z = summonPos.z + Math.sin(angle) * radius;
                double y = summonPos.y + Math.random() * 1.4;

                serverLevel.sendParticles(ParticleTypes.FLAME,
                        x, y, z, 1,
                        0, 0.1, 0, 0.02);

                if (Math.random() < 0.4) {
                    serverLevel.sendParticles(ParticleTypes.SMALL_FLAME,
                            x, y, z, 1,
                            0.1, 0.1, 0.1, 0.01);
                }
            }

            for (int i = 0; i < 10; i++) {
                double spiralAngle = i * Math.PI / 5;
                double spiralRadius = 0.6 + i * 0.15;
                double x = summonPos.x + Math.cos(spiralAngle) * spiralRadius;
                double z = summonPos.z + Math.sin(spiralAngle) * spiralRadius;
                double y = summonPos.y + i * 0.2;

                serverLevel.sendParticles(ParticleTypes.LAVA,
                        x, y, z, 1,
                        0, 0, 0, 0.01);
            }

            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    summonPos.x, summonPos.y, summonPos.z, 18,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.1);

            serverLevel.sendParticles(ParticleTypes.FLASH,
                    summonPos.x, summonPos.y, summonPos.z, 1,
                    0, 0, 0, 0);
        }
    }

    @Override
    public String getName() {
        return "Flame Sprite";
    }
}