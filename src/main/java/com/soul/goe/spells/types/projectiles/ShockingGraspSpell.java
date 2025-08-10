package com.soul.goe.spells.types.projectiles;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseProjectileSpell;
import com.soul.goe.spells.config.ProjectileSpellConfig;
import com.soul.goe.spells.entities.projectiles.ShockingGraspEntity;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ShockingGraspSpell extends BaseProjectileSpell<ShockingGraspEntity> {

    @Override
    protected EntityType<ShockingGraspEntity> getEntityType() {
        return ModEntityRegistry.SHOCKING_GRASP_PROJECTILE.get();
    }

    @Override
    protected ProjectileSpellConfig getSpellConfig() {
        return ProjectileSpellConfig.shockingGrasp();
    }

    @Override
    protected ShockingGraspEntity createProjectile(EntityType<ShockingGraspEntity> entityType, Level level, LivingEntity owner) {
        return new ShockingGraspEntity(entityType, level, owner);
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
    protected void configureProjectile(ShockingGraspEntity projectile, ProjectileSpellConfig config, WandStats wandStats, boolean isCriticalCast) {
        if (wandStats == null) {
            projectile.setDamageRange(1.0F, 8.0F);
            projectile.setMaxRange(5);
        } else {
            float powerModifier = wandStats.power();
            if (config.spellAffinity().equals(wandStats.affinity())) {
                powerModifier += config.affinityPowerBonus();
            }

            projectile.setDamageRange(1.0F * powerModifier, 8.0F * powerModifier);
            projectile.setMaxRange(Math.round(5 * wandStats.durability()));
            projectile.setCriticalChance(wandStats.critical());
            projectile.setCriticalStunMultiplier(2.0f);
            projectile.setCriticalChainChance(0.4f);
            projectile.setIsCriticalCast(isCriticalCast);
            projectile.setPowerModifier(powerModifier);
        }
    }

    @Override
    public String getName() {
        return "Shocking Grasp";
    }
}