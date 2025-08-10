package com.soul.goe.spells.types.projectiles;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseProjectileSpell;
import com.soul.goe.spells.config.ProjectileSpellConfig;
import com.soul.goe.spells.entities.projectiles.FireboltEntity;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireboltSpell extends BaseProjectileSpell<FireboltEntity> {

    @Override
    protected EntityType<FireboltEntity> getEntityType() {
        return ModEntityRegistry.FIREBOLT_PROJECTILE.get();
    }

    @Override
    protected ProjectileSpellConfig getSpellConfig() {
        return ProjectileSpellConfig.firebolt();
    }

    @Override
    protected FireboltEntity createProjectile(EntityType<FireboltEntity> entityType, Level level, LivingEntity owner) {
        return new FireboltEntity(entityType, level, owner);
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
    protected void configureProjectile(FireboltEntity projectile, ProjectileSpellConfig config, WandStats wandStats, boolean isCriticalCast) {
        if (wandStats == null) {
            projectile.setDamageRange(1.0F, 10.0F);
            projectile.setFireDuration(40);
            projectile.setMaxRange(120);
        } else {
            float powerModifier = wandStats.power();
            if (config.spellAffinity().equals(wandStats.affinity())) {
                powerModifier += config.affinityPowerBonus();
            }

            projectile.setDamageRange(1.0F * powerModifier, 10.0F * powerModifier);
            projectile.setFireDuration(Math.round(40 * powerModifier));
            projectile.setMaxRange(Math.round(120 * wandStats.durability()));
            projectile.setCriticalChance(wandStats.critical());
            projectile.setCriticalMultipliers(2.0f, 2.0f);
            projectile.setIsCriticalCast(isCriticalCast);
        }
    }

    @Override
    public String getName() {
        return "Fire Bolt";
    }
}