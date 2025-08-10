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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ConeOfColdEntity extends Projectile implements ProjectileEntity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> CONE_RANGE = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CONE_WIDTH = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_DAMAGE_MULTIPLIER = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_RANGE_MULTIPLIER = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(ConeOfColdEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE = 28.0F;
    private static final int DEFAULT_CONE_RANGE = 15;
    private static final float DEFAULT_CONE_WIDTH = 60.0F;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final float DEFAULT_CRITICAL_DAMAGE_MULTIPLIER = 1.8F;
    private static final float DEFAULT_CRITICAL_RANGE_MULTIPLIER = 1.4F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final float DEFAULT_WAND_STAT = 1.0F;
    private static final int DURATION_TICKS = 20;
    private static final int PARTICLE_DENSITY = 5;
    private static final float EXPLOSION_VOLUME = 1.5F;
    private static final float EXPLOSION_PITCH = 0.7F;
    private static final float MAX_SAVE_FAILURE_CHANCE = 0.4f;

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
        setCriticalChance(DEFAULT_CRITICAL_CHANCE);
        setCriticalMultipliers(DEFAULT_CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_RANGE_MULTIPLIER);
        setIsCriticalCast(DEFAULT_IS_CRITICAL_CAST);
        setWandStats(DEFAULT_WAND_STAT, DEFAULT_WAND_STAT, DEFAULT_WAND_STAT);
        this.coneDirection = owner.getLookAngle();
        this.coneOrigin = owner.getEyePosition();
        setPos(coneOrigin.x, coneOrigin.y, coneOrigin.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(CONE_RANGE, DEFAULT_CONE_RANGE);
        builder.define(CONE_WIDTH, DEFAULT_CONE_WIDTH);
        builder.define(CRITICAL_CHANCE, DEFAULT_CRITICAL_CHANCE);
        builder.define(CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_DAMAGE_MULTIPLIER);
        builder.define(CRITICAL_RANGE_MULTIPLIER, DEFAULT_CRITICAL_RANGE_MULTIPLIER);
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
        setConeRange(range);
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
    public void setCriticalChance(float chance) {
        this.entityData.set(CRITICAL_CHANCE, chance);
    }

    public void setCriticalMultipliers(float damageMultiplier, float rangeMultiplier) {
        this.entityData.set(CRITICAL_DAMAGE_MULTIPLIER, damageMultiplier);
        this.entityData.set(CRITICAL_RANGE_MULTIPLIER, rangeMultiplier);
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

    private float getWandStability() {
        return this.entityData.get(WAND_STABILITY);
    }

    private float getWandDurability() {
        return this.entityData.get(WAND_DURABILITY);
    }

    @Override
    public void tick() {
        super.tick();
        activeTicks++;

        if (coneOrigin == null && getOwner() instanceof LivingEntity owner) {
            this.coneOrigin = owner.getEyePosition();
            this.coneDirection = owner.getLookAngle();
        }

        if (coneOrigin == null || coneDirection == null) {
            discard();
            return;
        }

        int adjustedDuration = Math.round(DURATION_TICKS * getWandDurability());

        if (activeTicks >= adjustedDuration) {
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

        int coneRange = getConeRange();
        if (coneRange <= 0 || coneRange > 100) {
            System.err.println("Invalid cone range: " + coneRange);
            return;
        }

        if (coneOrigin == null || !Double.isFinite(coneOrigin.x) || !Double.isFinite(coneOrigin.y) || !Double.isFinite(coneOrigin.z)) {
            System.err.println("Invalid cone origin: " + coneOrigin);
            return;
        }

        AABB searchArea = new AABB(coneOrigin, coneOrigin).inflate(coneRange);

        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (entity == getOwner()) continue;

            if (isEntityInCone(entity)) {
                float damage = getDamage();
                boolean isCriticalHit = random.nextFloat() < this.entityData.get(CRITICAL_CHANCE);

                if (isCriticalHit) {
                    damage *= this.entityData.get(CRITICAL_DAMAGE_MULTIPLIER);
                }

                if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                    Vec3 currentMotion = entity.getDeltaMovement();
                    entity.setDeltaMovement(currentMotion.scale(0.3));
                    applyFreezeEffect(entity, isCriticalHit);
                    continue;
                }

                float stabilityModifier = getWandStability();
                float saveFailureChance = Math.max(0.0f, (1.0f - stabilityModifier) * MAX_SAVE_FAILURE_CHANCE);
                float baseSaveChance = 0.5f - saveFailureChance;

                boolean passedSave = entity.getRandom().nextFloat() < baseSaveChance;
                if (passedSave) {
                    damage *= 0.5f;
                }

                DamageSource damageSource = level().damageSources().magic();
                entity.hurt(damageSource, damage);

                applyFreezeEffect(entity, isCriticalHit);

                if (isCriticalHit) {
                    spawnCriticalHitEffects(entity);
                }
            }
        }

        float powerModifier = getWandPower();
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.POWDER_SNOW_BREAK, SoundSource.HOSTILE,
                Math.min(2.0F, EXPLOSION_VOLUME * powerModifier),
                Math.max(0.4F, EXPLOSION_PITCH * (1.0f - (powerModifier - 1.0f) * 0.1f)));
    }

    private void spawnCriticalHitEffects(LivingEntity entity) {
        level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.HOSTILE,
                0.8F, 0.8F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 15,
                    0.5, 0.5, 0.5, 0.1);

            serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 25,
                    0.7, 0.7, 0.7, 0.15);
        }
    }

    private void freezeWaterBlocks() {
        if (activeTicks != 1) return;

        float powerModifier = getWandPower();
        int effectiveRange = Math.min(20, Math.round(getConeRange() * (powerModifier / 10.0f)));

        for (int x = -effectiveRange; x <= effectiveRange; x++) {
            for (int y = -effectiveRange; y <= effectiveRange; y++) {
                for (int z = -effectiveRange; z <= effectiveRange; z++) {
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

    private void applyFreezeEffect(LivingEntity entity, boolean isCriticalHit) {
        int slowdownAmplifier = isCriticalHit ? 4 : 3;
        int slowdownDuration = isCriticalHit ? 160 : 100;
        int freezeTicks = isCriticalHit ? 200 : 100;

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                slowdownDuration,
                slowdownAmplifier
        ));

        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze() + freezeTicks, 300));

        if (isCriticalHit) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN,
                    slowdownDuration,
                    2
            ));
        }
    }

    private void spawnConeParticles() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        float powerModifier = getWandPower();
        int baseParticleCount = PARTICLE_DENSITY * (activeTicks + 1);
        int adjustedParticleCount = Math.round(baseParticleCount * powerModifier);
        boolean isCrit = isCriticalCast();

        if (isCrit) {
            adjustedParticleCount = Math.round(adjustedParticleCount * 1.5f);
        }

        for (int i = 0; i < adjustedParticleCount; i++) {
            double angle = (random.nextDouble() - 0.5) * Math.toRadians(getConeWidth());
            double distance = random.nextDouble() * getConeRange();

            Vec3 right = coneDirection.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 up = right.cross(coneDirection).normalize();

            Vec3 rotatedDir = coneDirection.scale(Math.cos(angle))
                    .add(right.scale(Math.sin(angle)));

            Vec3 particlePos = coneOrigin.add(rotatedDir.scale(distance));
            particlePos = particlePos.add(up.scale((random.nextDouble() - 0.5) * 2));

            if (isCrit && random.nextFloat() < 0.3f) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        0.15, 0.15, 0.15, 0.08);
            }

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    particlePos.x, particlePos.y, particlePos.z, 1,
                    0.2 * powerModifier, 0.2 * powerModifier, 0.2 * powerModifier, 0.1);

            if (random.nextFloat() < 0.3f * powerModifier) {
                serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        0.1, 0.1, 0.1, 0.05);
            }
        }

        if (isCrit && activeTicks % 3 == 0) {
            for (int i = 0; i < 5; i++) {
                double spiralAngle = (activeTicks * 0.3 + i * Math.PI / 2.5) % (2 * Math.PI);
                double spiralRadius = 2.0 + Math.sin(activeTicks * 0.1) * 0.5;

                Vec3 spiralPos = coneOrigin.add(
                        Math.cos(spiralAngle) * spiralRadius,
                        Math.sin(spiralAngle * 1.5) * 1.0,
                        Math.sin(spiralAngle) * spiralRadius
                );

                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        spiralPos.x, spiralPos.y, spiralPos.z, 1,
                        0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }
}