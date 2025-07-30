package com.soul.goe.spells.entities;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ConeOfColdEntity extends Projectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CONE_RANGE = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CONE_WIDTH = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE = 28.0F;
    private static final int DEFAULT_CONE_RANGE = 15;
    private static final float DEFAULT_CONE_WIDTH = 60.0F;
    private static final int DURATION_TICKS = 20;
    private static final int PARTICLE_DENSITY = 5;
    private static final float EXPLOSION_VOLUME = 1.5F;
    private static final float EXPLOSION_PITCH = 0.7F;

    private int activeTicks = 0;
    private Vec3 coneDirection;
    private Vec3 coneOrigin;

    public ConeOfColdEntity(EntityType<? extends ConeOfColdEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ConeOfColdEntity(EntityType<? extends ConeOfColdEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setDamage(DEFAULT_DAMAGE);
        setConeRange(DEFAULT_CONE_RANGE);
        setConeWidth(DEFAULT_CONE_WIDTH);
        this.coneDirection = owner.getLookAngle();
        this.coneOrigin = owner.getEyePosition();
        setPos(coneOrigin.x, coneOrigin.y, coneOrigin.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(CONE_RANGE, DEFAULT_CONE_RANGE);
        builder.define(CONE_WIDTH, DEFAULT_CONE_WIDTH);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setConeRange(int range) {
        this.entityData.set(CONE_RANGE, range);
    }

    public int getConeRange() {
        return this.entityData.get(CONE_RANGE);
    }

    public void setConeWidth(float width) {
        this.entityData.set(CONE_WIDTH, width);
    }

    public float getConeWidth() {
        return this.entityData.get(CONE_WIDTH);
    }

    @Override
    public void tick() {
        super.tick();
        activeTicks++;

        if (activeTicks >= DURATION_TICKS) {
            discard();
            return;
        }

        if (!level().isClientSide()) {
            damageEntitiesInCone();
            freezeWaterBlocks();
        }

        spawnConeParticles();
    }

    private void damageEntitiesInCone() {
        if (activeTicks != 1) return;

        AABB searchArea = new AABB(coneOrigin, coneOrigin).inflate(getConeRange());

        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (entity == getOwner()) continue;

            if (isEntityInCone(entity)) {
                float damage = getDamage();

                if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                    Vec3 currentMotion = entity.getDeltaMovement();
                    entity.setDeltaMovement(currentMotion.scale(0.3));
                    applyFreezeEffect(entity);
                    continue;
                }

                boolean passedSave = entity.getRandom().nextFloat() < 0.5f;
                if (passedSave) {
                    damage *= 0.5f;
                }

                DamageSource damageSource = level().damageSources().magic();
                entity.hurt(damageSource, damage);

                applyFreezeEffect(entity);
            }
        }

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.POWDER_SNOW_BREAK, SoundSource.HOSTILE,
                EXPLOSION_VOLUME, EXPLOSION_PITCH);
    }

    private void freezeWaterBlocks() {
        if (activeTicks != 1) return;

        for (int x = -getConeRange(); x <= getConeRange(); x++) {
            for (int y = -getConeRange(); y <= getConeRange(); y++) {
                for (int z = -getConeRange(); z <= getConeRange(); z++) {
                    BlockPos pos = new BlockPos((int) (coneOrigin.x + x), (int) (coneOrigin.y + y), (int) (coneOrigin.z + z));

                    if (isPositionInCone(pos)) {
                        if (level().getBlockState(pos).is(Blocks.WATER)) {
                            level().setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
                        } else if (level().getFluidState(pos).getType() == Fluids.WATER) {
                            level().setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    private boolean isPositionInCone(BlockPos pos) {
        Vec3 blockCenter = Vec3.atCenterOf(pos);
        Vec3 toBlock = blockCenter.subtract(coneOrigin).normalize();
        double dotProduct = coneDirection.dot(toBlock);
        double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dotProduct))) * 180.0 / Math.PI;
        double distance = blockCenter.distanceTo(coneOrigin);

        return angle <= getConeWidth() / 2.0 && distance <= getConeRange();
    }

    private boolean isEntityInCone(LivingEntity entity) {
        Vec3 toEntity = entity.position().subtract(coneOrigin).normalize();
        double dotProduct = coneDirection.dot(toEntity);
        double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dotProduct))) * 180.0 / Math.PI;

        double distance = entity.distanceTo(this);
        return angle <= getConeWidth() / 2.0 && distance <= getConeRange();
    }

    private void applyFreezeEffect(LivingEntity entity) {
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                100,
                3
        ));

        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze() + 100, 300));
    }

    private void spawnConeParticles() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        int particleCount = PARTICLE_DENSITY * (activeTicks + 1);

        for (int i = 0; i < particleCount; i++) {
            double angle = (random.nextDouble() - 0.5) * Math.toRadians(getConeWidth());
            double distance = random.nextDouble() * getConeRange();

            Vec3 right = coneDirection.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 up = right.cross(coneDirection).normalize();

            Vec3 rotatedDir = coneDirection.scale(Math.cos(angle))
                    .add(right.scale(Math.sin(angle)));

            Vec3 particlePos = coneOrigin.add(rotatedDir.scale(distance));

            particlePos = particlePos.add(up.scale((random.nextDouble() - 0.5) * 2));

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    particlePos.x, particlePos.y, particlePos.z, 1,
                    0.2, 0.2, 0.2, 0.1);

            if (random.nextFloat() < 0.3f) {
                serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        0.1, 0.1, 0.1, 0.05);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }
}