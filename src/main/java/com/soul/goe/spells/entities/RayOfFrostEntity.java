package com.soul.goe.spells.entities;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RayOfFrostEntity extends Projectile {
    private static final EntityDataAccessor<Float> DAMAGE_MIN = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE_MAX = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SLOW_DURATION = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.INT);

    private static final float DEFAULT_DAMAGE_MIN = 1.0F;
    private static final float DEFAULT_DAMAGE_MAX = 8.0F;
    private static final int DEFAULT_SLOW_DURATION = 20;
    private static final int DEFAULT_MAX_RANGE = 120;
    private static final int PARTICLE_INTERVAL = 1;
    private static final double TRAIL_PARTICLE_SPREAD = 0.1;
    private static final int EXPLOSION_PARTICLE_COUNT = 20;
    private static final double EXPLOSION_PARTICLE_SPREAD = 0.4;
    private static final float EXPLOSION_VOLUME = 0.5F;
    private static final float EXPLOSION_PITCH = 1.5F;

    private int ticksInAir = 0;
    private Vec3 startPos;

    public RayOfFrostEntity(EntityType<? extends RayOfFrostEntity> entityType, Level level) {
        super(entityType, level);
    }

    public RayOfFrostEntity(EntityType<? extends RayOfFrostEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setDamageRange(DEFAULT_DAMAGE_MIN, DEFAULT_DAMAGE_MAX);
        setSlowDuration(DEFAULT_SLOW_DURATION);
        setMaxRange(DEFAULT_MAX_RANGE);
        this.startPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE_MIN, DEFAULT_DAMAGE_MIN);
        builder.define(DAMAGE_MAX, DEFAULT_DAMAGE_MAX);
        builder.define(SLOW_DURATION, DEFAULT_SLOW_DURATION);
        builder.define(MAX_RANGE, DEFAULT_MAX_RANGE);
    }

    public void setDamageRange(float min, float max) {
        this.entityData.set(DAMAGE_MIN, min);
        this.entityData.set(DAMAGE_MAX, max);
    }

    public float getRandomDamage() {
        float min = this.entityData.get(DAMAGE_MIN);
        float max = this.entityData.get(DAMAGE_MAX);
        return min + random.nextFloat() * (max - min);
    }

    public void setSlowDuration(int duration) {
        this.entityData.set(SLOW_DURATION, duration);
    }

    public int getSlowDuration() {
        return this.entityData.get(SLOW_DURATION);
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
            level().addParticle(ParticleTypes.SNOWFLAKE,
                    getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    0, 0, 0);

            if (random.nextFloat() < 0.5f) {
                level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                        getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                        getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                        0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        Entity owner = getOwner();

        if (hitEntity == owner) return;

        if (hitEntity instanceof LivingEntity livingEntity) {
            if (livingEntity.getType().is(EntityTypeTags.UNDEAD)) {
                applySlowEffect(livingEntity);
                explode();
                return;
            }

            DamageSource damageSource = this.damageSources().thrown(this, owner);
            float damage = getRandomDamage();

            livingEntity.hurt(damageSource, damage);

            applySlowEffect(livingEntity);

            if (owner instanceof LivingEntity livingOwner) {
                livingEntity.setLastHurtByMob(livingOwner);
            }
        }

        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!level().isClientSide()) {
            BlockPos hitPos = result.getBlockPos();

            if (level().getBlockState(hitPos).is(Blocks.WATER)) {
                level().setBlockAndUpdate(hitPos, Blocks.ICE.defaultBlockState());
            } else if (level().getFluidState(hitPos).getType() == Fluids.WATER) {
                level().setBlockAndUpdate(hitPos, Blocks.ICE.defaultBlockState());
            }

            for (BlockPos adjacentPos : BlockPos.betweenClosed(hitPos.offset(-1, -1, -1), hitPos.offset(1, 1, 1))) {
                if (level().getBlockState(adjacentPos).is(Blocks.WATER) && random.nextFloat() < 0.5f) {
                    level().setBlockAndUpdate(adjacentPos, Blocks.ICE.defaultBlockState());
                } else if (level().getFluidState(adjacentPos).getType() == Fluids.WATER && random.nextFloat() < 0.5f) {
                    level().setBlockAndUpdate(adjacentPos, Blocks.ICE.defaultBlockState());
                }
            }
        }

        explode();
    }

    private void applySlowEffect(LivingEntity entity) {
        Vec3 currentMotion = entity.getDeltaMovement();
        entity.setDeltaMovement(currentMotion.scale(0.5));

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                getSlowDuration(),
                2
        ));
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
                    SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL,
                    EXPLOSION_VOLUME, EXPLOSION_PITCH);

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT,
                        EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD,
                        0.1);

                serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT / 2,
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