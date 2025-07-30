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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LightningBoltEntity extends Projectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LINE_RANGE = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LINE_WIDTH = SynchedEntityData.defineId(LightningBoltEntity.class, EntityDataSerializers.FLOAT);

    private static final float DEFAULT_DAMAGE = 28.0F;
    private static final int DEFAULT_LINE_RANGE = 25;
    private static final float DEFAULT_LINE_WIDTH = 5.0F;
    private static final int DURATION_TICKS = 10;
    private static final int PARTICLE_DENSITY = 8;
    private static final float EXPLOSION_VOLUME = 2.0F;
    private static final float EXPLOSION_PITCH = 1.2F;

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
        this.lineDirection = owner.getLookAngle();
        this.lineOrigin = owner.getEyePosition();
        setPos(lineOrigin.x, lineOrigin.y, lineOrigin.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, DEFAULT_DAMAGE);
        builder.define(LINE_RANGE, DEFAULT_LINE_RANGE);
        builder.define(LINE_WIDTH, DEFAULT_LINE_WIDTH);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
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
    public void tick() {
        super.tick();
        activeTicks++;

        if (activeTicks >= DURATION_TICKS) {
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
                if (entity instanceof Creeper creeper) {
                    if (random.nextFloat() < 0.3f) {
                        chargeCreeper(creeper);
                    }
                    continue;
                }

                float damage = getDamage();

                if (entity instanceof Witch) {
                    damage *= 0.5f;
                }

                if (entity.isInWaterOrRain()) {
                    damage *= 1.5f;
                }

                boolean passedSave = entity.getRandom().nextFloat() < 0.5f;
                if (passedSave) {
                    damage *= 0.5f;
                }

                DamageSource damageSource = level().damageSources().magic();
                entity.hurt(damageSource, damage);

                applyElectricEffect(entity);
            }
        }

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE,
                EXPLOSION_VOLUME, EXPLOSION_PITCH);
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

    private void applyElectricEffect(LivingEntity entity) {
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                60,
                2
        ));

        Vec3 currentMotion = entity.getDeltaMovement();
        entity.setDeltaMovement(currentMotion.scale(0.2));

        if (entity.getRandom().nextFloat() < 0.3f) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.CONFUSION,
                    80,
                    1
            ));
        }
    }

    private void spawnLightningParticles() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        int particleCount = PARTICLE_DENSITY * (activeTicks + 1);

        for (int i = 0; i < particleCount; i++) {
            double distance = random.nextDouble() * getLineRange();
            Vec3 particlePos = lineOrigin.add(lineDirection.scale(distance));

            double offsetX = (random.nextDouble() - 0.5) * getLineWidth();
            double offsetY = (random.nextDouble() - 0.5) * getLineWidth();
            double offsetZ = (random.nextDouble() - 0.5) * getLineWidth();

            particlePos = particlePos.add(offsetX, offsetY, offsetZ);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    particlePos.x, particlePos.y, particlePos.z, 1,
                    0.3, 0.3, 0.3, 0.2);

            if (random.nextFloat() < 0.4f) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        0.2, 0.2, 0.2, 0.1);
            }

            if (random.nextFloat() < 0.2f) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        particlePos.x, particlePos.y, particlePos.z, 1,
                        0.1, 0.1, 0.1, 0.05);
            }
        }

        if (activeTicks <= 3) {
            for (int i = 0; i < 5; i++) {
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