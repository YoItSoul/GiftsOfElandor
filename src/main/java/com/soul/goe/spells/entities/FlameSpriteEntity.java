package com.soul.goe.spells.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FlameSpriteEntity extends SpriteEntityBase {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FlameSpriteEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(FlameSpriteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_COOLDOWN = SynchedEntityData.defineId(FlameSpriteEntity.class, EntityDataSerializers.INT);

    private static final float DEFAULT_DAMAGE = 1.5F;
    private static final int DEFAULT_DURATION = 2400;
    private static final int DEFAULT_ATTACK_COOLDOWN = 40;
    private static final double ORBIT_RADIUS = 3.0;
    private static final double ORBIT_SPEED = 0.11;
    private static final double DETECTION_RADIUS = 12.0;
    private static final double ATTACK_SPEED = .55;
    private static final double RETURN_SPEED = .55;
    private static final int PARTICLE_INTERVAL = 2;

    private int lifeTicks = 0;
    private double orbitAngle = 0.0;
    private LivingEntity targetEntity;
    private SpriteState state = SpriteState.ORBITING;
    private Vec3 lastOwnerPos;

    public FlameSpriteEntity(EntityType<? extends FlameSpriteEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FlameSpriteEntity(EntityType<? extends FlameSpriteEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level, owner);
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
        orbitAngle += ORBIT_SPEED;
        if (orbitAngle >= 2 * Math.PI) {
            orbitAngle -= 2 * Math.PI;
        }

        double x = owner.getX() + Math.cos(orbitAngle) * ORBIT_RADIUS;
        double y = owner.getY() + 1.5 + Math.sin(orbitAngle * 4) * 0.3;
        double z = owner.getZ() + Math.sin(orbitAngle) * ORBIT_RADIUS;

        Vec3 targetPos = new Vec3(x, y, z);
        Vec3 direction = targetPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(0.32));

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
        if (distanceToTarget < 2.5) {
            attackTarget();
            targetEntity = null;
            state = SpriteState.RETURNING;
        }
    }

    private void handleReturnBehavior(LivingEntity owner) {
        Vec3 ownerPos = new Vec3(owner.getX(), owner.getY() + 1.5, owner.getZ());
        Vec3 direction = ownerPos.subtract(position()).normalize();
        setDeltaMovement(direction.scale(RETURN_SPEED));

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        if (distanceTo(owner) < 2.0) {
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

        if (targetEntity.getType().is(EntityTypeTags.UNDEAD)) {
            damage *= 1.5f;
        }

        float healthBefore = targetEntity.getHealth();
        targetEntity.hurt(damageSource, damage);
        float healthAfter = targetEntity.getHealth();

        boolean damaged = healthAfter < healthBefore;

        if (damaged && targetEntity.isAlive()) {
            targetEntity.setRemainingFireTicks(80);
        }

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                0.4F, 2.2F + (random.nextFloat() - 0.5F) * 0.3F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 15,
                    0.5, 0.5, 0.5, 0.1);

            serverLevel.sendParticles(ParticleTypes.LAVA,
                    targetEntity.getX(), targetEntity.getY() + 0.5, targetEntity.getZ(), 3,
                    0.3, 0.3, 0.3, 0.05);
        }

        setAttackCooldown(DEFAULT_ATTACK_COOLDOWN);
    }

    private void spawnParticles() {
        if (lifeTicks % PARTICLE_INTERVAL == 0) {
            if (level() instanceof ServerLevel serverLevel) {
                SimpleParticleType particleType = (state == SpriteState.SEEKING_FUSION) ?
                        ParticleTypes.ENCHANT : ParticleTypes.FLAME;

                serverLevel.sendParticles(particleType,
                        getX(), getY(), getZ(), 1,
                        0.1, 0.1, 0.1, 0.02);

                if (random.nextFloat() < 0.3f) {
                    serverLevel.sendParticles(ParticleTypes.SMALL_FLAME,
                            getX(), getY(), getZ(), 1,
                            0.05, 0.05, 0.05, 0.01);
                }
            } else {
                level().addParticle(ParticleTypes.FLAME,
                        getX() + (random.nextGaussian() * 0.1),
                        getY() + (random.nextGaussian() * 0.1),
                        getZ() + (random.nextGaussian() * 0.1),
                        0, 0, 0);
            }
        }
    }

    private void despawnWithEffect() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                0.8F, 1.5F + (random.nextFloat() - 0.5F) * 0.2F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    getX(), getY(), getZ(), 20,
                    0.5, 0.5, 0.5, 0.1);

            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    getX(), getY(), getZ(), 5,
                    0.2, 0.2, 0.2, 0.05);
        }

        discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }
}