package com.soul.goe.spells.types.projectiles;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseProjectileSpell;
import com.soul.goe.spells.config.ProjectileSpellConfig;
import com.soul.goe.spells.entities.projectiles.LightningBoltEntity;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LightningBoltSpell extends BaseProjectileSpell<LightningBoltEntity> {

    @Override
    protected EntityType<LightningBoltEntity> getEntityType() {
        return ModEntityRegistry.LIGHTNING_BOLT_PROJECTILE.get();
    }

    @Override
    protected ProjectileSpellConfig getSpellConfig() {
        return ProjectileSpellConfig.lightningBolt();
    }

    @Override
    protected LightningBoltEntity createProjectile(EntityType<LightningBoltEntity> entityType, Level level, LivingEntity owner) {
        return new LightningBoltEntity(entityType, level, owner);
    }

    @Override
    protected Vec3 calculateLaunchDirection(Player caster, float stabilityModifier) {
        return caster.getLookAngle();
    }

    @Override
    protected Vec3 calculateStartPosition(Player caster, Vec3 direction) {
        return caster.getEyePosition().add(direction.scale(0.5));
    }

    @Override
    protected void configureProjectile(LightningBoltEntity projectile, ProjectileSpellConfig config, WandStats wandStats, boolean isCriticalCast) {
        if (wandStats == null) {
            projectile.setDamage(config.baseDamage());
            projectile.setLineRange(25);
            projectile.setLineWidth(5.0F);
            projectile.setCriticalChance(0.05f);
            projectile.setCriticalMultipliers(config.criticalDamageMultiplier(), 1.8f);
        } else {
            float powerModifier = wandStats.power();
            float stabilityModifier = wandStats.stability();
            float durabilityModifier = wandStats.durability();

            if (config.spellAffinity().equals(wandStats.affinity())) {
                powerModifier += config.affinityPowerBonus();
            }

            float adjustedDamage = config.baseDamage() * powerModifier;
            int adjustedRange = Math.round(25 * durabilityModifier);
            float adjustedWidth = calculateLineWidth(stabilityModifier);

            if (isCriticalCast) {
                adjustedDamage *= config.criticalDamageMultiplier();
                adjustedWidth *= 1.8f;
            }

            projectile.setDamage(adjustedDamage);
            projectile.setLineRange(adjustedRange);
            projectile.setLineWidth(adjustedWidth);
            projectile.setCriticalChance(wandStats.critical());
            projectile.setCriticalMultipliers(config.criticalDamageMultiplier(), 1.8f);
            projectile.setWandStats(powerModifier, stabilityModifier, durabilityModifier);
            projectile.setIsCriticalCast(isCriticalCast);
        }
    }

    private float calculateLineWidth(float stabilityModifier) {
        float widthVariation = Math.max(0.0f, (1.0f - stabilityModifier) * 3.0f);
        float randomVariation = (float) ((Math.random() - 0.5) * 2 * widthVariation);
        return Math.max(2.0f, 5.0F + randomVariation);
    }

    @Override
    public String getName() {
        return "Lightning Bolt";
    }
}