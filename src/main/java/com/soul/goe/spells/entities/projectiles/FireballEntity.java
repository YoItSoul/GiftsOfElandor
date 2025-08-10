package com.soul.goe.spells.entities.projectiles;

import com.soul.goe.spells.config.ProjectileEntity;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireballEntity extends Projectile implements ProjectileEntity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> EXPLOSION_RADIUS = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_DAMAGE_MULTIPLIER = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_RADIUS_MULTIPLIER = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE = 24.0F;
    private static final int DEFAULT_EXPLOSION_RADIUS = 3;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final float DEFAULT_CRITICAL_DAMAGE_MULTIPLIER = 2.2F;
    private static final float DEFAULT_CRITICAL_RADIUS_MULTIPLIER = 1.5F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final float DEFAULT_WAND_STAT = 1.0F;
    private static final int TRAIL_PARTICLE_COUNT = 5;
    private static final double TRAIL_PARTICLE_SPREAD = 0.2;
    private static final float EXPLOSION_VOLUME = 2.0F;
    private static final float EXPLOSION_PITCH = 0.8F;
    private static final float MAX_SAVE_FAILURE_CHANCE = 0.4f;

    public FireballEntity(EntityType<? extends FireballEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FireballEntity(EntityType<? extends FireballEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setDamage(DEFAULT_DAMAGE);
        setExplosionRadius(DEFAULT_EXPLOSION_RADIUS);
        setCriticalChance(DEFAULT_CRITICAL_CHANCE);
        setCriticalMultipliers(DEFAULT_CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_RADIUS_MULTIPLIER);
        setIsCriticalCast(DEFAULT_IS_CRITICAL_CAST);
        setWandStats(DEFAULT_WAND_STAT, DEFAULT_WAND_STAT, DEFAULT_WAND_STAT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(EXPLOSION_RADIUS, DEFAULT_EXPLOSION_RADIUS);
        builder.define(CRITICAL_CHANCE, DEFAULT_CRITICAL_CHANCE);
        builder.define(CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_DAMAGE_MULTIPLIER);
        builder.define(CRITICAL_RADIUS_MULTIPLIER, DEFAULT_CRITICAL_RADIUS_MULTIPLIER);
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
    public void setMaxRange(int range) {

    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setExplosionRadius(int radius) {
        this.entityData.set(EXPLOSION_RADIUS, radius);
    }

    public int getExplosionRadius() {
        return this.entityData.get(EXPLOSION_RADIUS);
    }

    @Override
    public void setCriticalChance(float chance) {
        this.entityData.set(CRITICAL_CHANCE, chance);
    }

    public void setCriticalMultipliers(float damageMultiplier, float radiusMultiplier) {
        this.entityData.set(CRITICAL_DAMAGE_MULTIPLIER, damageMultiplier);
        this.entityData.set(CRITICAL_RADIUS_MULTIPLIER, radiusMultiplier);
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
        boolean isCrit = isCriticalCast();
        float powerModifier = getWandPower();

        int particleCount = Math.max(1, Math.round(TRAIL_PARTICLE_COUNT * powerModifier));
        double spread = TRAIL_PARTICLE_SPREAD * Math.max(0.5, powerModifier);

        for (int i = 0; i < particleCount; i++) {
            level().addParticle(ParticleTypes.FLAME,
                    getX() + (random.nextGaussian() * spread),
                    getY() + (random.nextGaussian() * spread),
                    getZ() + (random.nextGaussian() * spread),
                    0, 0, 0);
        }

        if (random.nextFloat() < 0.3f * powerModifier) {
            level().addParticle(ParticleTypes.SMOKE,
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

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            explode();
        }
    }

    private void explode() {
        boolean isCriticalHit = random.nextFloat() < this.entityData.get(CRITICAL_CHANCE);
        float damage = getDamage();
        int radius = getExplosionRadius();

        if (isCriticalHit) {
            damage *= this.entityData.get(CRITICAL_DAMAGE_MULTIPLIER);
            radius = Math.round(radius * this.entityData.get(CRITICAL_RADIUS_MULTIPLIER));
        }

        float powerModifier = getWandPower();
        float stabilityModifier = getWandStability();

        level().explode(this, getX(), getY(), getZ(), radius, false, Level.ExplosionInteraction.MOB);

        float adjustedVolume = Math.min(3.0F, EXPLOSION_VOLUME * powerModifier);
        float adjustedPitch = Math.max(0.5F, EXPLOSION_PITCH * (1.0f - (powerModifier - 1.0f) * 0.1f));

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
                adjustedVolume, adjustedPitch);

        if (isCriticalHit) {
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE,
                    0.8F, 1.5F);
        }

        if (level() instanceof ServerLevel serverLevel) {
            spawnExplosionParticles(serverLevel, powerModifier, isCriticalHit);
        }

        dealDamageToEntities(damage, radius, stabilityModifier, isCriticalHit);
        discard();
    }

    private void spawnExplosionParticles(ServerLevel serverLevel, float powerModifier, boolean isCriticalHit) {
        int explosionParticles = Math.round(10 * powerModifier);
        int flameParticles = Math.round(50 * powerModifier);
        double particleSpread = 0.5 * powerModifier;

        if (isCriticalHit) {
            explosionParticles = Math.round(explosionParticles * 1.5f);
            flameParticles = Math.round(flameParticles * 2.0f);
        }

        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                getX(), getY(), getZ(), explosionParticles,
                particleSpread, particleSpread, particleSpread, 0.2);

        serverLevel.sendParticles(ParticleTypes.FLAME,
                getX(), getY(), getZ(), flameParticles,
                1.0 * powerModifier, 1.0 * powerModifier, 1.0 * powerModifier, 0.1);

        if (isCriticalHit) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    getX(), getY(), getZ(), 30,
                    1.2, 1.2, 1.2, 0.15);

            serverLevel.sendParticles(ParticleTypes.LAVA,
                    getX(), getY(), getZ(), 20,
                    0.8, 0.8, 0.8, 0.1);
        }
    }

    private void dealDamageToEntities(float damage, int radius, float stabilityModifier, boolean isCriticalHit) {
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
            if (entity == getOwner()) continue;

            float distance = (float) entity.distanceTo(this);
            float adjustedDamage = damage * (1.0f - (distance / (radius * 2.0f)));

            if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                adjustedDamage *= 1.5f;
            }

            float saveBonus = Math.max(0.0f, (1.0f - stabilityModifier) * MAX_SAVE_FAILURE_CHANCE);
            float adjustedSaveChance = Math.min(0.9f, 0.5f + saveBonus);

            boolean passedSave = entity.getRandom().nextFloat() < adjustedSaveChance;
            if (passedSave) {
                adjustedDamage *= 0.5f;
            }

            DamageSource damageSource = level().damageSources().magic();
            entity.hurt(damageSource, adjustedDamage);

            int fireDuration = isCriticalHit ? 120 : 60;
            entity.setRemainingFireTicks(fireDuration);

            if (isCriticalHit) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WEAKNESS,
                        100,
                        1
                ));
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner();
    }
}