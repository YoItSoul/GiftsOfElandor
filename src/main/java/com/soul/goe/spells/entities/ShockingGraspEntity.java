package com.soul.goe.spells.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShockingGraspEntity extends Projectile {
    private static final EntityDataAccessor<Float> DAMAGE_MIN = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE_MAX = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.INT);

    private static final float DEFAULT_DAMAGE_MIN = 1.0F;
    private static final float DEFAULT_DAMAGE_MAX = 8.0F;
    private static final int DEFAULT_MAX_RANGE = 5;
    private static final int PARTICLE_INTERVAL = 1;
    private static final double TRAIL_PARTICLE_SPREAD = 0.15;
    private static final int EXPLOSION_PARTICLE_COUNT = 25;
    private static final double EXPLOSION_PARTICLE_SPREAD = 0.5;
    private static final float EXPLOSION_VOLUME = 0.8F;
    private static final float EXPLOSION_PITCH = 1.8F;

    private int ticksInAir = 0;
    private Vec3 startPos;

    public ShockingGraspEntity(EntityType<? extends ShockingGraspEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ShockingGraspEntity(EntityType<? extends ShockingGraspEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setDamageRange(DEFAULT_DAMAGE_MIN, DEFAULT_DAMAGE_MAX);
        setMaxRange(DEFAULT_MAX_RANGE);
        this.startPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE_MIN, DEFAULT_DAMAGE_MIN);
        builder.define(DAMAGE_MAX, DEFAULT_DAMAGE_MAX);
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
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    0, 0, 0);

            if (random.nextFloat() < 0.7f) {
                level().addParticle(ParticleTypes.CRIT,
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
            if (livingEntity instanceof Creeper creeper) {
                if (random.nextFloat() < 0.3f && !creeper.isPowered()) {
                    chargeCreeper(creeper);
                }
                explode();
                return;
            }

            DamageSource damageSource = this.damageSources().thrown(this, owner);
            float damage = getRandomDamage();

            if (livingEntity instanceof Witch) {
                damage *= 0.5f;
            }

            if (livingEntity.isInWaterOrRain()) {
                damage *= 1.5f;
            }

            livingEntity.hurt(damageSource, damage);

            applyShockEffect(livingEntity);

            if (owner instanceof LivingEntity livingOwner) {
                livingEntity.setLastHurtByMob(livingOwner);
            }
        }

        explode();
    }

    private void chargeCreeper(Creeper creeper) {
        if (level() instanceof ServerLevel serverLevel) {
            LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            lightning.moveTo(creeper.getX(), creeper.getY(), creeper.getZ());
            lightning.setVisualOnly(true);
            creeper.thunderHit(serverLevel, lightning);

            level().playSound(null, creeper.getX(), creeper.getY(), creeper.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 0.5F, 1.0F);
        }
    }

    private void applyShockEffect(LivingEntity entity) {
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                40,
                1
        ));

        Vec3 currentMotion = entity.getDeltaMovement();
        entity.setDeltaMovement(currentMotion.scale(0.3));
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
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL,
                    EXPLOSION_VOLUME, EXPLOSION_PITCH);

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT,
                        EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD,
                        0.2);

                serverLevel.sendParticles(ParticleTypes.CRIT,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT / 2,
                        EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD,
                        0.1);
            }

            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner();
    }
}