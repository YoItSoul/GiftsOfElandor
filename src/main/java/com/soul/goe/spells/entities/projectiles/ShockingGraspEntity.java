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
import net.minecraft.world.phys.AABB;

public class ShockingGraspEntity extends Projectile implements ProjectileEntity {
    private static final EntityDataAccessor<Float> DAMAGE_MIN = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE_MAX = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_RANGE = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_STUN_MULTIPLIER = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_CHAIN_CHANCE = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> POWER_MODIFIER = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(ShockingGraspEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE_MIN = 1.0F;
    private static final float DEFAULT_DAMAGE_MAX = 8.0F;
    private static final int DEFAULT_MAX_RANGE = 5;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final float DEFAULT_CRITICAL_STUN_MULTIPLIER = 2.0F;
    private static final float DEFAULT_CRITICAL_CHAIN_CHANCE = 0.4F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final float DEFAULT_POWER_MODIFIER = 1.0F;
    private static final float DEFAULT_WAND_STAT = 1.0F;
    private static final int BASE_PARTICLE_INTERVAL = 1;
    private static final double BASE_TRAIL_PARTICLE_SPREAD = 0.15;
    private static final int BASE_EXPLOSION_PARTICLE_COUNT = 25;
    private static final double BASE_EXPLOSION_PARTICLE_SPREAD = 0.5;
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
        setCriticalChance(DEFAULT_CRITICAL_CHANCE);
        setCriticalStunMultiplier(DEFAULT_CRITICAL_STUN_MULTIPLIER);
        setCriticalChainChance(DEFAULT_CRITICAL_CHAIN_CHANCE);
        setIsCriticalCast(DEFAULT_IS_CRITICAL_CAST);
        setPowerModifier(DEFAULT_POWER_MODIFIER);
        setWandStats(DEFAULT_WAND_STAT, DEFAULT_WAND_STAT, DEFAULT_WAND_STAT);
        this.startPos = owner.position();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE_MIN, DEFAULT_DAMAGE_MIN);
        builder.define(DAMAGE_MAX, DEFAULT_DAMAGE_MAX);
        builder.define(MAX_RANGE, DEFAULT_MAX_RANGE);
        builder.define(CRITICAL_CHANCE, DEFAULT_CRITICAL_CHANCE);
        builder.define(CRITICAL_STUN_MULTIPLIER, DEFAULT_CRITICAL_STUN_MULTIPLIER);
        builder.define(CRITICAL_CHAIN_CHANCE, DEFAULT_CRITICAL_CHAIN_CHANCE);
        builder.define(IS_CRITICAL_CAST, DEFAULT_IS_CRITICAL_CAST);
        builder.define(POWER_MODIFIER, DEFAULT_POWER_MODIFIER);
        builder.define(WAND_POWER, DEFAULT_WAND_STAT);
        builder.define(WAND_STABILITY, DEFAULT_WAND_STAT);
        builder.define(WAND_DURABILITY, DEFAULT_WAND_STAT);
    }

    @Override
    public void setDamage(float damage) {
        setDamageRange(damage, damage);
    }

    public void setDamageRange(float min, float max) {
        this.entityData.set(DAMAGE_MIN, min);
        this.entityData.set(DAMAGE_MAX, max);
    }

    @Override
    public void setCriticalChance(float chance) {
        this.entityData.set(CRITICAL_CHANCE, chance);
    }

    public void setCriticalStunMultiplier(float multiplier) {
        this.entityData.set(CRITICAL_STUN_MULTIPLIER, multiplier);
    }

    public void setCriticalChainChance(float chance) {
        this.entityData.set(CRITICAL_CHAIN_CHANCE, chance);
    }

    @Override
    public void setIsCriticalCast(boolean isCriticalCast) {
        this.entityData.set(IS_CRITICAL_CAST, isCriticalCast);
    }

    public void setPowerModifier(float powerModifier) {
        this.entityData.set(POWER_MODIFIER, powerModifier);
    }

    @Override
    public void setWandStats(float power, float stability, float durability) {
        this.entityData.set(WAND_POWER, power);
        this.entityData.set(WAND_STABILITY, stability);
        this.entityData.set(WAND_DURABILITY, durability);
    }

    public boolean isCriticalCast() {
        return this.entityData.get(IS_CRITICAL_CAST);
    }

    public float getPowerModifier() {
        return this.entityData.get(POWER_MODIFIER);
    }

    public float getRandomDamage() {
        float min = this.entityData.get(DAMAGE_MIN);
        float max = this.entityData.get(DAMAGE_MAX);
        float criticalChance = this.entityData.get(CRITICAL_CHANCE);
        boolean isCritical = random.nextFloat() < criticalChance;

        float damage = min + random.nextFloat() * (max - min);

        if (isCritical) {
            return damage * 1.75f;
        }

        return damage;
    }

    public boolean isCriticalHit() {
        float criticalChance = this.entityData.get(CRITICAL_CHANCE);
        return random.nextFloat() < criticalChance;
    }

    @Override
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
        float powerModifier = getPowerModifier();
        int particleInterval = Math.max(1, Math.round(BASE_PARTICLE_INTERVAL / powerModifier));
        double spreadModifier = BASE_TRAIL_PARTICLE_SPREAD * Math.max(0.5, powerModifier);

        if (ticksInAir % particleInterval == 0) {
            if (isCriticalCast()) {
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        getX() + (random.nextGaussian() * spreadModifier),
                        getY() + (random.nextGaussian() * spreadModifier),
                        getZ() + (random.nextGaussian() * spreadModifier),
                        0, 0, 0);

                level().addParticle(ParticleTypes.CRIT,
                        getX() + (random.nextGaussian() * spreadModifier * 1.5),
                        getY() + (random.nextGaussian() * spreadModifier * 1.5),
                        getZ() + (random.nextGaussian() * spreadModifier * 1.5),
                        0, 0, 0);

                level().addParticle(ParticleTypes.FLASH,
                        getX() + (random.nextGaussian() * spreadModifier * 0.5),
                        getY() + (random.nextGaussian() * spreadModifier * 0.5),
                        getZ() + (random.nextGaussian() * spreadModifier * 0.5),
                        0, 0, 0);
            } else {
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        getX() + (random.nextGaussian() * spreadModifier),
                        getY() + (random.nextGaussian() * spreadModifier),
                        getZ() + (random.nextGaussian() * spreadModifier),
                        0, 0, 0);

                if (random.nextFloat() < 0.7f) {
                    level().addParticle(ParticleTypes.CRIT,
                            getX() + (random.nextGaussian() * spreadModifier),
                            getY() + (random.nextGaussian() * spreadModifier),
                            getZ() + (random.nextGaussian() * spreadModifier),
                            0, 0, 0);
                }
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
                float chargeChance = 0.3f * getPowerModifier();
                if (random.nextFloat() < chargeChance && !creeper.isPowered()) {
                    chargeCreeper(creeper);
                }
                explode();
                return;
            }

            DamageSource damageSource = this.damageSources().thrown(this, owner);
            boolean isCritical = isCriticalHit();
            float damage = getRandomDamage();

            if (livingEntity instanceof Witch) {
                damage *= 0.5f;
            }

            if (livingEntity.isInWaterOrRain()) {
                damage *= 1.5f;
            }

            livingEntity.hurt(damageSource, damage);

            applyShockEffect(livingEntity, isCritical);

            if (isCritical) {
                attemptChainLightning(livingEntity);
                spawnCriticalHitEffects();
            }

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

    private void applyShockEffect(LivingEntity entity, boolean isCritical) {
        int stunDuration = isCritical ?
                Math.round(40 * this.entityData.get(CRITICAL_STUN_MULTIPLIER)) : 40;
        int stunLevel = isCritical ? 2 : 1;

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                stunDuration,
                stunLevel
        ));

        Vec3 currentMotion = entity.getDeltaMovement();
        float slowScale = isCritical ? 0.1f : 0.3f;
        entity.setDeltaMovement(currentMotion.scale(slowScale));

        if (isCritical) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    stunDuration,
                    3
            ));
        }
    }

    private void attemptChainLightning(LivingEntity primaryTarget) {
        float chainChance = this.entityData.get(CRITICAL_CHAIN_CHANCE);
        if (random.nextFloat() > chainChance) return;

        double chainRange = 4.0 * getPowerModifier();
        AABB searchArea = new AABB(primaryTarget.getX() - chainRange, primaryTarget.getY() - chainRange, primaryTarget.getZ() - chainRange,
                primaryTarget.getX() + chainRange, primaryTarget.getY() + chainRange, primaryTarget.getZ() + chainRange);

        for (LivingEntity nearbyEntity : level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (nearbyEntity != primaryTarget && nearbyEntity != getOwner() && nearbyEntity.distanceTo(primaryTarget) <= chainRange) {
                chainLightningToEntity(primaryTarget, nearbyEntity);
                break;
            }
        }
    }

    private void chainLightningToEntity(LivingEntity from, LivingEntity to) {
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            Vec3 fromPos = from.position().add(0, from.getBbHeight() * 0.5, 0);
            Vec3 toPos = to.position().add(0, to.getBbHeight() * 0.5, 0);
            Vec3 direction = toPos.subtract(fromPos).normalize();

            for (int i = 0; i < 10; i++) {
                Vec3 particlePos = fromPos.add(direction.scale(i * from.distanceTo(to) / 10));
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        particlePos.x, particlePos.y, particlePos.z, 2,
                        0.1, 0.1, 0.1, 0.05);
            }

            float chainDamage = (this.entityData.get(DAMAGE_MIN) + this.entityData.get(DAMAGE_MAX)) * 0.5f * 0.7f;
            DamageSource damageSource = this.damageSources().thrown(this, getOwner());
            to.hurt(damageSource, chainDamage);

            applyShockEffect(to, false);

            level().playSound(null, to.getX(), to.getY(), to.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL,
                    0.5F, 2.0F);
        }
    }

    private void spawnCriticalHitEffects() {
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    getX(), getY(), getZ(), 35,
                    0.8, 0.8, 0.8, 0.2);

            serverLevel.sendParticles(ParticleTypes.FLASH,
                    getX(), getY(), getZ(), 5,
                    0.3, 0.3, 0.3, 0.1);

            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL,
                    0.8F, 1.5F);
        }
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
                float powerModifier = getPowerModifier();
                int particleCount = Math.round(BASE_EXPLOSION_PARTICLE_COUNT * powerModifier);
                double spread = BASE_EXPLOSION_PARTICLE_SPREAD * Math.max(0.5, powerModifier);

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        getX(), getY(), getZ(), particleCount,
                        spread, spread, spread,
                        0.2 * powerModifier);

                serverLevel.sendParticles(ParticleTypes.CRIT,
                        getX(), getY(), getZ(), particleCount / 2,
                        spread, spread, spread,
                        0.1 * powerModifier);
            }

            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner();
    }
}