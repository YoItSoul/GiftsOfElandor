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
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireboltEntity extends Projectile {
    private static final EntityDataAccessor<Float> DAMAGE_MIN = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE_MAX = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FIRE_DURATION = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.INT);

    private static final float DEFAULT_DAMAGE_MIN = 1.0F;
    private static final float DEFAULT_DAMAGE_MAX = 10.0F;
    private static final int DEFAULT_FIRE_DURATION = 40;
    private static final int DEFAULT_MAX_RANGE = 120;
    private static final int PARTICLE_INTERVAL = 2;
    private static final int TRAIL_PARTICLE_COUNT = 2;
    private static final double TRAIL_PARTICLE_SPREAD = 0.05;
    private static final int EXPLOSION_PARTICLE_COUNT = 15;
    private static final double EXPLOSION_PARTICLE_SPREAD = 0.3;
    private static final float EXPLOSION_VOLUME = 0.7F;
    private static final float EXPLOSION_PITCH = 1.2F;

    private int ticksInAir = 0;
    private Vec3 startPos;

    public FireboltEntity(EntityType<? extends FireboltEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FireboltEntity(EntityType<? extends FireboltEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setDamageRange(DEFAULT_DAMAGE_MIN, DEFAULT_DAMAGE_MAX);
        setFireDuration(DEFAULT_FIRE_DURATION);
        setMaxRange(DEFAULT_MAX_RANGE);
        this.startPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE_MIN, DEFAULT_DAMAGE_MIN);
        builder.define(DAMAGE_MAX, DEFAULT_DAMAGE_MAX);
        builder.define(FIRE_DURATION, DEFAULT_FIRE_DURATION);
        builder.define(MAX_RANGE, DEFAULT_MAX_RANGE);
    }

    public void setDamageRange(float min, float max) {
        this.entityData.set(DAMAGE_MIN, min);
        this.entityData.set(DAMAGE_MAX, max);
    }

    public float getRandomDamage() {
        float min = this.entityData.get(DAMAGE_MIN);
        float max = this.entityData.get(DAMAGE_MAX);
        boolean isCritical = random.nextFloat() < 0.05f;
        float damage = min + random.nextFloat() * (max - min);
        return isCritical ? damage * 2 : damage;
    }

    public void setFireDuration(int duration) {
        this.entityData.set(FIRE_DURATION, duration);
    }

    public int getFireDuration() {
        return this.entityData.get(FIRE_DURATION);
    }

    public void setMaxRange(int range) {
        this.entityData.set(MAX_RANGE, range);
    }

    public int getMaxRange() {
        return this.entityData.get(MAX_RANGE);
    }

    @Override
    public void tick() {
        super.tick();
        ticksInAir++;

        if (startPos != null && distanceToSqr(startPos) > getMaxRange() * getMaxRange()) {
            explode();
            return;
        }

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
        if (ticksInAir % PARTICLE_INTERVAL == 0) {
            level().addParticle(ParticleTypes.FLAME,
                    getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    0, 0, 0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        Entity owner = getOwner();

        if (hitEntity == owner) return;

        if (hitEntity instanceof LivingEntity livingEntity) {
            DamageSource damageSource = this.damageSources().thrown(this, owner);
            float damage = getRandomDamage();

            if (livingEntity.getType().is(EntityTypeTags.UNDEAD)) {
                damage *= 1.5f;
            }

            livingEntity.hurt(damageSource, damage);
            livingEntity.setRemainingFireTicks(getFireDuration());

            if (owner instanceof LivingEntity livingOwner) {
                livingEntity.setLastHurtByMob(livingOwner);
            }
        }

        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide() && random.nextFloat() < 0.3f) {
            if (BaseFireBlock.canBePlacedAt(level(), result.getBlockPos().relative(result.getDirection()), result.getDirection())) {
                level().setBlockAndUpdate(result.getBlockPos().relative(result.getDirection()),
                        BaseFireBlock.getState(level(), result.getBlockPos().relative(result.getDirection())));
            }
        }
        explode();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            explode();
        }
    }

    private void explode() {
        if (!level().isClientSide()) {
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL,
                    EXPLOSION_VOLUME, EXPLOSION_PITCH);

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT,
                        EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD,
                        0.1);

                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT / 3,
                        EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD,
                        0.05);
            }

            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner();
    }
}