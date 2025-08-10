package com.soul.goe.spells.entities.projectiles;

import com.soul.goe.spells.config.ProjectileEntity;
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

public class RayOfFrostEntity extends Projectile implements ProjectileEntity {
    private static final EntityDataAccessor<Float> DAMAGE_MIN = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE_MAX = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SLOW_DURATION = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_SLOW_MULTIPLIER = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_FREEZE_CHANCE = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> POWER_MODIFIER = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(RayOfFrostEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE_MIN = 1.0F;
    private static final float DEFAULT_DAMAGE_MAX = 8.0F;
    private static final int DEFAULT_SLOW_DURATION = 20;
    private static final int DEFAULT_MAX_RANGE = 120;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final float DEFAULT_CRITICAL_SLOW_MULTIPLIER = 2.0F;
    private static final float DEFAULT_CRITICAL_FREEZE_CHANCE = 0.3F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final float DEFAULT_POWER_MODIFIER = 1.0F;
    private static final float DEFAULT_WAND_STAT = 1.0F;
    private static final int BASE_PARTICLE_INTERVAL = 1;
    private static final double BASE_TRAIL_PARTICLE_SPREAD = 0.1;
    private static final int BASE_EXPLOSION_PARTICLE_COUNT = 20;
    private static final double BASE_EXPLOSION_PARTICLE_SPREAD = 0.4;
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
        setCriticalChance(DEFAULT_CRITICAL_CHANCE);
        setCriticalSlowMultiplier(DEFAULT_CRITICAL_SLOW_MULTIPLIER);
        setCriticalFreezeChance(DEFAULT_CRITICAL_FREEZE_CHANCE);
        setIsCriticalCast(DEFAULT_IS_CRITICAL_CAST);
        setPowerModifier(DEFAULT_POWER_MODIFIER);
        setWandStats(DEFAULT_WAND_STAT, DEFAULT_WAND_STAT, DEFAULT_WAND_STAT);
        this.startPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE_MIN, DEFAULT_DAMAGE_MIN);
        builder.define(DAMAGE_MAX, DEFAULT_DAMAGE_MAX);
        builder.define(SLOW_DURATION, DEFAULT_SLOW_DURATION);
        builder.define(MAX_RANGE, DEFAULT_MAX_RANGE);
        builder.define(CRITICAL_CHANCE, DEFAULT_CRITICAL_CHANCE);
        builder.define(CRITICAL_SLOW_MULTIPLIER, DEFAULT_CRITICAL_SLOW_MULTIPLIER);
        builder.define(CRITICAL_FREEZE_CHANCE, DEFAULT_CRITICAL_FREEZE_CHANCE);
        builder.define(IS_CRITICAL_CAST, DEFAULT_IS_CRITICAL_CAST);
        builder.define(POWER_MODIFIER, DEFAULT_POWER_MODIFIER);
        builder.define(WAND_POWER, DEFAULT_WAND_STAT);
        builder.define(WAND_STABILITY, DEFAULT_WAND_STAT);
        builder.define(WAND_DURABILITY, DEFAULT_WAND_STAT);
    }

    @Override
    public void setDamage(float damage) {
        setDamageRange(damage, damage);
    }

    public void setDamageRange(float min, float max) {
        this.entityData.set(DAMAGE_MIN, min);
        this.entityData.set(DAMAGE_MAX, max);
    }

    @Override
    public void setCriticalChance(float chance) {
        this.entityData.set(CRITICAL_CHANCE, chance);
    }

    public void setCriticalSlowMultiplier(float multiplier) {
        this.entityData.set(CRITICAL_SLOW_MULTIPLIER, multiplier);
    }

    public void setCriticalFreezeChance(float chance) {
        this.entityData.set(CRITICAL_FREEZE_CHANCE, chance);
    }

    @Override
    public void setIsCriticalCast(boolean isCriticalCast) {
        this.entityData.set(IS_CRITICAL_CAST, isCriticalCast);
    }

    public void setPowerModifier(float powerModifier) {
        this.entityData.set(POWER_MODIFIER, powerModifier);
    }

    @Override
    public void setWandStats(float power, float stability, float durability) {
        this.entityData.set(WAND_POWER, power);
        this.entityData.set(WAND_STABILITY, stability);
        this.entityData.set(WAND_DURABILITY, durability);
    }

    public boolean isCriticalCast() {
        return this.entityData.get(IS_CRITICAL_CAST);
    }

    public float getPowerModifier() {
        return this.entityData.get(POWER_MODIFIER);
    }

    public float getRandomDamage() {
        float min = this.entityData.get(DAMAGE_MIN);
        float max = this.entityData.get(DAMAGE_MAX);
        float criticalChance = this.entityData.get(CRITICAL_CHANCE);
        boolean isCritical = random.nextFloat() < criticalChance;

        float damage = min + random.nextFloat() * (max - min);

        if (isCritical) {
            return damage * 1.5f;
        }

        return damage;
    }

    public boolean isCriticalHit() {
        float criticalChance = this.entityData.get(CRITICAL_CHANCE);
        return random.nextFloat() < criticalChance;
    }

    public void setSlowDuration(int duration) {
        this.entityData.set(SLOW_DURATION, duration);
    }

    public int getSlowDuration() {
        return this.entityData.get(SLOW_DURATION);
    }

    public int getCriticalSlowDuration() {
        int baseDuration = getSlowDuration();
        float criticalMultiplier = this.entityData.get(CRITICAL_SLOW_MULTIPLIER);
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
        float powerModifier = getPowerModifier();
        int particleInterval = Math.max(1, Math.round(BASE_PARTICLE_INTERVAL / powerModifier));
        double spreadModifier = BASE_TRAIL_PARTICLE_SPREAD * Math.max(0.5, powerModifier);

        if (ticksInAir % particleInterval == 0) {
            if (isCriticalCast()) {
                level().addParticle(ParticleTypes.SNOWFLAKE,
                        getX() + (random.nextGaussian() * spreadModifier),
                        getY() + (random.nextGaussian() * spreadModifier),
                        getZ() + (random.nextGaussian() * spreadModifier),
                        0, 0, 0);

                level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        getX() + (random.nextGaussian() * spreadModifier * 1.5),
                        getY() + (random.nextGaussian() * spreadModifier * 1.5),
                        getZ() + (random.nextGaussian() * spreadModifier * 1.5),
                        0, 0, 0);
            } else {
                level().addParticle(ParticleTypes.SNOWFLAKE,
                        getX() + (random.nextGaussian() * spreadModifier),
                        getY() + (random.nextGaussian() * spreadModifier),
                        getZ() + (random.nextGaussian() * spreadModifier),
                        0, 0, 0);

                if (random.nextFloat() < 0.5f) {
                    level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                            getX() + (random.nextGaussian() * spreadModifier),
                            getY() + (random.nextGaussian() * spreadModifier),
                            getZ() + (random.nextGaussian() * spreadModifier),
                            0, 0, 0);
                }
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
            boolean isCritical = isCriticalHit();
            float damage = getRandomDamage();

            livingEntity.hurt(damageSource, damage);

            int slowDuration = isCritical ? getCriticalSlowDuration() : getSlowDuration();
            applySlowEffect(livingEntity, slowDuration, isCritical);

            if (owner instanceof LivingEntity livingOwner) {
                livingEntity.setLastHurtByMob(livingOwner);
            }

            if (isCritical) {
                spawnCriticalHitEffects();
            }
        }

        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!level().isClientSide()) {
            BlockPos hitPos = result.getBlockPos();
            float powerModifier = getPowerModifier();
            float freezeChance = Math.min(0.8f, 0.5f * powerModifier);

            if (level().getBlockState(hitPos).is(Blocks.WATER)) {
                level().setBlockAndUpdate(hitPos, Blocks.ICE.defaultBlockState());
            } else if (level().getFluidState(hitPos).getType() == Fluids.WATER) {
                level().setBlockAndUpdate(hitPos, Blocks.ICE.defaultBlockState());
            }

            int freezeRadius = powerModifier > 1.5f ? 2 : 1;
            for (BlockPos adjacentPos : BlockPos.betweenClosed(
                    hitPos.offset(-freezeRadius, -freezeRadius, -freezeRadius),
                    hitPos.offset(freezeRadius, freezeRadius, freezeRadius))) {
                if (level().getBlockState(adjacentPos).is(Blocks.WATER) && random.nextFloat() < freezeChance) {
                    level().setBlockAndUpdate(adjacentPos, Blocks.ICE.defaultBlockState());
                } else if (level().getFluidState(adjacentPos).getType() == Fluids.WATER && random.nextFloat() < freezeChance) {
                    level().setBlockAndUpdate(adjacentPos, Blocks.ICE.defaultBlockState());
                }
            }
        }

        explode();
    }

    private void applySlowEffect(LivingEntity entity) {
        applySlowEffect(entity, getSlowDuration(), false);
    }

    private void applySlowEffect(LivingEntity entity, int duration, boolean isCritical) {
        Vec3 currentMotion = entity.getDeltaMovement();
        float slowScale = isCritical ? 0.3f : 0.5f;
        entity.setDeltaMovement(currentMotion.scale(slowScale));

        int slowLevel = isCritical ? 3 : 2;
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                duration,
                slowLevel
        ));

        if (isCritical) {
            float freezeChance = this.entityData.get(CRITICAL_FREEZE_CHANCE);
            if (random.nextFloat() < freezeChance) {
                entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), 60));
            }
        }
    }

    private void spawnCriticalHitEffects() {
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    getX(), getY(), getZ(), 25,
                    0.5, 0.5, 0.5, 0.1);

            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL,
                    0.8F, 1.5F);
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
        if (!level().isClientSide()) {
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL,
                    EXPLOSION_VOLUME, EXPLOSION_PITCH);

            if (level() instanceof ServerLevel serverLevel) {
                float powerModifier = getPowerModifier();
                int particleCount = Math.round(BASE_EXPLOSION_PARTICLE_COUNT * powerModifier);
                double spread = BASE_EXPLOSION_PARTICLE_SPREAD * Math.max(0.5, powerModifier);

                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        getX(), getY(), getZ(), particleCount,
                        spread, spread, spread,
                        0.1 * powerModifier);

                serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                        getX(), getY(), getZ(), particleCount / 2,
                        spread, spread, spread,
                        0.05 * powerModifier);
            }

            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner();
    }
}