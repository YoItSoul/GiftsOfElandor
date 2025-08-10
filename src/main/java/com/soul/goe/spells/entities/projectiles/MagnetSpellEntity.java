package com.soul.goe.spells.entities.projectiles;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MagnetSpellEntity extends Projectile {
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(MagnetSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MAGNET_RADIUS = SynchedEntityData.defineId(MagnetSpellEntity.class, EntityDataSerializers.FLOAT);

    private static final int DEFAULT_MAX_RANGE = 25;
    private static final float DEFAULT_MAGNET_RADIUS = 10.0F;
    private static final int PARTICLE_INTERVAL = 2;
    private static final double TRAIL_PARTICLE_SPREAD = 0.3;
    private static final float MAGNET_FORCE = 0.6F;
    private static final float ATTACH_DISTANCE = 10F;

    private int ticksInAir = 0;
    private Vec3 startPos;
    private List<Projectile> attachedProjectiles = new ArrayList<>();

    public MagnetSpellEntity(EntityType<? extends MagnetSpellEntity> entityType, Level level) {
        super(entityType, level);
    }

    public MagnetSpellEntity(EntityType<? extends MagnetSpellEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setMaxRange(DEFAULT_MAX_RANGE);
        setMagnetRadius(DEFAULT_MAGNET_RADIUS);
        this.startPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MAX_RANGE, DEFAULT_MAX_RANGE);
        builder.define(MAGNET_RADIUS, DEFAULT_MAGNET_RADIUS);
    }

    public void setMaxRange(int range) {
        this.entityData.set(MAX_RANGE, range);
    }

    public int getMaxRange() {
        return this.entityData.get(MAX_RANGE);
    }

    public void setMagnetRadius(float radius) {
        this.entityData.set(MAGNET_RADIUS, radius);
    }

    public float getMagnetRadius() {
        return this.entityData.get(MAGNET_RADIUS);
    }

    @Override
    public void tick() {
        super.tick();
        ticksInAir++;

        if (startPos != null && distanceToSqr(startPos) > getMaxRange() * getMaxRange()) {
            dissipate();
            return;
        }

        if (this.level().isClientSide()) {
            spawnTrailParticles();
        } else {
            magnetizeNearbyProjectiles();
            moveAttachedProjectiles();

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

            if (!attachedProjectiles.isEmpty()) {
                level().addParticle(ParticleTypes.END_ROD,
                        getX() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD * 2),
                        getY() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD * 2),
                        getZ() + (random.nextGaussian() * TRAIL_PARTICLE_SPREAD * 2),
                        0, 0, 0);
            }
        }
    }

    private void magnetizeNearbyProjectiles() {
        float radius = getMagnetRadius();
        AABB searchArea = new AABB(position().subtract(radius, radius, radius),
                position().add(radius, radius, radius));

        List<Projectile> nearbyProjectiles = level().getEntitiesOfClass(Projectile.class, searchArea);

        for (Projectile projectile : nearbyProjectiles) {
            if (projectile != this && projectile.getOwner() != getOwner() && !attachedProjectiles.contains(projectile)) {
                attractProjectile(projectile);
            }
        }
    }

    private void attractProjectile(Projectile projectile) {
        Vec3 projectilePos = projectile.position();
        Vec3 magnetPos = position();
        Vec3 direction = magnetPos.subtract(projectilePos).normalize();

        Vec3 currentVelocity = projectile.getDeltaMovement();
        Vec3 magnetForce = direction.scale(MAGNET_FORCE);

        projectile.setDeltaMovement(currentVelocity.add(magnetForce));

        double distance = projectilePos.distanceTo(magnetPos);
        if (distance < ATTACH_DISTANCE) {
            attachProjectile(projectile);
        }
    }

    private void attachProjectile(Projectile projectile) {
        if (!attachedProjectiles.contains(projectile)) {
            attachedProjectiles.add(projectile);
            projectile.setDeltaMovement(Vec3.ZERO);

            level().playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL,
                    0.4F, 1.8F);

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        projectile.getX(), projectile.getY(), projectile.getZ(), 8,
                        0.2, 0.2, 0.2, 0.1);
            }
        }
    }

    private void moveAttachedProjectiles() {
        attachedProjectiles.removeIf(projectile -> !projectile.isAlive());

        for (Projectile projectile : attachedProjectiles) {
            Vec3 offset = new Vec3(
                    (random.nextFloat() - 0.5F) * 0.4F,
                    (random.nextFloat() - 0.5F) * 0.4F,
                    (random.nextFloat() - 0.5F) * 0.4F
            );
            Vec3 targetPos = position().add(offset);

            projectile.setPos(targetPos.x, targetPos.y, targetPos.z);
            projectile.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            dissipate();
        }
    }

    private void dissipate() {
        if (!level().isClientSide()) {
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL,
                    0.8F, 0.6F);

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        getX(), getY(), getZ(), 25,
                        0.6, 0.6, 0.6, 0.3);

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        getX(), getY(), getZ(), 15,
                        0.4, 0.4, 0.4, 0.15);
            }

            for (Projectile projectile : attachedProjectiles) {
                if (projectile.isAlive()) {
                    projectile.discard();
                }
            }

            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner();
    }
}