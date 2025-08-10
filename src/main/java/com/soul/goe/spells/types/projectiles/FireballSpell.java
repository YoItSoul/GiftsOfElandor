package com.soul.goe.spells.types.projectiles;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseProjectileSpell;
import com.soul.goe.spells.config.ProjectileSpellConfig;
import com.soul.goe.spells.entities.projectiles.FireballEntity;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireballSpell extends BaseProjectileSpell<FireballEntity> {

    @Override
    protected EntityType<FireballEntity> getEntityType() {
        return ModEntityRegistry.FIREBALL_PROJECTILE.get();
    }

    @Override
    protected ProjectileSpellConfig getSpellConfig() {
        return ProjectileSpellConfig.fireball();
    }

    @Override
    protected FireballEntity createProjectile(EntityType<FireballEntity> entityType, Level level, LivingEntity owner) {
        return new FireballEntity(entityType, level, owner);
    }

    @Override
    protected Vec3 calculateLaunchDirection(Player caster, float stabilityModifier) {
        return calculateAccurateDirection(caster, stabilityModifier, getSpellConfig().maxAccuracyOffset());
    }

    @Override
    protected Vec3 calculateStartPosition(Player caster, Vec3 direction) {
        return getStandardStartPosition(caster, direction, 0.8f);
    }

    @Override
    protected void configureProjectile(FireballEntity projectile, ProjectileSpellConfig config, WandStats wandStats, boolean isCriticalCast) {
        if (wandStats == null) {
            projectile.setDamage(config.baseDamage());
            projectile.setExplosionRadius(3);
            projectile.setCriticalChance(0.05f);
            projectile.setCriticalMultipliers(config.criticalDamageMultiplier(), config.criticalRangeMultiplier());
        } else {
            float powerModifier = wandStats.power();
            float durabilityModifier = wandStats.durability();

            if (config.spellAffinity().equals(wandStats.affinity())) {
                powerModifier += config.affinityPowerBonus();
            }

            float adjustedDamage = config.baseDamage() * powerModifier;
            int adjustedRadius = Math.max(1, Math.round(3 * durabilityModifier));

            if (isCriticalCast) {
                adjustedDamage *= config.criticalDamageMultiplier();
                adjustedRadius = Math.round(adjustedRadius * config.criticalRangeMultiplier());
            }

            projectile.setDamage(adjustedDamage);
            projectile.setExplosionRadius(adjustedRadius);
            projectile.setCriticalChance(wandStats.critical());
            projectile.setCriticalMultipliers(config.criticalDamageMultiplier(), config.criticalRangeMultiplier());
            projectile.setWandStats(powerModifier, wandStats.stability(), durabilityModifier);
            projectile.setIsCriticalCast(isCriticalCast);
        }
    }

    @Override
    public String getName() {
        return "Fireball";
    }
}