package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.ArcSpriteEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

public class ArcSpriteSpell extends SpellEffect {
    private static final int SPRITE_DURATION = 2400;
    private static final float SPRITE_DAMAGE = 1.0F;
    private static final int CAST_PARTICLE_COUNT = 25;
    private static final double CAST_PARTICLE_SPREAD = 1.0;
    private static final float SOUND_VOLUME = 0.8F;
    private static final float SOUND_PITCH = 1.4F;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        Vec3 summonPos = findSummonPosition(caster);

        ArcSpriteEntity arcSprite = new ArcSpriteEntity(ModEntityRegistry.ARC_SPRITE_PROJECTILE.get(), level, caster);
        arcSprite.setPos(summonPos.x, summonPos.y, summonPos.z);
        arcSprite.setDamage(SPRITE_DAMAGE);
        arcSprite.setDuration(SPRITE_DURATION);

        level.addFreshEntity(arcSprite);
        playCastEffects(level, caster, summonPos);
    }

    private Vec3 findSummonPosition(Player caster) {
        Vec3 casterPos = caster.position();
        Vec3 lookDir = caster.getLookAngle();

        Vec3 rightVector = lookDir.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 offset = rightVector.scale(-2.0);

        return new Vec3(
                casterPos.x + offset.x,
                casterPos.y + 1.5,
                casterPos.z + offset.z
        );
    }

    private void playCastEffects(Level level, Player caster, Vec3 summonPos) {
        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS,
                SOUND_VOLUME * 0.4F, SOUND_PITCH);

        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                SOUND_VOLUME * 0.5F, SOUND_PITCH + 0.6F);

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < CAST_PARTICLE_COUNT; i++) {
                double angle = 2 * Math.PI * i / CAST_PARTICLE_COUNT;
                double radius = 1.2 + Math.random() * 0.8;
                double x = summonPos.x + Math.cos(angle) * radius;
                double z = summonPos.z + Math.sin(angle) * radius;
                double y = summonPos.y + Math.random() * 1.2;

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        x, y, z, 1,
                        0, 0.1, 0, 0.03);

                if (Math.random() < 0.3) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            x, y, z, 1,
                            0.1, 0.1, 0.1, 0.01);
                }
            }

            for (int i = 0; i < 6; i++) {
                double zigzagAngle = i * Math.PI / 3;
                double zigzagRadius = 0.4 + i * 0.15;
                double x = summonPos.x + Math.cos(zigzagAngle) * zigzagRadius;
                double z = summonPos.z + Math.sin(zigzagAngle) * zigzagRadius;
                double y = summonPos.y + i * 0.25;

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        x, y, z, 2,
                        0.05, 0.05, 0.05, 0.02);
            }

            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    summonPos.x, summonPos.y, summonPos.z, 12,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.08);

            serverLevel.sendParticles(ParticleTypes.FLASH,
                    summonPos.x, summonPos.y, summonPos.z, 1,
                    0, 0, 0, 0);
        }
    }

    @Override
    public String getName() {
        return "Arc Sprite";
    }
}