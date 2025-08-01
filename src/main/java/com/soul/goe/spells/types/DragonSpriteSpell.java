package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.DragonSpriteEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class DragonSpriteSpell extends SpellEffect {
    private static final int SPRITE_DURATION = 3000;
    private static final float SPRITE_DAMAGE = 1.8F;
    private static final int CAST_PARTICLE_COUNT = 40;
    private static final double CAST_PARTICLE_SPREAD = 1.5;
    private static final float SOUND_VOLUME = 1.2F;
    private static final float SOUND_PITCH = 0.8F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 summonPos = findSummonPosition(caster);

        DragonSpriteEntity dragonSprite = new DragonSpriteEntity(ModEntityRegistry.DRAGON_SPRITE_PROJECTILE.get(), level, caster);
        dragonSprite.setPos(summonPos.x, summonPos.y, summonPos.z);
        dragonSprite.setDamage(SPRITE_DAMAGE);
        dragonSprite.setDuration(SPRITE_DURATION);

        level.addFreshEntity(dragonSprite);
        playCastEffects(level, caster, summonPos);
    }

    private Vec3 findSummonPosition(Player caster) {
        Vec3 casterPos = caster.position();

        return new Vec3(
                casterPos.x,
                casterPos.y + 2.5,
                casterPos.z
        );
    }

    private void playCastEffects(Level level, Player caster, Vec3 summonPos) {
        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS,
                SOUND_VOLUME * 0.5F, SOUND_PITCH);

        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                SOUND_VOLUME * 0.4F, SOUND_PITCH + 0.8F);

        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                SOUND_VOLUME * 0.3F, SOUND_PITCH + 0.5F);

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < CAST_PARTICLE_COUNT; i++) {
                double angle = 2 * Math.PI * i / CAST_PARTICLE_COUNT;
                double radius = 2.0 + Math.random() * 1.5;
                double x = summonPos.x + Math.cos(angle) * radius;
                double z = summonPos.z + Math.sin(angle) * radius;
                double y = summonPos.y + Math.random() * 2.0;

                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                        x, y, z, 2,
                        0, 0.15, 0, 0.03);

                if (Math.random() < 0.5) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            x, y, z, 1,
                            0.1, 0.1, 0.1, 0.02);
                }

                if (Math.random() < 0.3) {
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            x, y, z, 1,
                            0.1, 0.1, 0.1, 0.01);
                }
            }

            for (int i = 0; i < 15; i++) {
                double spiralAngle = i * Math.PI / 7.5;
                double spiralRadius = 0.8 + i * 0.2;
                double x = summonPos.x + Math.cos(spiralAngle) * spiralRadius;
                double z = summonPos.z + Math.sin(spiralAngle) * spiralRadius;
                double y = summonPos.y + i * 0.15;

                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        x, y, z, 1,
                        0, 0, 0, 0.02);
            }

            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    summonPos.x, summonPos.y, summonPos.z, 25,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.12);

            serverLevel.sendParticles(ParticleTypes.FLASH,
                    summonPos.x, summonPos.y, summonPos.z, 2,
                    0.2, 0.2, 0.2, 0);

            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    summonPos.x, summonPos.y, summonPos.z, 1,
                    0, 0, 0, 0);
        }
    }

    @Override
    public String getName() {
        return "Dragon Sprite";
    }
}