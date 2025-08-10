package com.soul.goe.spells.base;

import com.soul.goe.items.custom.Wand;
import com.soul.goe.spells.config.*;
import com.soul.goe.spells.util.SpellEffect;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class BaseProjectileSpell<T extends Projectile & ProjectileEntity> extends SpellEffect {

    protected abstract EntityType<T> getEntityType();
    protected abstract ProjectileSpellConfig getSpellConfig();
    protected abstract T createProjectile(EntityType<T> entityType, Level level, LivingEntity owner);
    protected abstract Vec3 calculateLaunchDirection(Player caster, float stabilityModifier);
    protected abstract Vec3 calculateStartPosition(Player caster, Vec3 direction);
    protected abstract void configureProjectile(T projectile, ProjectileSpellConfig config, WandStats wandStats, boolean isCriticalCast);

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        ItemStack wandStack = caster.getMainHandItem();
        ProjectileSpellConfig config = getSpellConfig();

        if (!(wandStack.getItem() instanceof Wand)) {
            castWithBaseStats(level, caster, config);
            return;
        }

        WandStats wandStats = Wand.getWandStats(wandStack);
        if (wandStats == null) {
            castWithBaseStats(level, caster, config);
            return;
        }

        castWithWandStats(level, caster, wandStats, config);
    }

    private void castWithBaseStats(Level level, Player caster, ProjectileSpellConfig config) {
        Vec3 direction = calculateLaunchDirection(caster, 1.0f);
        Vec3 startPos = calculateStartPosition(caster, direction);

        T projectile = createProjectile(getEntityType(), level, caster);
        projectile.setPos(startPos.x, startPos.y, startPos.z);
        projectile.setDeltaMovement(direction.scale(config.baseSpeed()));

        configureProjectile(projectile, config, null, false);

        level.addFreshEntity(projectile);
        playBaseCastEffects(level, caster, config);
    }

    private void castWithWandStats(Level level, Player caster, WandStats wandStats, ProjectileSpellConfig config) {
        float powerModifier = wandStats.power();
        float stabilityModifier = wandStats.stability();
        float durabilityModifier = wandStats.durability();
        float criticalChance = wandStats.critical();

        boolean hasAffinityBonus = config.spellAffinity().equals(wandStats.affinity());
        if (hasAffinityBonus) {
            powerModifier += config.affinityPowerBonus();
        }

        boolean isCriticalCast = level.random.nextFloat() < criticalChance;

        Vec3 direction = calculateLaunchDirection(caster, stabilityModifier);
        Vec3 startPos = calculateStartPosition(caster, direction);

        float adjustedSpeed = config.baseSpeed() * powerModifier;

        T projectile = createProjectile(getEntityType(), level, caster);
        projectile.setPos(startPos.x, startPos.y, startPos.z);
        projectile.setDeltaMovement(direction.scale(adjustedSpeed));

        configureProjectile(projectile, config, wandStats, isCriticalCast);

        level.addFreshEntity(projectile);
        playEnhancedCastEffects(level, caster, powerModifier, hasAffinityBonus, isCriticalCast, config);
    }

    protected Vec3 calculateAccurateDirection(Player caster, float stabilityModifier, float maxOffset) {
        Vec3 baseLookDirection = caster.getLookAngle();
        float accuracyOffset = Math.max(0.0f, (1.0f - stabilityModifier) * maxOffset);

        if (accuracyOffset > 0.0f) {
            double offsetX = (caster.getRandom().nextGaussian() * accuracyOffset);
            double offsetY = (caster.getRandom().nextGaussian() * accuracyOffset);
            double offsetZ = (caster.getRandom().nextGaussian() * accuracyOffset);
            return baseLookDirection.add(offsetX, offsetY, offsetZ).normalize();
        }

        return baseLookDirection;
    }

    protected Vec3 getStandardStartPosition(Player caster, Vec3 direction, float distance) {
        return caster.getEyePosition().add(direction.scale(distance));
    }

    private void playBaseCastEffects(Level level, Player caster, ProjectileSpellConfig config) {
        ProjectileSoundConfig soundConfig = config.sounds();

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                soundConfig.primarySound(), SoundSource.PLAYERS,
                soundConfig.baseVolume(), soundConfig.basePitch());

        if (level instanceof ServerLevel serverLevel) {
            spawnBaseCastParticles(serverLevel, caster, config);
        }
    }

    private void playEnhancedCastEffects(Level level, Player caster, float powerModifier,
                                         boolean hasAffinityBonus, boolean isCriticalCast, ProjectileSpellConfig config) {
        ProjectileSoundConfig soundConfig = config.sounds();
        float adjustedVolume = Math.min(soundConfig.maxVolume(), soundConfig.baseVolume() * powerModifier);
        float adjustedPitch = Math.max(soundConfig.minPitch(),
                Math.min(soundConfig.maxPitch(), soundConfig.basePitch() * soundConfig.powerPitchModifier(powerModifier)));

        if (isCriticalCast) {
            adjustedVolume *= soundConfig.criticalVolumeMultiplier();
            adjustedPitch *= soundConfig.criticalPitchMultiplier();
        }

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                soundConfig.primarySound(), SoundSource.PLAYERS,
                adjustedVolume, adjustedPitch);

        if (isCriticalCast && soundConfig.criticalSound() != null) {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    soundConfig.criticalSound(), SoundSource.PLAYERS,
                    0.6F, soundConfig.criticalSoundPitch());
        }

        if (level instanceof ServerLevel serverLevel) {
            spawnEnhancedCastParticles(serverLevel, caster, powerModifier, hasAffinityBonus, isCriticalCast, config);
        }
    }

    private void spawnBaseCastParticles(ServerLevel serverLevel, Player caster, ProjectileSpellConfig config) {
        ProjectileParticleConfig particleConfig = config.particles();
        Vec3 eyePos = caster.getEyePosition();
        Vec3 lookDir = caster.getLookAngle();
        Vec3 particlePos = eyePos.add(lookDir.scale(particleConfig.baseDistance()));

        serverLevel.sendParticles(particleConfig.primaryParticle(),
                particlePos.x, particlePos.y, particlePos.z,
                particleConfig.baseParticleCount(),
                particleConfig.baseSpread(), particleConfig.baseSpread(), particleConfig.baseSpread(),
                particleConfig.baseSpeed());

        if (particleConfig.secondaryParticle() != null) {
            serverLevel.sendParticles(particleConfig.secondaryParticle(),
                    particlePos.x, particlePos.y, particlePos.z,
                    particleConfig.baseParticleCount() / 3,
                    particleConfig.baseSpread(), particleConfig.baseSpread(), particleConfig.baseSpread(),
                    particleConfig.baseSpeed() * 0.5);
        }
    }

    private void spawnEnhancedCastParticles(ServerLevel serverLevel, Player caster, float powerModifier,
                                            boolean hasAffinityBonus, boolean isCriticalCast, ProjectileSpellConfig config) {
        ProjectileParticleConfig particleConfig = config.particles();
        Vec3 eyePos = caster.getEyePosition();
        Vec3 lookDir = caster.getLookAngle();
        Vec3 particlePos = eyePos.add(lookDir.scale(particleConfig.baseDistance()));

        int adjustedParticleCount = Math.round(particleConfig.baseParticleCount() * powerModifier);
        double adjustedSpread = particleConfig.baseSpread() * Math.max(0.5, powerModifier);

        if (isCriticalCast) {
            adjustedParticleCount = Math.round(adjustedParticleCount * particleConfig.criticalParticleMultiplier());
        }

        serverLevel.sendParticles(particleConfig.primaryParticle(),
                particlePos.x, particlePos.y, particlePos.z,
                adjustedParticleCount,
                adjustedSpread, adjustedSpread, adjustedSpread,
                particleConfig.baseSpeed() * powerModifier);

        if (particleConfig.secondaryParticle() != null) {
            serverLevel.sendParticles(particleConfig.secondaryParticle(),
                    particlePos.x, particlePos.y, particlePos.z,
                    adjustedParticleCount / 3,
                    adjustedSpread, adjustedSpread, adjustedSpread,
                    particleConfig.baseSpeed() * powerModifier * 0.5);
        }

        if (hasAffinityBonus || isCriticalCast) {
            spawnAffinityParticles(serverLevel, particlePos, powerModifier, config);
        }

        if (isCriticalCast && particleConfig.flashParticle() != null) {
            serverLevel.sendParticles(particleConfig.flashParticle(),
                    particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
        }
    }

    protected void spawnAffinityParticles(ServerLevel serverLevel, Vec3 particlePos, float powerModifier, ProjectileSpellConfig config) {
        ProjectileParticleConfig particleConfig = config.particles();
        if (particleConfig.affinityParticle() != null) {
            for (int i = 0; i < particleConfig.affinityParticleCount(); i++) {
                double angle = i * Math.PI / (particleConfig.affinityParticleCount() / 2.0);
                double radius = particleConfig.affinityRadius() + i * particleConfig.affinityRadiusIncrement();
                double x = particlePos.x + Math.cos(angle) * radius;
                double z = particlePos.z + Math.sin(angle) * radius;
                double y = particlePos.y + particleConfig.affinityHeightFunction().apply(angle, i);

                serverLevel.sendParticles(particleConfig.affinityParticle(),
                        x, y, z, 2, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }
}