package com.soul.goe.spells.entities.summons;

import com.soul.goe.spells.config.SummonableEntity;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DragonSpriteEntity extends Projectile implements SummonableEntity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_COOLDOWN = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_DAMAGE_MULTIPLIER = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_DURATION_MULTIPLIER = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE = 4.5F;
    private static final int DEFAULT_DURATION = 7200;
    private static final int DEFAULT_ATTACK_COOLDOWN = 20;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final float DEFAULT_CRITICAL_DAMAGE_MULTIPLIER = 2.5F;
    private static final float DEFAULT_CRITICAL_DURATION_MULTIPLIER = 1.8F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final float DEFAULT_WAND_STAT = 1.0F;

    private static final double BASE_ORBIT_RADIUS = 3.5;
    private static final double BASE_ORBIT_SPEED = 0.08;
    private static final double BASE_DETECTION_RADIUS = 20;
    private static final double BASE_ATTACK_SPEED = 0.45;
    private static final double BASE_RETURN_SPEED = 0.45;
    private static final int PARTICLE_INTERVAL = 1;
    private static final float MAX_STABILITY_MISS_CHANCE = 0.35f;

    private int lifeTicks = 0;
    private double orbitAngle = 0.0;
    private LivingEntity targetEntity;
    private SpriteState state = SpriteState.ORBITING;
    private Vec3 lastOwnerPos;

    private enum SpriteState {
        ORBITING,
        ATTACKING,
        RETURNING
    }

    public DragonSpriteEntity(EntityType<? extends DragonSpriteEntity> entityType, Level level) {
        super(entityType, level);
    }

    public DragonSpriteEntity(EntityType<? extends DragonSpriteEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setDamage(DEFAULT_DAMAGE);
        setDuration(DEFAULT_DURATION);
        setAttackCooldown(0);
        setCriticalChance(DEFAULT_CRITICAL_CHANCE);
        setCriticalMultipliers(DEFAULT_CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_DURATION_MULTIPLIER);
        setIsCriticalCast(DEFAULT_IS_CRITICAL_CAST);
        setWandStats(DEFAULT_WAND_STAT, DEFAULT_WAND_STAT, DEFAULT_WAND_STAT);
        this.orbitAngle = Math.random() * 2 * Math.PI;
        this.lastOwnerPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(DURATION, DEFAULT_DURATION);
        builder.define(ATTACK_COOLDOWN, DEFAULT_ATTACK_COOLDOWN);
        builder.define(CRITICAL_CHANCE, DEFAULT_CRITICAL_CHANCE);
        builder.define(CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_DAMAGE_MULTIPLIER);
        builder.define(CRITICAL_DURATION_MULTIPLIER, DEFAULT_CRITICAL_DURATION_MULTIPLIER);
        builder.define(IS_CRITICAL_CAST, DEFAULT_IS_CRITICAL_CAST);
        builder.define(WAND_POWER, DEFAULT_WAND_STAT);
        builder.define(WAND_STABILITY, DEFAULT_WAND_STAT);
        builder.define(WAND_DURABILITY, DEFAULT_WAND_STAT);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setDuration(int duration) {
        this.entityData.set(DURATION, duration);
    }

    public int getDuration() {
        return this.entityData.get(DURATION);
    }

    public void setAttackCooldown(int cooldown) {
        this.entityData.set(ATTACK_COOLDOWN, cooldown);
    }

    public int getAttackCooldown() {
        return this.entityData.get(ATTACK_COOLDOWN);
    }

    public void setCriticalChance(float chance) {
        this.entityData.set(CRITICAL_CHANCE, chance);
    }

    public void setCriticalMultipliers(float damageMultiplier, float durationMultiplier) {
        this.entityData.set(CRITICAL_DAMAGE_MULTIPLIER, damageMultiplier);
        this.entityData.set(CRITICAL_DURATION_MULTIPLIER, durationMultiplier);
    }

    public void setIsCriticalCast(boolean isCriticalCast) {
        this.entityData.set(IS_CRITICAL_CAST, isCriticalCast);
    }

    public boolean isCriticalCast() {
        return this.entityData.get(IS_CRITICAL_CAST);
    }

    public void setWandStats(float power, float stability, float durability) {
        this.entityData.set(WAND_POWER, power);
        this.entityData.set(WAND_STABILITY, stability);
        this.entityData.set(WAND_DURABILITY, durability);
    }

    private float getWandPower() {
        return this.entityData.get(WAND_POWER);
    }

    private float getWandStability() {
        return this.entityData.get(WAND_STABILITY);
    }

    private float getWandDurability() {
        return this.entityData.get(WAND_DURABILITY);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (lifeTicks >= getDuration()) {
            despawnWithEffect();
            return;
        }

        LivingEntity owner = (LivingEntity) getOwner();
        if (owner == null || !owner.isAlive()) {
            discard();
            return;
        }

        if (getAttackCooldown() > 0) {
            setAttackCooldown(getAttackCooldown() - 1);
        }

        lastOwnerPos = owner.position();

        switch (state) {
            case ORBITING:
                handleOrbitBehavior(owner);
                break;
            case ATTACKING:
                handleAttackBehavior(owner);
                break;
            case RETURNING:
                handleReturnBehavior(owner);
                break;
        }

        spawnParticles();
    }

    private void handleOrbitBehavior(LivingEntity owner) {
        float powerModifier = getWandPower();
        double adjustedOrbitSpeed = BASE_ORBIT_SPEED * powerModifier;
        double adjustedOrbitRadius = BASE_ORBIT_RADIUS * Math.max(0.8, powerModifier);

        orbitAngle += adjustedOrbitSpeed;
        if (orbitAngle >= 2 * Math.PI) {
            orbitAngle -= 2 * Math.PI;
        }

        double x = owner.getX() + Math.cos(orbitAngle) * adjustedOrbitRadius;
        double y = owner.getY() + 2.0 + Math.sin(orbitAngle * 2) * 0.6;
        double z = owner.getZ() + Math.sin(orbitAngle) * adjustedOrbitRadius;

        Vec3 targetPos = new Vec3(x, y, z);
        Vec3 direction = targetPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(0.25 * powerModifier));

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        if (getAttackCooldown() <= 0) {
            LivingEntity nearbyEnemy = findNearbyEnemy(owner);
            if (nearbyEnemy != null) {
                targetEntity = nearbyEnemy;
                state = SpriteState.ATTACKING;
            }
        }
    }

    private void handleAttackBehavior(LivingEntity owner) {
        float durabilityModifier = getWandDurability();
        double adjustedDetectionRadius = BASE_DETECTION_RADIUS * durabilityModifier;

        if (targetEntity == null || !targetEntity.isAlive() ||
                targetEntity.distanceTo(owner) > adjustedDetectionRadius * 1.5) {
            targetEntity = null;
            state = SpriteState.RETURNING;
            return;
        }

        float powerModifier = getWandPower();
        Vec3 targetPos = targetEntity.getEyePosition();
        Vec3 direction = targetPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(BASE_ATTACK_SPEED * powerModifier));

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        double distanceToTarget = distanceTo(targetEntity);
        if (distanceToTarget < 3.0) {
            attackTarget();
            targetEntity = null;
            state = SpriteState.RETURNING;
        }
    }

    private void handleReturnBehavior(LivingEntity owner) {
        float powerModifier = getWandPower();
        Vec3 ownerPos = new Vec3(owner.getX(), owner.getY() + 2.0, owner.getZ());
        Vec3 direction = ownerPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(BASE_RETURN_SPEED * powerModifier));

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        if (distanceTo(owner) < 2.5) {
            state = SpriteState.ORBITING;
        }
    }

    private LivingEntity findNearbyEnemy(LivingEntity owner) {
        float durabilityModifier = getWandDurability();
        double adjustedDetectionRadius = BASE_DETECTION_RADIUS * durabilityModifier;

        AABB searchArea = new AABB(owner.position(), owner.position()).inflate(adjustedDetectionRadius);
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, searchArea);

        LivingEntity closestEnemy = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            if (entity == owner || !(entity instanceof Monster)) continue;
            if (!owner.hasLineOfSight(entity)) continue;

            double distance = entity.distanceTo(owner);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEnemy = entity;
            }
        }

        return closestEnemy;
    }

    private void attackTarget() {
        if (targetEntity == null) return;

        float stabilityModifier = getWandStability();
        float missChance = Math.max(0.0f, (1.0f - stabilityModifier) * MAX_STABILITY_MISS_CHANCE);

        if (random.nextFloat() < missChance) {
            playMissEffects();
            setAttackCooldown(DEFAULT_ATTACK_COOLDOWN / 2);
            return;
        }

        LivingEntity owner = (LivingEntity) getOwner();
        DamageSource damageSource = this.damageSources().thrown(this, owner);

        float criticalChance = this.entityData.get(CRITICAL_CHANCE);
        boolean isCriticalHit = random.nextFloat() < criticalChance;
        float damage = getDamage();

        if (isCriticalHit) {
            float criticalMultiplier = this.entityData.get(CRITICAL_DAMAGE_MULTIPLIER);
            damage *= criticalMultiplier;
        }

        float healthBefore = targetEntity.getHealth();
        targetEntity.hurt(damageSource, damage);
        float healthAfter = targetEntity.getHealth();

        boolean damaged = healthAfter < healthBefore;

        if (damaged && targetEntity.isAlive()) {
            applyAllDebuffs(targetEntity, isCriticalHit);
        }

        if (isCriticalHit) {
            spawnCriticalHitEffects();
        } else {
            spawnNormalHitEffects();
        }

        setAttackCooldown(DEFAULT_ATTACK_COOLDOWN);
    }

    private void playMissEffects() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                0.3F, 1.2F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    getX(), getY(), getZ(), 8,
                    0.4, 0.4, 0.4, 0.05);
        }
    }

    private void spawnNormalHitEffects() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL,
                0.3F, 1.8F + (random.nextFloat() - 0.5F) * 0.4F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 25,
                    0.8, 0.8, 0.8, 0.1);

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 10,
                    0.5, 0.5, 0.5, 0.08);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 15,
                    0.5, 0.5, 0.5, 0.1);

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 10,
                    0.5, 0.5, 0.5, 0.1);
        }
    }

    private void spawnCriticalHitEffects() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL,
                0.5F, 2.0F);

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL,
                0.4F, 1.5F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 50,
                    1.2, 1.2, 1.2, 0.15);

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 20,
                    0.8, 0.8, 0.8, 0.12);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 30,
                    0.8, 0.8, 0.8, 0.15);

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 20,
                    0.8, 0.8, 0.8, 0.15);

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 15,
                    0.6, 0.6, 0.6, 0.1);

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 12,
                    0.4, 0.4, 0.4, 0.08);
        }
    }

    private void applyAllDebuffs(LivingEntity entity, boolean isCriticalHit) {
        int fireDuration = isCriticalHit ? 180 : 120;
        int debuffDuration = isCriticalHit ? 180 : 120;
        int debuffAmplifier = isCriticalHit ? 2 : 1;

        entity.setRemainingFireTicks(fireDuration);

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                debuffDuration,
                debuffAmplifier
        ));

        applyFrostSlowEffect(entity, isCriticalHit);
        applyShockEffect(entity, isCriticalHit);

        if (isCriticalHit) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WITHER,
                    debuffDuration / 2,
                    0
            ));
        }
    }

    private void applyFrostSlowEffect(LivingEntity entity, boolean isCriticalHit) {
        float slowdownMultiplier = isCriticalHit ? 0.3f : 0.5f;
        int slowdownAmplifier = isCriticalHit ? 3 : 2;
        int slowdownDuration = isCriticalHit ? 180 : 120;

        Vec3 currentMotion = entity.getDeltaMovement();
        entity.setDeltaMovement(currentMotion.scale(slowdownMultiplier));

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                slowdownDuration,
                slowdownAmplifier
        ));
    }

    private void applyShockEffect(LivingEntity entity, boolean isCriticalHit) {
        float shockMultiplier = isCriticalHit ? 0.2f : 0.3f;
        Vec3 currentMotion = entity.getDeltaMovement();
        entity.setDeltaMovement(currentMotion.scale(shockMultiplier));
    }

    private void spawnParticles() {
        if (lifeTicks % PARTICLE_INTERVAL == 0) {
            boolean isCrit = isCriticalCast();
            float powerModifier = getWandPower();

            if (level() instanceof ServerLevel serverLevel) {
                int particleCount = Math.max(1, Math.round(2 * powerModifier));
                double spread = 0.15 * Math.max(0.5, powerModifier);

                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                        getX(), getY(), getZ(), particleCount,
                        spread, spread, spread, 0.02);

                if (random.nextFloat() < 0.5f * powerModifier) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            getX(), getY(), getZ(), 1,
                            0.08, 0.08, 0.08, 0.01);
                }

                if (random.nextFloat() < 0.3f * powerModifier) {
                    serverLevel.sendParticles(ParticleTypes.SMALL_FLAME,
                            getX(), getY(), getZ(), 1,
                            0.05, 0.05, 0.05, 0.01);
                }

                if (random.nextFloat() < 0.2f * powerModifier) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            getX(), getY(), getZ(), 1,
                            0.05, 0.05, 0.05, 0.01);
                }

                if (random.nextFloat() < 0.2f * powerModifier) {
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            getX(), getY(), getZ(), 1,
                            0.05, 0.05, 0.05, 0.01);
                }

                if (isCrit) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            getX(), getY(), getZ(), 1,
                            0.1, 0.1, 0.1, 0.02);
                }
            } else {
                if (isCrit) {
                    level().addParticle(ParticleTypes.PORTAL,
                            getX() + (random.nextGaussian() * 0.15),
                            getY() + (random.nextGaussian() * 0.15),
                            getZ() + (random.nextGaussian() * 0.15),
                            0, 0, 0);
                } else {
                    level().addParticle(ParticleTypes.DRAGON_BREATH,
                            getX() + (random.nextGaussian() * 0.15),
                            getY() + (random.nextGaussian() * 0.15),
                            getZ() + (random.nextGaussian() * 0.15),
                            0, 0, 0);
                }
            }
        }
    }

    private void despawnWithEffect() {
        float powerModifier = getWandPower();

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.ENDER_DRAGON_DEATH, SoundSource.NEUTRAL,
                Math.min(1.0F, 0.4F * powerModifier), 2.0F + (random.nextFloat() - 0.5F) * 0.3F);

        if (level() instanceof ServerLevel serverLevel) {
            int particleMultiplier = Math.round(powerModifier);

            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    getX(), getY(), getZ(), 40 * particleMultiplier,
                    0.8 * powerModifier, 0.8 * powerModifier, 0.8 * powerModifier, 0.15);

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    getX(), getY(), getZ(), 12 * particleMultiplier,
                    0.4, 0.4, 0.4, 0.08);

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    getX(), getY(), getZ(), 8 * particleMultiplier,
                    0.3, 0.3, 0.3, 0.1);

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    getX(), getY(), getZ(), 15 * particleMultiplier,
                    0.4, 0.4, 0.4, 0.1);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    getX(), getY(), getZ(), 15 * particleMultiplier,
                    0.4, 0.4, 0.4, 0.1);

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    getX(), getY(), getZ(), 15 * particleMultiplier,
                    0.4, 0.4, 0.4, 0.1);

            if (isCriticalCast()) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        getX(), getY(), getZ(), 3,
                        0.5, 0.5, 0.5, 0);
            }
        }

        discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }
}