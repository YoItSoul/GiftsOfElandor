package com.soul.goe.spells.types;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.entities.MagicMissileEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.soul.goe.spells.SpellEffect;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MagicMissileSpell extends SpellEffect {
    private static final float MISSILE_SPEED = 0.8F;
    private static final float BASE_DAMAGE = 3.5F;
    private static final int BASE_MISSILES = 3;
    private static final int CAST_PARTICLE_COUNT = 20;
    private static final double CAST_PARTICLE_SPREAD = 0.8;
    private static final float SOUND_VOLUME = 0.9F;
    private static final float SOUND_PITCH = 1.3F;
    private static final int MAX_RANGE = 120;
    private static final double TARGET_SEARCH_RADIUS = 120.0;

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        List<LivingEntity> availableTargets = findAvailableTargets(level, caster);
        int missileCount = BASE_MISSILES;

        fireMissiles(level, caster, availableTargets, missileCount);
        playCastEffects(level, caster);
    }

    public void cast(Level level, Player caster, int spellLevel) {
        if (level.isClientSide()) return;

        List<LivingEntity> availableTargets = findAvailableTargets(level, caster);
        int missileCount = BASE_MISSILES + Math.max(0, spellLevel - 1);

        fireMissiles(level, caster, availableTargets, missileCount);
        playCastEffects(level, caster);
    }

    private List<LivingEntity> findAvailableTargets(Level level, Player caster) {
        Vec3 casterPos = caster.getEyePosition();
        AABB searchArea = new AABB(casterPos, casterPos).inflate(TARGET_SEARCH_RADIUS);

        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, searchArea);
        List<LivingEntity> validTargets = new ArrayList<>();

        for (LivingEntity entity : potentialTargets) {
            if (entity != caster && caster.hasLineOfSight(entity) &&
                    caster.distanceTo(entity) <= MAX_RANGE) {
                validTargets.add(entity);
            }
        }

        return validTargets;
    }

    private void fireMissiles(Level level, Player caster, List<LivingEntity> availableTargets, int missileCount) {
        Vec3 lookDirection = caster.getLookAngle();
        Vec3 startPos = caster.getEyePosition().add(lookDirection.scale(0.8));

        float singleRollDamage = 1 + level.random.nextInt(4) + 1;

        LivingEntity primaryTarget = findPrimaryTarget(caster, availableTargets);

        for (int i = 0; i < missileCount; i++) {
            MagicMissileEntity missile = new MagicMissileEntity(ModEntityRegistry.MAGIC_MISSILE_PROJECTILE.get(), level, caster);

            Vec3 missileStart = startPos.add(
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5
            );

            missile.setPos(missileStart.x, missileStart.y, missileStart.z);
            missile.setDamage(singleRollDamage);
            missile.setMaxRange(MAX_RANGE);
            missile.setMissileId(i);

            LivingEntity target = selectTarget(availableTargets, primaryTarget, i);
            if (target != null) {
                missile.setTarget(target);
                Vec3 targetDirection = target.getEyePosition().subtract(missileStart).normalize();
                missile.setDeltaMovement(targetDirection.scale(MISSILE_SPEED));
            } else {
                missile.setDeltaMovement(lookDirection.scale(MISSILE_SPEED));
            }

            level.addFreshEntity(missile);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private LivingEntity findPrimaryTarget(Player caster, List<LivingEntity> availableTargets) {
        if (availableTargets.isEmpty()) {
            return null;
        }

        Vec3 lookDirection = caster.getLookAngle();
        LivingEntity bestTarget = null;
        double bestAlignment = -1;

        for (LivingEntity target : availableTargets) {
            Vec3 toTarget = target.position().subtract(caster.position()).normalize();
            double alignment = lookDirection.dot(toTarget);

            if (alignment > bestAlignment) {
                bestAlignment = alignment;
                bestTarget = target;
            }
        }

        return bestTarget;
    }

    private LivingEntity selectTarget(List<LivingEntity> availableTargets, LivingEntity primaryTarget, int missileIndex) {
        if (availableTargets.isEmpty()) {
            return null;
        }

        if (primaryTarget != null && missileIndex < 3) {
            return primaryTarget;
        }

        Collections.shuffle(availableTargets);
        return availableTargets.get(missileIndex % availableTargets.size());
    }

    private void playCastEffects(Level level, Player caster) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);

        if (level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = caster.getEyePosition();
            Vec3 lookDir = caster.getLookAngle();
            Vec3 particlePos = eyePos.add(lookDir.scale(0.5));

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.1);

            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    particlePos.x, particlePos.y, particlePos.z,
                    CAST_PARTICLE_COUNT / 2,
                    CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD, CAST_PARTICLE_SPREAD,
                    0.05);

            for (int i = 0; i < BASE_MISSILES; i++) {
                double angle = (i * 2.0 * Math.PI) / BASE_MISSILES;
                double x = particlePos.x + Math.cos(angle) * 0.8;
                double z = particlePos.z + Math.sin(angle) * 0.8;

                serverLevel.sendParticles(ParticleTypes.FLASH,
                        x, particlePos.y, z, 1,
                        0, 0, 0, 0);
            }
        }
    }

    @Override
    public String getName() {
        return "Magic Missile";
    }
}