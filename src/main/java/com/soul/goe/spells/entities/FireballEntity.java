package com.soul.goe.spells.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireballEntity extends Projectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> EXPLOSION_RADIUS = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.INT);

    private static final float DEFAULT_DAMAGE = 24.0F;
    private static final int DEFAULT_EXPLOSION_RADIUS = 4;
    private static final int TRAIL_PARTICLE_COUNT = 5;
    private static final double TRAIL_PARTICLE_SPREAD = 0.2;
    private static final float EXPLOSION_VOLUME = 2.0F;
    private static final float EXPLOSION_PITCH = 0.8F;

    public FireballEntity(EntityType<? extends FireballEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FireballEntity(EntityType<? extends FireballEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setDamage(DEFAULT_DAMAGE);
        setExplosionRadius(DEFAULT_EXPLOSION_RADIUS);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(EXPLOSION_RADIUS, DEFAULT_EXPLOSION_RADIUS);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setExplosionRadius(int radius) {
        this.entityData.set(EXPLOSION_RADIUS, radius);
    }

    public int getExplosionRadius() {
        return this.entityData.get(EXPLOSION_RADIUS);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            spawnTrailParticles();
        } else {
            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitResult.getType() != HitResult.Type.MISS) {
                onHit(hitResult);
            }
        }

        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
    }

    private void spawnTrailParticles() {
        level().addParticle(ParticleTypes.FLAME,
                getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                0, 0, 0);

        if (random.nextFloat() < 0.3f) {
            level().addParticle(ParticleTypes.SMOKE,
                    getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    0, 0, 0);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            explode();
        }
    }

    private void explode() {
        level().explode(this, getX(), getY(), getZ(), getExplosionRadius(), false, Level.ExplosionInteraction.MOB);

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
                EXPLOSION_VOLUME, EXPLOSION_PITCH);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    getX(), getY(), getZ(), 10,
                    0.5, 0.5, 0.5, 0.2);

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    getX(), getY(), getZ(), 50,
                    1.0, 1.0, 1.0, 0.1);
        }

        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(getExplosionRadius()))) {
            if (entity == getOwner()) continue;

            float distance = (float) entity.distanceTo(this);
            float damage = getDamage() * (1.0f - (distance / (getExplosionRadius() * 2.0f)));

            if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                damage *= 1.5f;
            }

            boolean passedSave = entity.getRandom().nextFloat() < 0.5f;
            if (passedSave) {
                damage *= 0.5f;
            }

            DamageSource damageSource = level().damageSources().magic();
            entity.hurt(damageSource, damage);
            entity.setRemainingFireTicks(60);
        }

        discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner();
    }
}