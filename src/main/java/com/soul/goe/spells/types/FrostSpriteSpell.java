package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.FrostSpriteEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class FrostSpriteSpell extends SpellEffect {
    private static final int SPRITE_DURATION = 2400;
    private static final float SPRITE_DAMAGE = 1.0F;
    private static final int CAST_PARTICLE_COUNT = 30;
    private static final double CAST_PARTICLE_SPREAD = 1.2;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.6F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 summonPos = findSummonPosition(caster);

        FrostSpriteEntity frostSprite = new FrostSpriteEntity(ModEntityRegistry.FROST_SPRITE_PROJECTILE.get(), level, caster);
        frostSprite.setPos(summonPos.x, summonPos.y, summonPos.z);
        frostSprite.setDamage(SPRITE_DAMAGE);
        frostSprite.setDuration(SPRITE_DURATION);

        level.addFreshEntity(frostSprite);
        playCastEffects(level, caster, summonPos);
    }

    private Vec3 findSummonPosition(Player caster) {
        Vec3 casterPos = caster.position();
        Vec3 lookDir = caster.getLookAngle();

        Vec3 rightVector = lookDir.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 offset = rightVector.scale(2.0);

        return new Vec3(
                casterPos.x + offset.x,
                casterPos.y + 1.5,
                casterPos.z + offset.z
        );
    }

    private void playCastEffects(Level level, Player caster, Vec3 summonPos) {
        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                SoundEvents.SNOW_PLACE, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                SOUND_VOLUME * 0.6F, SOUND_PITCH + 0.4F);

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < CAST_PARTICLE_COUNT; i++) {
                double angle = 2 * Math.PI * i / CAST_PARTICLE_COUNT;
                double radius = 1.5 + Math.random() * 1.0;
                double x = summonPos.x + Math.cos(angle) * radius;
                double z = summonPos.z + Math.sin(angle) * radius;
                double y = summonPos.y + Math.random() * 1.5;

                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        x, y, z, 1,
                        0, 0.1, 0, 0.02);

                if (Math.random() < 0.4) {
                    serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                            x, y, z, 1,
                            0.1, 0.1, 0.1, 0.01);
                }
            }

            for (int i = 0; i < 8; i++) {
                double spiralAngle = i * Math.PI / 4;
                double spiralRadius = 0.5 + i * 0.2;
                double x = summonPos.x + Math.cos(spiralAngle) * spiralRadius;
                double z = summonPos.z + Math.sin(spiralAngle) * spiralRadius;
                double y = summonPos.y + i * 0.3;

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        x, y, z, 1,
                        0, 0, 0, 0.02);
            }

            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    summonPos.x, summonPos.y, summonPos.z, 15,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.1);

            serverLevel.sendParticles(ParticleTypes.FLASH,
                    summonPos.x, summonPos.y, summonPos.z, 1,
                    0, 0, 0, 0);
        }
    }

    @Override
    public String getName() {
        return "Frost Sprite";
    }
}