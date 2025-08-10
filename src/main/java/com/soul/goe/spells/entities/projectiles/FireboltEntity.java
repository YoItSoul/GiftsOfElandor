package com.soul.goe.spells.entities.projectiles;

import com.soul.goe.spells.config.ProjectileEntity;
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

public class FireboltEntity extends Projectile implements ProjectileEntity {
    private static final EntityDataAccessor<Float> DAMAGE_MIN = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE_MAX = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FIRE_DURATION = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_DAMAGE_MULTIPLIER = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_FIRE_MULTIPLIER = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(FireboltEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE_MIN = 1.0F;
    private static final float DEFAULT_DAMAGE_MAX = 10.0F;
    private static final int DEFAULT_FIRE_DURATION = 40;
    private static final int DEFAULT_MAX_RANGE = 120;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final float DEFAULT_CRITICAL_DAMAGE_MULTIPLIER = 2.0F;
    private static final float DEFAULT_CRITICAL_FIRE_MULTIPLIER = 2.0F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final float DEFAULT_WAND_STAT = 1.0F;
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
        setCriticalChance(DEFAULT_CRITICAL_CHANCE);
        setCriticalMultipliers(DEFAULT_CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_FIRE_MULTIPLIER);
        setIsCriticalCast(false);
        setWandStats(DEFAULT_WAND_STAT, DEFAULT_WAND_STAT, DEFAULT_WAND_STAT);
        this.startPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE_MIN, DEFAULT_DAMAGE_MIN);
        builder.define(DAMAGE_MAX, DEFAULT_DAMAGE_MAX);
        builder.define(FIRE_DURATION, DEFAULT_FIRE_DURATION);
        builder.define(MAX_RANGE, DEFAULT_MAX_RANGE);
        builder.define(CRITICAL_CHANCE, DEFAULT_CRITICAL_CHANCE);
        builder.define(CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_DAMAGE_MULTIPLIER);
        builder.define(CRITICAL_FIRE_MULTIPLIER, DEFAULT_CRITICAL_FIRE_MULTIPLIER);
        builder.define(IS_CRITICAL_CAST, DEFAULT_IS_CRITICAL_CAST);
        builder.define(WAND_POWER, DEFAULT_WAND_STAT);
        builder.define(WAND_STABILITY, DEFAULT_WAND_STAT);
        builder.define(WAND_DURABILITY, DEFAULT_WAND_STAT);
    }

    @Override
    public void setDamage(float damage) {
        setDamageRange(damage, damage);
    }

    @Override
    public void setWandStats(float power, float stability, float durability) {
        this.entityData.set(WAND_POWER, power);
        this.entityData.set(WAND_STABILITY, stability);
        this.entityData.set(WAND_DURABILITY, durability);
    }

    public void setDamageRange(float min, float max) {
        this.entityData.set(DAMAGE_MIN, min);
        this.entityData.set(DAMAGE_MAX, max);
    }

    public void setCriticalChance(float chance) {
        this.entityData.set(CRITICAL_CHANCE, chance);
    }

    public void setCriticalMultipliers(float damageMultiplier, float fireMultiplier) {
        this.entityData.set(CRITICAL_DAMAGE_MULTIPLIER, damageMultiplier);
        this.entityData.set(CRITICAL_FIRE_MULTIPLIER, fireMultiplier);
    }

    public void setIsCriticalCast(boolean isCriticalCast) {
        this.entityData.set(IS_CRITICAL_CAST, isCriticalCast);
    }

    public boolean isCriticalCast() {
        return this.entityData.get(IS_CRITICAL_CAST);
    }

    public float getRandomDamage() {
        float min = this.entityData.get(DAMAGE_MIN);
        float max = this.entityData.get(DAMAGE_MAX);
        float criticalChance = this.entityData.get(CRITICAL_CHANCE);
        boolean isCritical = random.nextFloat() < criticalChance;

        float damage = min + random.nextFloat() * (max - min);

        if (isCritical) {
            float criticalMultiplier = this.entityData.get(CRITICAL_DAMAGE_MULTIPLIER);
            return damage * criticalMultiplier;
        }

        return damage;
    }

    public boolean isCriticalHit() {
        float criticalChance = this.entityData.get(CRITICAL_CHANCE);
        return random.nextFloat() < criticalChance;
    }

    public void setFireDuration(int duration) {
        this.entityData.set(FIRE_DURATION, duration);
    }

    public int getFireDuration() {
        return this.entityData.get(FIRE_DURATION);
    }

    public int getCriticalFireDuration() {
        int baseDuration = getFireDuration();
        float criticalMultiplier = this.entityData.get(CRITICAL_FIRE_MULTIPLIER);
        return Math.round(baseDuration * criticalMultiplier);
    }

    @Override
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
            if (isCriticalCast()) {
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                        getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                        getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                        0, 0, 0);

                level().addParticle(ParticleTypes.FLAME,
                        getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD * 1.5),
                        getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD * 1.5),
                        getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD * 1.5),
                        0, 0, 0);
            } else {
                level().addParticle(ParticleTypes.FLAME,
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
            DamageSource damageSource = this.damageSources().thrown(this, owner);
            boolean isCritical = isCriticalHit();
            float damage = getRandomDamage();

            if (livingEntity.getType().is(EntityTypeTags.UNDEAD)) {
                damage *= 1.5f;
            }

            livingEntity.hurt(damageSource, damage);

            int fireDuration = isCritical ? getCriticalFireDuration() : getFireDuration();
            livingEntity.setRemainingFireTicks(fireDuration);

            if (owner instanceof LivingEntity livingOwner) {
                livingEntity.setLastHurtByMob(livingOwner);
            }

            if (isCritical) {
                spawnCriticalHitEffects();
            }
        }

        explode();
    }

    private void spawnCriticalHitEffects() {
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    getX(), getY(), getZ(), 20,
                    0.5, 0.5, 0.5, 0.1);

            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL,
                    0.8F, 1.5F);
        }
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