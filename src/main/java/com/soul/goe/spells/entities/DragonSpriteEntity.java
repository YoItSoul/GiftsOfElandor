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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DragonSpriteEntity extends Projectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_COOLDOWN = SynchedEntityData.defineId(DragonSpriteEntity.class, EntityDataSerializers.INT);

    private static final float DEFAULT_DAMAGE = 4.5F;
    private static final int DEFAULT_DURATION = 7200;
    private static final int DEFAULT_ATTACK_COOLDOWN = 20;
    private static final double ORBIT_RADIUS = 3.5;
    private static final double ORBIT_SPEED = 0.08;
    private static final double DETECTION_RADIUS = 20;
    private static final double ATTACK_SPEED = .45;
    private static final double RETURN_SPEED = .45;
    private static final int PARTICLE_INTERVAL = 1;

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
        this.orbitAngle = Math.random() * 2 * Math.PI;
        this.lastOwnerPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(DURATION, DEFAULT_DURATION);
        builder.define(ATTACK_COOLDOWN, DEFAULT_ATTACK_COOLDOWN);
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
        orbitAngle += ORBIT_SPEED;
        if (orbitAngle >= 2 * Math.PI) {
            orbitAngle -= 2 * Math.PI;
        }

        double x = owner.getX() + Math.cos(orbitAngle) * ORBIT_RADIUS;
        double y = owner.getY() + 2.0 + Math.sin(orbitAngle * 2) * 0.6;
        double z = owner.getZ() + Math.sin(orbitAngle) * ORBIT_RADIUS;

        Vec3 targetPos = new Vec3(x, y, z);
        Vec3 direction = targetPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(0.25));

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
        if (targetEntity == null || !targetEntity.isAlive() ||
                targetEntity.distanceTo(owner) > DETECTION_RADIUS * 1.5) {
            targetEntity = null;
            state = SpriteState.RETURNING;
            return;
        }

        Vec3 targetPos = targetEntity.getEyePosition();
        Vec3 direction = targetPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(ATTACK_SPEED));

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        double distanceToTarget = distanceTo(targetEntity);
        if (distanceToTarget < 3.0) {
            attackTarget();
            targetEntity = null;
            state = SpriteState.RETURNING;
        }
    }

    private void handleReturnBehavior(LivingEntity owner) {
        Vec3 ownerPos = new Vec3(owner.getX(), owner.getY() + 2.0, owner.getZ());
        Vec3 direction = ownerPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(RETURN_SPEED));

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        if (distanceTo(owner) < 2.5) {
            state = SpriteState.ORBITING;
        }
    }

    private LivingEntity findNearbyEnemy(LivingEntity owner) {
        AABB searchArea = new AABB(owner.position(), owner.position()).inflate(DETECTION_RADIUS);
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

        LivingEntity owner = (LivingEntity) getOwner();
        DamageSource damageSource = this.damageSources().thrown(this, owner);

        float damage = getDamage();

        boolean isCritical = random.nextFloat() < 0.05f;
        if (isCritical) {
            damage *= 2;
        }

        float healthBefore = targetEntity.getHealth();
        targetEntity.hurt(damageSource, damage);
        float healthAfter = targetEntity.getHealth();

        boolean damaged = healthAfter < healthBefore;

        if (damaged && targetEntity.isAlive()) {
            applyAllDebuffs(targetEntity);
        }

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL,
                0.3F, 1.8F + (random.nextFloat() - 0.5F) * 0.4F);

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                0.5F, 0.8F + (random.nextFloat() - 0.5F) * 0.3F);

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

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 8,
                    0.3, 0.3, 0.3, 0.05);
        }

        setAttackCooldown(DEFAULT_ATTACK_COOLDOWN);
    }

    private void applyAllDebuffs(LivingEntity entity) {
        entity.setRemainingFireTicks(120);

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                120,
                1
        ));

        applyFrostSlowEffect(entity);
        applyShockEffect(entity);
    }

    private void applyFrostSlowEffect(LivingEntity entity) {
        Vec3 currentMotion = entity.getDeltaMovement();
        entity.setDeltaMovement(currentMotion.scale(0.5));

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                120,
                2
        ));
    }

    private void applyShockEffect(LivingEntity entity) {
        Vec3 currentMotion = entity.getDeltaMovement();
        entity.setDeltaMovement(currentMotion.scale(0.3));
    }

    private void spawnParticles() {
        if (lifeTicks % PARTICLE_INTERVAL == 0) {
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                        getX(), getY(), getZ(), 2,
                        0.15, 0.15, 0.15, 0.02);

                if (random.nextFloat() < 0.5f) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            getX(), getY(), getZ(), 1,
                            0.08, 0.08, 0.08, 0.01);
                }

                if (random.nextFloat() < 0.3f) {
                    serverLevel.sendParticles(ParticleTypes.SMALL_FLAME,
                            getX(), getY(), getZ(), 1,
                            0.05, 0.05, 0.05, 0.01);
                }

                if (random.nextFloat() < 0.2f) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            getX(), getY(), getZ(), 1,
                            0.05, 0.05, 0.05, 0.01);
                }

                if (random.nextFloat() < 0.2f) {
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            getX(), getY(), getZ(), 1,
                            0.05, 0.05, 0.05, 0.01);
                }
            } else {
                level().addParticle(ParticleTypes.DRAGON_BREATH,
                        getX() + (random.nextGaussian() * 0.15),
                        getY() + (random.nextGaussian() * 0.15),
                        getZ() + (random.nextGaussian() * 0.15),
                        0, 0, 0);
            }
        }
    }

    private void despawnWithEffect() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.ENDER_DRAGON_DEATH, SoundSource.NEUTRAL,
                0.4F, 2.0F + (random.nextFloat() - 0.5F) * 0.3F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    getX(), getY(), getZ(), 40,
                    0.8, 0.8, 0.8, 0.15);

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    getX(), getY(), getZ(), 12,
                    0.4, 0.4, 0.4, 0.08);

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    getX(), getY(), getZ(), 8,
                    0.3, 0.3, 0.3, 0.1);

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    getX(), getY(), getZ(), 15,
                    0.4, 0.4, 0.4, 0.1);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    getX(), getY(), getZ(), 15,
                    0.4, 0.4, 0.4, 0.1);

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    getX(), getY(), getZ(), 15,
                    0.4, 0.4, 0.4, 0.1);
        }

        discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }
}