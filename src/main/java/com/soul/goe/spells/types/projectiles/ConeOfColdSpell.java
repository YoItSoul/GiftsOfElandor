package com.soul.goe.spells.types.projectiles;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseProjectileSpell;
import com.soul.goe.spells.config.ProjectileSpellConfig;
import com.soul.goe.spells.entities.projectiles.ConeOfColdEntity;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ConeOfColdSpell extends BaseProjectileSpell<ConeOfColdEntity> {

    @Override
    protected EntityType<ConeOfColdEntity> getEntityType() {
        return ModEntityRegistry.CONE_OF_COLD_PROJECTILE.get();
    }

    @Override
    protected ProjectileSpellConfig getSpellConfig() {
        return ProjectileSpellConfig.coneOfCold();
    }

    @Override
    protected ConeOfColdEntity createProjectile(EntityType<ConeOfColdEntity> entityType, Level level, LivingEntity owner) {
        return new ConeOfColdEntity(entityType, level, owner);
    }

    @Override
    protected Vec3 calculateLaunchDirection(Player caster, float stabilityModifier) {
        Vec3 baseLookDirection = caster.getLookAngle();
        float directionOffset = Math.max(0.0f, (1.0f - stabilityModifier) * 0.1f);

        if (directionOffset > 0.0f) {
            double offsetX = (caster.getRandom().nextGaussian() * directionOffset);
            double offsetY = (caster.getRandom().nextGaussian() * directionOffset);
            double offsetZ = (caster.getRandom().nextGaussian() * directionOffset);
            return baseLookDirection.add(offsetX, offsetY, offsetZ).normalize();
        }
        return baseLookDirection;
    }

    @Override
    protected Vec3 calculateStartPosition(Player caster, Vec3 direction) {
        return caster.getEyePosition().add(direction.scale(0.5));
    }

    @Override
    protected void configureProjectile(ConeOfColdEntity projectile, ProjectileSpellConfig config, WandStats wandStats, boolean isCriticalCast) {
        if (wandStats == null) {
            projectile.setDamage(config.baseDamage());
            projectile.setConeRange(15);
            projectile.setConeWidth(60.0F);
            projectile.setCriticalChance(0.05f);
            projectile.setCriticalMultipliers(config.criticalDamageMultiplier(), config.criticalRangeMultiplier());
        } else {
            float powerModifier = wandStats.power();
            float stabilityModifier = wandStats.stability();
            float durabilityModifier = wandStats.durability();

            if (config.spellAffinity().equals(wandStats.affinity())) {
                powerModifier += config.affinityPowerBonus();
            }

            float adjustedDamage = config.baseDamage() * powerModifier;
            int adjustedRange = Math.round(15 * durabilityModifier);
            float adjustedWidth = calculateConeWidth(stabilityModifier);

            if (isCriticalCast) {
                adjustedDamage *= config.criticalDamageMultiplier();
                adjustedRange = Math.round(adjustedRange * config.criticalRangeMultiplier());
            }

            projectile.setDamage(adjustedDamage);
            projectile.setConeRange(adjustedRange);
            projectile.setConeWidth(adjustedWidth);
            projectile.setCriticalChance(wandStats.critical());
            projectile.setCriticalMultipliers(config.criticalDamageMultiplier(), config.criticalRangeMultiplier());
            projectile.setWandStats(powerModifier, stabilityModifier, durabilityModifier);
            projectile.setIsCriticalCast(isCriticalCast);
        }
    }

    private float calculateConeWidth(float stabilityModifier) {
        float widthVariation = Math.max(0.0f, (1.0f - stabilityModifier) * 20.0f);
        float randomVariation = (float) ((Math.random() - 0.5) * 2 * widthVariation);
        return Math.max(30.0f, 60.0F + randomVariation);
    }

    @Override
    public String getName() {
        return "Cone of Cold";
    }
}