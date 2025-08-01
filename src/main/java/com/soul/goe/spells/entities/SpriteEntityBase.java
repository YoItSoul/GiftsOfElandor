package com.soul.goe.spells.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class SpriteEntityBase extends Projectile {
    protected static final double FUSION_PRIORITY_RADIUS = 8.0;

    protected enum SpriteState {
        ORBITING,
        ATTACKING,
        RETURNING,
        SEEKING_FUSION
    }

    public SpriteEntityBase(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public SpriteEntityBase(EntityType<? extends Projectile> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
    }

    public abstract float getDamage();
    public abstract void setDamage(float damage);
    public abstract int getDuration();
    public abstract void setDuration(int duration);
    public abstract int getAttackCooldown();
    public abstract void setAttackCooldown(int cooldown);

    protected boolean shouldSeekFusion() {
        if (getAttackCooldown() > 0) return false;

        if (!SpriteFusionUtil.canAttemptFusion(this)) return false;

        SpriteEntityBase nearestSprite = SpriteFusionUtil.findNearestCompatibleSprite(this);
        return nearestSprite != null && distanceTo(nearestSprite) <= FUSION_PRIORITY_RADIUS;
    }

    protected void handleFusionSeeking(LivingEntity owner) {
        if (SpriteFusionUtil.attemptFusion(this)) {
            return;
        }

        SpriteEntityBase targetSprite = SpriteFusionUtil.findNearestCompatibleSprite(this);
        if (targetSprite == null) {
            setState(SpriteState.ORBITING);
            return;
        }

        Vec3 targetPos = targetSprite.position();
        Vec3 currentPos = position();
        Vec3 direction = targetPos.subtract(currentPos);
        double distance = direction.length();

        if (distance > 0.1) {
            direction = direction.normalize();
            double speed = Math.min(0.3, distance * 0.1);
            setDeltaMovement(direction.scale(speed));
            setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);
        }

        if (distance > FUSION_PRIORITY_RADIUS * 2.0) {
            setState(SpriteState.ORBITING);
        }
    }

    protected abstract void setState(SpriteState state);
    protected abstract SpriteState getState();

    public void createFusionEffect() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL,
                0.8F, 1.5F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    getX(), getY(), getZ(), 30,
                    0.5, 0.5, 0.5, 0.1);

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    getX(), getY(), getZ(), 15,
                    0.3, 0.3, 0.3, 0.05);
        }
    }
}