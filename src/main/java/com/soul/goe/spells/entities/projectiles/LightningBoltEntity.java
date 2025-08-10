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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LightningBoltEntity extends Projectile implements ProjectileEntity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LINE_RANGE = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LINE_WIDTH = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_CHANCE = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_DAMAGE_MULTIPLIER = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CRITICAL_WIDTH_MULTIPLIER = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_CRITICAL_CAST = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WAND_POWER = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_STABILITY = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WAND_DURABILITY = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE = 28.0F;
    private static final int DEFAULT_LINE_RANGE = 25;
    private static final float DEFAULT_LINE_WIDTH = 5.0F;
    private static final float DEFAULT_CRITICAL_CHANCE = 0.05F;
    private static final float DEFAULT_CRITICAL_DAMAGE_MULTIPLIER = 2.0F;
    private static final float DEFAULT_CRITICAL_WIDTH_MULTIPLIER = 1.8F;
    private static final boolean DEFAULT_IS_CRITICAL_CAST = false;
    private static final float DEFAULT_WAND_STAT = 1.0F;
    private static final int DURATION_TICKS = 10;
    private static final int PARTICLE_DENSITY = 8;
    private static final float EXPLOSION_VOLUME = 2.0F;
    private static final float EXPLOSION_PITCH = 1.2F;
    private static final float MAX_SAVE_FAILURE_CHANCE = 0.3f;

    private int activeTicks = 0;
    private Vec3 lineDirection;
    private Vec3 lineOrigin;

    public LightningBoltEntity(EntityType<? extends LightningBoltEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LightningBoltEntity(EntityType<? extends LightningBoltEntity> entityType, Level level, LivingEntity owner) {
        super(entityType, level);
        setOwner(owner);
        setDamage(DEFAULT_DAMAGE);
        setLineRange(DEFAULT_LINE_RANGE);
        setLineWidth(DEFAULT_LINE_WIDTH);
        setCriticalChance(DEFAULT_CRITICAL_CHANCE);
        setCriticalMultipliers(DEFAULT_CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_WIDTH_MULTIPLIER);
        setIsCriticalCast(DEFAULT_IS_CRITICAL_CAST);
        setWandStats(DEFAULT_WAND_STAT, DEFAULT_WAND_STAT, DEFAULT_WAND_STAT);
        this.lineDirection = owner.getLookAngle();
        this.lineOrigin = owner.getEyePosition();
        setPos(lineOrigin.x, lineOrigin.y, lineOrigin.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(LINE_RANGE, DEFAULT_LINE_RANGE);
        builder.define(LINE_WIDTH, DEFAULT_LINE_WIDTH);
        builder.define(CRITICAL_CHANCE, DEFAULT_CRITICAL_CHANCE);
        builder.define(CRITICAL_DAMAGE_MULTIPLIER, DEFAULT_CRITICAL_DAMAGE_MULTIPLIER);
        builder.define(CRITICAL_WIDTH_MULTIPLIER, DEFAULT_CRITICAL_WIDTH_MULTIPLIER);
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
        setLineRange(range);
    }

    public void setLineRange(int range) {
        this.entityData.set(LINE_RANGE, range);
    }

    public int getLineRange() {
        return this.entityData.get(LINE_RANGE);
    }

    public void setLineWidth(float width) {
        this.entityData.set(LINE_WIDTH, width);
    }

    public float getLineWidth() {
        return this.entityData.get(LINE_WIDTH);
    }

    @Override
    public void setCriticalChance(float chance) {
        this.entityData.set(CRITICAL_CHANCE, chance);
    }

    public void setCriticalMultipliers(float damageMultiplier, float widthMultiplier) {
        this.entityData.set(CRITICAL_DAMAGE_MULTIPLIER, damageMultiplier);
        this.entityData.set(CRITICAL_WIDTH_MULTIPLIER, widthMultiplier);
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

        float durabilityModifier = getWandDurability();
        int adjustedDuration = Math.round(DURATION_TICKS * durabilityModifier);

        if (activeTicks >= adjustedDuration) {
            discard();
            return;
        }

        if (!level().isClientSide()) {
            damageEntitiesInLine();
        }

        spawnLightningParticles();
    }

    private void damageEntitiesInLine() {
        if (activeTicks != 1) return;

        AABB searchArea = new AABB(lineOrigin, lineOrigin).inflate(getLineRange());

        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, searchArea)) {
            if (entity == getOwner()) continue;

            if (isEntityInLine(entity)) {
                boolean isCriticalHit = random.nextFloat() < this.entityData.get(CRITICAL_CHANCE);

                if (entity instanceof Creeper creeper) {
                    float powerModifier = getWandPower();
                    float chargeChance = Math.min(0.7f, 0.3f * powerModifier);
                    if (random.nextFloat() < chargeChance) {
                        chargeCreeper(creeper);
                    }
                    continue;
                }

                float damage = getDamage();
                if (isCriticalHit) {
                    damage *= this.entityData.get(CRITICAL_DAMAGE_MULTIPLIER);
                }

                if (entity instanceof Witch) {
                    damage *= 0.5f;
                }

                if (entity.isInWaterOrRain()) {
                    damage *= 1.5f;
                }

                float stabilityModifier = getWandStability();
                float saveBonus = Math.max(0.0f, (1.0f - stabilityModifier) * MAX_SAVE_FAILURE_CHANCE);
                float adjustedSaveChance = Math.min(0.8f, 0.5f + saveBonus);

                boolean passedSave = entity.getRandom().nextFloat() < adjustedSaveChance;
                if (passedSave) {
                    damage *= 0.5f;
                }

                DamageSource damageSource = level().damageSources().magic();
                entity.hurt(damageSource, damage);

                applyElectricEffect(entity, isCriticalHit);

                if (isCriticalHit) {
                    spawnCriticalHitEffects(entity);
                }
            }
        }

        float powerModifier = getWandPower();
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE,
                Math.min(3.0F, EXPLOSION_VOLUME * powerModifier),
                Math.max(0.8F, EXPLOSION_PITCH * (1.0f + (powerModifier - 1.0f) * 0.1f)));
    }

    private void spawnCriticalHitEffects(LivingEntity entity) {
        level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE,
                0.8F, 1.5F);

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 15,
                    0.5, 0.5, 0.5, 0.1);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 30,
                    0.8, 0.8, 0.8, 0.3);
        }
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

    private boolean isEntityInLine(LivingEntity entity) {
        Vec3 toEntity = entity.position().subtract(lineOrigin);
        Vec3 projectedPoint = lineOrigin.add(lineDirection.scale(toEntity.dot(lineDirection)));
        double distanceFromLine = entity.position().distanceTo(projectedPoint);
        double distanceFromOrigin = projectedPoint.distanceTo(lineOrigin);

        return distanceFromLine <= getLineWidth() / 2.0 && distanceFromOrigin <= getLineRange();
    }

    private void applyElectricEffect(LivingEntity entity, boolean isCriticalHit) {
        int weaknessAmplifier = isCriticalHit ? 3 : 2;
        int weaknessDuration = isCriticalHit ? 100 : 60;

        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                weaknessDuration,
                weaknessAmplifier
        ));

        Vec3 currentMotion = entity.getDeltaMovement();
        float stunMultiplier = isCriticalHit ? 0.1f : 0.2f;
        entity.setDeltaMovement(currentMotion.scale(stunMultiplier));

        float confusionChance = isCriticalHit ? 0.6f : 0.3f;
        if (entity.getRandom().nextFloat() < confusionChance) {
            int confusionDuration = isCriticalHit ? 120 : 80;
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.CONFUSION,
                    confusionDuration,
                    isCriticalHit ? 2 : 1
            ));
        }
    }

    private void spawnLightningParticles() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        float powerModifier = getWandPower();
        boolean isCrit = isCriticalCast();
        int baseParticleCount = PARTICLE_DENSITY * (activeTicks + 1);
        int adjustedParticleCount = Math.round(baseParticleCount * powerModifier);

        if (isCrit) {
            adjustedParticleCount = Math.round(adjustedParticleCount * 1.5f);
        }

        for (int i = 0; i < adjustedParticleCount; i++) {
            double distance = random.nextDouble() * getLineRange();
            Vec3 particlePos = lineOrigin.add(lineDirection.scale(distance));

            double offsetX = (random.nextDouble() - 0.5) * getLineWidth();
            double offsetY = (random.nextDouble() - 0.5) * getLineWidth();
            double offsetZ = (random.nextDouble() - 0.5) * getLineWidth();

            particlePos = particlePos.add(offsetX, offsetY, offsetZ);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z, 1,
                    0.3 * powerModifier, 0.3 * powerModifier, 0.3 * powerModifier, 0.2 * powerModifier);

            if (random.nextFloat() < 0.4f * powerModifier) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        0.2, 0.2, 0.2, 0.1);
            }

            if (random.nextFloat() < 0.2f * powerModifier) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        0.1, 0.1, 0.1, 0.05);
            }

            if (isCrit && random.nextFloat() < 0.3f) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        0.15, 0.15, 0.15, 0.08);
            }
        }

        if (activeTicks <= Math.round(3 * getWandDurability())) {
            for (int i = 0; i < Math.round(5 * powerModifier); i++) {
                double distance = random.nextDouble() * getLineRange();
                Vec3 particlePos = lineOrigin.add(lineDirection.scale(distance));

                serverLevel.sendParticles(ParticleTypes.FLASH,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        0.1, 0.1, 0.1, 0);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }
}