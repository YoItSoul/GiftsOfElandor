package com.soul.goe.spells.types.projectiles;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseProjectileSpell;
import com.soul.goe.spells.config.ProjectileSpellConfig;
import com.soul.goe.spells.entities.projectiles.RayOfFrostEntity;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RayOfFrostSpell extends BaseProjectileSpell<RayOfFrostEntity> {

    @Override
    protected EntityType<RayOfFrostEntity> getEntityType() {
        return ModEntityRegistry.RAY_OF_FROST_PROJECTILE.get();
    }

    @Override
    protected ProjectileSpellConfig getSpellConfig() {
        return ProjectileSpellConfig.rayOfFrost();
    }

    @Override
    protected RayOfFrostEntity createProjectile(EntityType<RayOfFrostEntity> entityType, Level level, LivingEntity owner) {
        return new RayOfFrostEntity(entityType, level, owner);
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
    protected void configureProjectile(RayOfFrostEntity projectile, ProjectileSpellConfig config, WandStats wandStats, boolean isCriticalCast) {
        if (wandStats == null) {
            projectile.setDamageRange(1.0F, 8.0F);
            projectile.setSlowDuration(20);
            projectile.setMaxRange(120);
        } else {
            float powerModifier = wandStats.power();
            if (config.spellAffinity().equals(wandStats.affinity())) {
                powerModifier += config.affinityPowerBonus();
            }

            projectile.setDamageRange(1.0F * powerModifier, 8.0F * powerModifier);
            projectile.setSlowDuration(Math.round(20 * powerModifier));
            projectile.setMaxRange(Math.round(120 * wandStats.durability()));
            projectile.setCriticalChance(wandStats.critical());
            projectile.setCriticalSlowMultiplier(2.0f);
            projectile.setCriticalFreezeChance(0.3f);
            projectile.setIsCriticalCast(isCriticalCast);
            projectile.setPowerModifier(powerModifier);
        }
    }

    @Override
    public String getName() {
        return "Ray of Frost";
    }
}