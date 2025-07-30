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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MagicMissileEntity extends Projectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MISSILE_ID = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.INT);

    private static final float DEFAULT_DAMAGE = 3.5F;
    private static final int DEFAULT_MAX_RANGE = 120;
    private static final int PARTICLE_INTERVAL = 1;
    private static final double TRAIL_PARTICLE_SPREAD = 0.05;
    private static final int EXPLOSION_PARTICLE_COUNT = 10;
    private static final double EXPLOSION_PARTICLE_SPREAD = 0.3;
    private static final float EXPLOSION_VOLUME = 0.4F;
    private static final float EXPLOSION_PITCH = 1.8F;

    private int ticksInAir = 0;
    private Vec3 startPos;
    private LivingEntity targetEntity;

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setDamage(DEFAULT_DAMAGE);
        setMaxRange(DEFAULT_MAX_RANGE);
        this.startPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(MAX_RANGE, DEFAULT_MAX_RANGE);
        builder.define(MISSILE_ID, 0);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setMaxRange(int range) {
        this.entityData.set(MAX_RANGE, range);
    }

    public int getMaxRange() {
        return this.entityData.get(MAX_RANGE);
    }

    public void setMissileId(int id) {
        this.entityData.set(MISSILE_ID, id);
    }

    public int getMissileId() {
        return this.entityData.get(MISSILE_ID);
    }

    public void setTarget(LivingEntity target) {
        this.targetEntity = target;
    }

    @Override
    public void tick() {
        super.tick();
        ticksInAir++;

        if (startPos != null && distanceToSqr(startPos) > getMaxRange() * getMaxRange()) {
            explode();
            return;
        }

        if (targetEntity != null && targetEntity.isAlive()) {
            Vec3 targetPos = targetEntity.getEyePosition();
            Vec3 currentPos = position();
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            double speed = 0.8;
            setDeltaMovement(direction.scale(speed));

            if (distanceTo(targetEntity) < 1.0) {
                onHitEntity(new EntityHitResult(targetEntity));
                return;
            }
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
            level().addParticle(ParticleTypes.END_ROD,
                    getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD),
                    0, 0, 0);

            if (random.nextFloat() < 0.3f) {
                level().addParticle(ParticleTypes.ENCHANT,
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
            float damage = getDamage();

            livingEntity.hurt(damageSource, damage);

            if (owner instanceof LivingEntity livingOwner) {
                livingEntity.setLastHurtByMob(livingOwner);
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
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL,
                    EXPLOSION_VOLUME, EXPLOSION_PITCH);

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT,
                        EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD,
                        0.1);

                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        getX(), getY(), getZ(), EXPLOSION_PARTICLE_COUNT / 2,
                        EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD, EXPLOSION_PARTICLE_SPREAD,
                        0.05);

                serverLevel.sendParticles(ParticleTypes.FLASH,
                        getX(), getY(), getZ(), 1,
                        0, 0, 0, 0);
            }

            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner() &&
                (targetEntity == null || entity == targetEntity);
    }
}