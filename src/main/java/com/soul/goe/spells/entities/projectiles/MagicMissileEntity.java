package com.soul.goe.spells.entities.projectiles;

import com.soul.goe.spells.config.ProjectileEntity;
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

public class MagicMissileEntity extends Projectile implements ProjectileEntity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MISSILE_ID = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE = 3.5F;
    private static final int DEFAULT_MAX_RANGE = 120;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final float DEFAULT_WAND_STAT = 1.0F;
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
        setCriticalChance(DEFAULT_CRITICAL_CHANCE);
        setIsCriticalCast(DEFAULT_IS_CRITICAL_CAST);
        setWandStats(DEFAULT_WAND_STAT, DEFAULT_WAND_STAT, DEFAULT_WAND_STAT);
        this.startPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(MAX_RANGE, DEFAULT_MAX_RANGE);
        builder.define(MISSILE_ID, 0);
        builder.define(CRITICAL_CHANCE, DEFAULT_CRITICAL_CHANCE);
        builder.define(IS_CRITICAL_CAST, DEFAULT_IS_CRITICAL_CAST);
        builder.define(WAND_POWER, DEFAULT_WAND_STAT);
        builder.define(WAND_STABILITY, DEFAULT_WAND_STAT);
        builder.define(WAND_DURABILITY, DEFAULT_WAND_STAT);
    }

    @Override
    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    @Override
    public void setMaxRange(int range) {
        this.entityData.set(MAX_RANGE, range);
    }

    public int getMaxRange() {
        return this.entityData.get(MAX_RANGE);
    }

    @Override
    public void setCriticalChance(float chance) {
        this.entityData.set(CRITICAL_CHANCE, chance);
    }

    public void setMissileId(int id) {
        this.entityData.set(MISSILE_ID, id);
    }

    public int getMissileId() {
        return this.entityData.get(MISSILE_ID);
    }

    @Override
    public void setIsCriticalCast(boolean isCriticalCast) {
        this.entityData.set(IS_CRITICAL_CAST, isCriticalCast);
    }

    public boolean isCriticalCast() {
        return this.entityData.get(IS_CRITICAL_CAST);
    }

    @Override
    public void setWandStats(float power, float stability, float durability) {
        this.entityData.set(WAND_POWER, power);
        this.entityData.set(WAND_STABILITY, stability);
        this.entityData.set(WAND_DURABILITY, durability);
    }

    private float getWandPower() {
        return this.entityData.get(WAND_POWER);
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

        // Magic Missile ALWAYS hits its target - enhanced homing with stability
        if (targetEntity != null && targetEntity.isAlive()) {
            Vec3 targetPos = targetEntity.getEyePosition();
            Vec3 currentPos = position();
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            // Enhanced homing with power scaling
            double homingStrength = Math.min(0.95, 0.6 + getWandPower() * 0.2);
            Vec3 currentVelocity = getDeltaMovement().normalize();
            Vec3 newDirection = currentVelocity.scale(1.0 - homingStrength)
                    .add(direction.scale(homingStrength)).normalize();

            double speed = getDeltaMovement().length();
            setDeltaMovement(newDirection.scale(speed));

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
            boolean isCrit = isCriticalCast();
            float powerModifier = getWandPower();
            double spread = TRAIL_PARTICLE_SPREAD * Math.max(0.5, powerModifier);

            int particleCount = Math.max(1, Math.round(1 * powerModifier));
            for (int i = 0; i < particleCount; i++) {
                level().addParticle(ParticleTypes.END_ROD,
                        getX() + (random.nextGaussian() * spread),
                        getY() + (random.nextGaussian() * spread),
                        getZ() + (random.nextGaussian() * spread),
                        0, 0, 0);
            }

            if (random.nextFloat() < 0.3f * powerModifier) {
                level().addParticle(ParticleTypes.ENCHANT,
                        getX() + (random.nextGaussian() * spread),
                        getY() + (random.nextGaussian() * spread),
                        getZ() + (random.nextGaussian() * spread),
                        0, 0, 0);
            }

            if (isCrit) {
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        getX() + (random.nextGaussian() * spread * 1.5),
                        getY() + (random.nextGaussian() * spread * 1.5),
                        getZ() + (random.nextGaussian() * spread * 1.5),
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
            float powerModifier = getWandPower();
            boolean isCrit = isCriticalCast();

            float adjustedVolume = Math.min(0.8F, EXPLOSION_VOLUME * powerModifier);
            float adjustedPitch = Math.max(1.2F, EXPLOSION_PITCH * (1.0f + (powerModifier - 1.0f) * 0.1f));

            if (isCrit) {
                adjustedVolume *= 1.2f;
                adjustedPitch *= 1.1f;
            }

            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL,
                    adjustedVolume, adjustedPitch);

            if (isCrit) {
                level().playSound(null, getX(), getY(), getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL,
                        0.3F, 2.0F);
            }

            if (level() instanceof ServerLevel serverLevel) {
                int rodParticles = Math.round(EXPLOSION_PARTICLE_COUNT * powerModifier);
                int enchantParticles = Math.round((EXPLOSION_PARTICLE_COUNT / 2) * powerModifier);

                if (isCrit) {
                    rodParticles = Math.round(rodParticles * 1.4f);
                    enchantParticles = Math.round(enchantParticles * 1.4f);
                }

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        getX(), getY(), getZ(), rodParticles,
                        EXPLOSION_PARTICLE_SPREAD * powerModifier,
                        EXPLOSION_PARTICLE_SPREAD * powerModifier,
                        EXPLOSION_PARTICLE_SPREAD * powerModifier, 0.1);

                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        getX(), getY(), getZ(), enchantParticles,
                        EXPLOSION_PARTICLE_SPREAD * powerModifier,
                        EXPLOSION_PARTICLE_SPREAD * powerModifier,
                        EXPLOSION_PARTICLE_SPREAD * powerModifier, 0.05);

                serverLevel.sendParticles(ParticleTypes.FLASH,
                        getX(), getY(), getZ(), isCrit ? 2 : 1, 0, 0, 0, 0);

                if (isCrit) {
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            getX(), getY(), getZ(), 8,
                            EXPLOSION_PARTICLE_SPREAD * 0.8,
                            EXPLOSION_PARTICLE_SPREAD * 0.8,
                            EXPLOSION_PARTICLE_SPREAD * 0.8, 0.05);
                }
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