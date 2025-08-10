package com.soul.goe.spells.types.projectiles;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseProjectileSpell;
import com.soul.goe.spells.config.ProjectileSpellConfig;
import com.soul.goe.spells.entities.projectiles.MagicMissileEntity;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MagicMissileSpell extends BaseProjectileSpell<MagicMissileEntity> {
    private static final int BASE_MISSILES = 3;
    private static final double BASE_TARGET_SEARCH_RADIUS = 120.0;
    private static final float CRITICAL_DAMAGE_MULTIPLIER = 1.5f;
    private static final int CRITICAL_BONUS_MISSILES = 2;

    @Override
    protected EntityType<MagicMissileEntity> getEntityType() {
        return ModEntityRegistry.MAGIC_MISSILE_PROJECTILE.get();
    }

    @Override
    protected ProjectileSpellConfig getSpellConfig() {
        return ProjectileSpellConfig.magicMissile();
    }

    @Override
    protected MagicMissileEntity createProjectile(EntityType<MagicMissileEntity> entityType, Level level, LivingEntity owner) {
        return new MagicMissileEntity(entityType, level, owner);
    }

    @Override
    protected Vec3 calculateLaunchDirection(Player caster, float stabilityModifier) {
        return caster.getLookAngle();
    }

    @Override
    protected Vec3 calculateStartPosition(Player caster, Vec3 direction) {
        return getStandardStartPosition(caster, direction, 0.8f);
    }

    @Override
    protected void configureProjectile(MagicMissileEntity projectile, ProjectileSpellConfig config, WandStats wandStats, boolean isCriticalCast) {
        if (wandStats == null) {
            fireMissiles(projectile.level(), (Player) projectile.getOwner(), BASE_MISSILES, 1.0f, 1.0f, 1.0f, false);
        } else {
            fireMissiles(projectile.level(), (Player) projectile.getOwner(), BASE_MISSILES,
                    wandStats.power(), wandStats.stability(), wandStats.durability(), isCriticalCast);
        }
        projectile.discard();
    }

    private void fireMissiles(Level level, Player caster, int baseMissileCount,
                              float powerModifier, float stabilityModifier, float durabilityModifier, boolean isCriticalCast) {
        ProjectileSpellConfig config = getSpellConfig();

        if (config.spellAffinity().equals("void")) {
            powerModifier += config.affinityPowerBonus();
        }

        int powerBonusMissiles = Math.round((powerModifier - 1.0f) * 2);
        int durabilityBonusMissiles = Math.round((durabilityModifier - 1.0f) * 3);
        int criticalBonusMissiles = isCriticalCast ? CRITICAL_BONUS_MISSILES : 0;

        int totalMissiles = Math.max(1, baseMissileCount + powerBonusMissiles + durabilityBonusMissiles + criticalBonusMissiles);

        Vec3 lookDirection = caster.getLookAngle();
        Vec3 startPos = caster.getEyePosition().add(lookDirection.scale(0.8));

        float singleRollDamage = (1 + level.random.nextInt(4) + 1) * powerModifier;
        if (isCriticalCast) {
            singleRollDamage *= CRITICAL_DAMAGE_MULTIPLIER;
        }

        float adjustedSpeed = config.baseSpeed() * powerModifier;
        int adjustedRange = Math.round(120 * durabilityModifier);
        double adjustedSearchRadius = BASE_TARGET_SEARCH_RADIUS * durabilityModifier;

        List<LivingEntity> availableTargets = findAvailableTargets(level, caster, adjustedSearchRadius);
        LivingEntity primaryTarget = findPrimaryTarget(caster, availableTargets);

        for (int i = 0; i < totalMissiles; i++) {
            MagicMissileEntity missile = new MagicMissileEntity(getEntityType(), level, caster);

            float spawnSpread = Math.max(0.2f, (2.0f - stabilityModifier) * 0.3f);
            Vec3 missileStart = startPos.add(
                    (Math.random() - 0.5) * spawnSpread,
                    (Math.random() - 0.5) * spawnSpread,
                    (Math.random() - 0.5) * spawnSpread
            );

            missile.setPos(missileStart.x, missileStart.y, missileStart.z);
            missile.setDamage(singleRollDamage);
            missile.setMaxRange(adjustedRange);
            missile.setMissileId(i);
            missile.setWandStats(powerModifier, stabilityModifier, durabilityModifier);
            missile.setIsCriticalCast(isCriticalCast);

            LivingEntity target = selectTarget(availableTargets, primaryTarget, i);
            if (target != null) {
                missile.setTarget(target);
                Vec3 targetDirection = target.getEyePosition().subtract(missileStart).normalize();
                missile.setDeltaMovement(targetDirection.scale(adjustedSpeed));
            } else {
                missile.setDeltaMovement(lookDirection.scale(adjustedSpeed));
            }

            level.addFreshEntity(missile);

            try {
                Thread.sleep(Math.max(20, Math.round(50 / powerModifier)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private List<LivingEntity> findAvailableTargets(Level level, Player caster, double searchRadius) {
        Vec3 casterPos = caster.getEyePosition();
        AABB searchArea = new AABB(casterPos, casterPos).inflate(searchRadius);

        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, searchArea);
        List<LivingEntity> validTargets = new ArrayList<>();

        for (LivingEntity entity : potentialTargets) {
            if (entity != caster && caster.hasLineOfSight(entity) &&
                    caster.distanceTo(entity) <= searchRadius) {
                validTargets.add(entity);
            }
        }

        return validTargets;
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

    public void cast(Level level, Player caster, int spellLevel) {
        if (level.isClientSide()) return;

        int baseMissileCount = BASE_MISSILES + Math.max(0, spellLevel - 1);
        fireMissiles(level, caster, baseMissileCount, 1.0f, 1.0f, 1.0f, false);
    }

    @Override
    public String getName() {
        return "Magic Missile";
    }
}