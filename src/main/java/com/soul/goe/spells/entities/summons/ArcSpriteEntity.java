package com.soul.goe.spells.entities.summons;

import com.soul.goe.spells.config.SummonableEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ArcSpriteEntity extends SpriteEntityBase implements SummonableEntity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_COOLDOWN = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_DAMAGE_MULTIPLIER = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_DURATION_MULTIPLIER = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(ArcSpriteEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE = 1.5F;
    private static final int DEFAULT_DURATION = 2400;
    private static final int DEFAULT_ATTACK_COOLDOWN = 40;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final float DEFAULT_CRITICAL_DAMAGE_MULTIPLIER = 1.5F;
    private static final float DEFAULT_CRITICAL_DURATION_MULTIPLIER = 1.3F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final float DEFAULT_WAND_STAT = 1.0F;

    private static final double BASE_ORBIT_RADIUS = 3.0;
    private static final double BASE_ORBIT_SPEED = 0.12;
    private static final double BASE_DETECTION_RADIUS = 12.0;
    private static final double BASE_ATTACK_SPEED = 0.6;
    private static final double BASE_RETURN_SPEED = 0.6;
    private static final int PARTICLE_INTERVAL = 2;
    private static final float MAX_STABILITY_MISS_CHANCE = 0.3f;

    private int lifeTicks = 0;
    private double orbitAngle = 0.0;
    private LivingEntity targetEntity;
    private SpriteState state = SpriteState.ORBITING;
    private Vec3 lastOwnerPos;

    public ArcSpriteEntity(EntityType<? extends ArcSpriteEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ArcSpriteEntity(EntityType<? extends ArcSpriteEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level, owner);
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

    @Override
    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    @Override
    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    @Override
    public void setDuration(int duration) {
        this.entityData.set(DURATION, duration);
    }

    @Override
    public int getDuration() {
        return this.entityData.get(DURATION);
    }

    @Override
    public void setAttackCooldown(int cooldown) {
        this.entityData.set(ATTACK_COOLDOWN, cooldown);
    }

    @Override
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
    protected void setState(SpriteState newState) {
        this.state = newState;
    }

    @Override
    protected SpriteState getState() {
        return this.state;
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

        if (shouldSeekFusion() && state != SpriteState.SEEKING_FUSION) {
            state = SpriteState.SEEKING_FUSION;
        }

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
            case SEEKING_FUSION:
                handleFusionSeeking(owner);
                break;
        }

        spawnParticles();
    }

    private void handleOrbitBehavior(LivingEntity owner) {
        float powerModifier = getWandPower();
        double adjustedOrbitSpeed = BASE_ORBIT_SPEED * powerModifier;
        double adjustedOrbitRadius = BASE_ORBIT_RADIUS * Math.max(0.7, powerModifier);

        orbitAngle += adjustedOrbitSpeed;
        if (orbitAngle >= 2 * Math.PI) {
            orbitAngle -= 2 * Math.PI;
        }

        double x = owner.getX() + Math.cos(orbitAngle) * adjustedOrbitRadius;
        double y = owner.getY() + 1.5 + Math.sin(orbitAngle * 2.5) * 0.4;
        double z = owner.getZ() + Math.sin(orbitAngle) * adjustedOrbitRadius;

        Vec3 targetPos = new Vec3(x, y, z);
        Vec3 direction = targetPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(0.35 * powerModifier));

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        if (getAttackCooldown() <= 0 && !shouldSeekFusion()) {
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
        if (distanceToTarget < 2.5) {
            attackTarget();
            targetEntity = null;
            state = SpriteState.RETURNING;
        }
    }

    private void handleReturnBehavior(LivingEntity owner) {
        float powerModifier = getWandPower();
        Vec3 ownerPos = new Vec3(owner.getX(), owner.getY() + 1.5, owner.getZ());
        Vec3 direction = ownerPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(BASE_RETURN_SPEED * powerModifier));

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        if (distanceTo(owner) < 2.0) {
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

        if (targetEntity instanceof Creeper creeper) {
            float powerModifier = getWandPower();
            float chargeChance = Math.min(0.6f, 0.3f * powerModifier);
            if (random.nextFloat() < chargeChance && !creeper.isPowered()) {
                chargeCreeper(creeper);
            }
        }

        if (targetEntity instanceof Witch) {
            damage *= 0.5f;
        }

        if (targetEntity.isInWaterOrRain()) {
            damage *= 1.5f;
        }

        float healthBefore = targetEntity.getHealth();
        targetEntity.hurt(damageSource, damage);
        float healthAfter = targetEntity.getHealth();

        boolean damaged = healthAfter < healthBefore;

        if (damaged && targetEntity.isAlive()) {
            applyShockEffect(targetEntity, isCriticalHit);
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
                0.2F, 1.5F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    getX(), getY(), getZ(), 5,
                    0.3, 0.3, 0.3, 0.05);
        }
    }

    private void spawnNormalHitEffects() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL,
                0.3F, 2.0F + (random.nextFloat() - 0.5F) * 0.3F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 20,
                    0.5, 0.5, 0.5, 0.1);

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 8,
                    0.3, 0.3, 0.3, 0.05);
        }
    }

    private void spawnCriticalHitEffects() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL,
                0.5F, 2.2F);

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL,
                0.4F, 1.8F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 35,
                    0.7, 0.7, 0.7, 0.15);

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 15,
                    0.5, 0.5, 0.5, 0.08);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 10,
                    0.4, 0.4, 0.4, 0.05);
        }
    }

    private void chargeCreeper(Creeper creeper) {
        if (level() instanceof ServerLevel serverLevel) {
            LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            lightning.moveTo(creeper.getX(), creeper.getY(), creeper.getZ());
            lightning.setVisualOnly(true);
            creeper.thunderHit(serverLevel, lightning);

            level().playSound(null, creeper.getX(), creeper.getY(), creeper.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE,
                    0.5F, 1.0F + (random.nextFloat() - 0.5F) * 0.2F);
        }
    }

    private void applyShockEffect(LivingEntity entity, boolean isCriticalHit) {
        int weaknessAmplifier = isCriticalHit ? 2 : 1;
        int weaknessDuration = isCriticalHit ? 60 : 40;

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                weaknessDuration,
                weaknessAmplifier
        ));

        Vec3 currentMotion = entity.getDeltaMovement();
        float slowdownMultiplier = isCriticalHit ? 0.1f : 0.3f;
        entity.setDeltaMovement(currentMotion.scale(slowdownMultiplier));
    }

    private void spawnParticles() {
        if (lifeTicks % PARTICLE_INTERVAL == 0) {
            boolean isCrit = isCriticalCast();
            float powerModifier = getWandPower();

            if (level() instanceof ServerLevel serverLevel) {
                SimpleParticleType primaryParticle = (state == SpriteState.SEEKING_FUSION) ?
                        ParticleTypes.ENCHANT : ParticleTypes.ELECTRIC_SPARK;

                int particleCount = Math.round(1 * powerModifier);
                double spread = 0.1 * Math.max(0.5, powerModifier);

                serverLevel.sendParticles(primaryParticle,
                        getX(), getY(), getZ(), particleCount,
                        spread, spread, spread, 0.02);

                if (random.nextFloat() < 0.4f * powerModifier) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            getX(), getY(), getZ(), 1,
                            0.05, 0.05, 0.05, 0.01);
                }

                if (isCrit) {
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            getX(), getY(), getZ(), 1,
                            0.08, 0.08, 0.08, 0.01);
                }
            } else {
                SimpleParticleType trailParticle = isCrit ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.ELECTRIC_SPARK;
                level().addParticle(trailParticle,
                        getX() + (random.nextGaussian() * 0.1),
                        getY() + (random.nextGaussian() * 0.1),
                        getZ() + (random.nextGaussian() * 0.1),
                        0, 0, 0);
            }
        }
    }

    private void despawnWithEffect() {
        float powerModifier = getWandPower();

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.NEUTRAL,
                Math.min(1.0F, 0.6F * powerModifier), 1.8F + (random.nextFloat() - 0.5F) * 0.2F);

        if (level() instanceof ServerLevel serverLevel) {
            int sparkCount = Math.round(25 * powerModifier);
            int rodCount = Math.round(8 * powerModifier);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    getX(), getY(), getZ(), sparkCount,
                    0.5 * powerModifier, 0.5 * powerModifier, 0.5 * powerModifier, 0.1);

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    getX(), getY(), getZ(), rodCount,
                    0.2, 0.2, 0.2, 0.05);

            if (isCriticalCast()) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        getX(), getY(), getZ(), 12,
                        0.3, 0.3, 0.3, 0.05);
            }
        }

        discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }
}