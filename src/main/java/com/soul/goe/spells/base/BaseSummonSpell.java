package com.soul.goe.spells.base;

import com.soul.goe.items.custom.Wand;
import com.soul.goe.spells.util.SpellEffect;
import com.soul.goe.spells.config.SummonSpellConfig;
import com.soul.goe.spells.config.SummonableEntity;
import com.soul.goe.spells.config.SoundConfig;
import com.soul.goe.spells.config.ParticleConfig;
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

public abstract class BaseSummonSpell<T extends Projectile & SummonableEntity> extends SpellEffect {

    protected abstract EntityType<T> getEntityType();
    protected abstract SummonSpellConfig getSpellConfig();
    protected abstract T createEntity(EntityType<T> entityType, Level level, LivingEntity owner);
    protected abstract Vec3 calculateSummonPosition(Player caster, float stabilityModifier);

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        ItemStack wandStack = caster.getMainHandItem();
        SummonSpellConfig config = getSpellConfig();

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

    private void castWithBaseStats(Level level, Player caster, SummonSpellConfig config) {
        Vec3 summonPos = calculateSummonPosition(caster, 1.0f);

        T entity = createEntity(getEntityType(), level, caster);
        entity.setPos(summonPos.x, summonPos.y, summonPos.z);
        entity.setDamage(config.baseDamage());
        entity.setDuration(config.baseDuration());
        entity.setCriticalChance(0.05f);
        entity.setCriticalMultipliers(config.criticalDamageMultiplier(), config.criticalDurationMultiplier());

        level.addFreshEntity(entity);
        playBaseCastEffects(level, caster, summonPos, config);
    }

    private void castWithWandStats(Level level, Player caster, WandStats wandStats, SummonSpellConfig config) {
        float powerModifier = wandStats.power();
        float stabilityModifier = wandStats.stability();
        float durabilityModifier = wandStats.durability();
        float criticalChance = wandStats.critical();

        boolean hasAffinityBonus = config.spellAffinity().equals(wandStats.affinity());
        if (hasAffinityBonus) {
            powerModifier += config.affinityPowerBonus();
        }

        boolean isCriticalCast = level.random.nextFloat() < criticalChance;

        Vec3 summonPos = calculateSummonPosition(caster, stabilityModifier);

        float adjustedDamage = config.baseDamage() * powerModifier;
        int adjustedDuration = Math.round(config.baseDuration() * durabilityModifier);

        if (isCriticalCast) {
            adjustedDamage *= config.criticalDamageMultiplier();
            adjustedDuration = Math.round(adjustedDuration * config.criticalDurationMultiplier());
        }

        T entity = createEntity(getEntityType(), level, caster);
        entity.setPos(summonPos.x, summonPos.y, summonPos.z);
        entity.setDamage(adjustedDamage);
        entity.setDuration(adjustedDuration);
        entity.setCriticalChance(criticalChance);
        entity.setCriticalMultipliers(config.criticalDamageMultiplier(), config.criticalDurationMultiplier());
        entity.setWandStats(powerModifier, stabilityModifier, durabilityModifier);
        entity.setIsCriticalCast(isCriticalCast);

        level.addFreshEntity(entity);
        playEnhancedCastEffects(level, caster, summonPos, powerModifier, hasAffinityBonus, isCriticalCast, config);
    }

    private void playBaseCastEffects(Level level, Player caster, Vec3 summonPos, SummonSpellConfig config) {
        SoundConfig soundConfig = config.sounds();

        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                soundConfig.primarySound(), SoundSource.PLAYERS,
                soundConfig.baseVolume() * soundConfig.primaryVolumeMultiplier(),
                soundConfig.basePitch());

        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                soundConfig.secondarySound(), SoundSource.PLAYERS,
                soundConfig.baseVolume() * soundConfig.secondaryVolumeMultiplier(),
                soundConfig.basePitch() + soundConfig.secondaryPitchOffset());

        if (level instanceof ServerLevel serverLevel) {
            spawnBaseCastParticles(serverLevel, summonPos, config);
        }
    }

    private void playEnhancedCastEffects(Level level, Player caster, Vec3 summonPos, float powerModifier,
                                         boolean hasAffinityBonus, boolean isCriticalCast, SummonSpellConfig config) {
        SoundConfig soundConfig = config.sounds();
        float adjustedVolume = Math.min(soundConfig.maxVolume(), soundConfig.baseVolume() * powerModifier);
        float adjustedPitch = Math.max(soundConfig.minPitch(),
                Math.min(soundConfig.maxPitch(), soundConfig.basePitch() * soundConfig.powerPitchModifier(powerModifier)));

        if (isCriticalCast) {
            adjustedVolume *= soundConfig.criticalVolumeMultiplier();
            adjustedPitch *= soundConfig.criticalPitchMultiplier();
        }

        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                soundConfig.primarySound(), SoundSource.PLAYERS,
                adjustedVolume * soundConfig.primaryVolumeMultiplier(), adjustedPitch);

        level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                soundConfig.secondarySound(), SoundSource.PLAYERS,
                adjustedVolume * soundConfig.secondaryVolumeMultiplier(),
                adjustedPitch + soundConfig.secondaryPitchOffset());

        if (isCriticalCast && soundConfig.criticalSound() != null) {
            level.playSound(null, summonPos.x, summonPos.y, summonPos.z,
                    soundConfig.criticalSound(), SoundSource.PLAYERS,
                    0.6F, soundConfig.criticalSoundPitch());
        }

        if (level instanceof ServerLevel serverLevel) {
            spawnEnhancedCastParticles(serverLevel, summonPos, powerModifier, hasAffinityBonus, isCriticalCast, config);
        }
    }

    private void spawnBaseCastParticles(ServerLevel serverLevel, Vec3 summonPos, SummonSpellConfig config) {
        ParticleConfig particleConfig = config.particles();

        for (int i = 0; i < particleConfig.baseParticleCount(); i++) {
            double angle = 2 * Math.PI * i / particleConfig.baseParticleCount();
            double radius = particleConfig.baseRadius() + Math.random() * particleConfig.radiusVariation();
            double x = summonPos.x + Math.cos(angle) * radius;
            double z = summonPos.z + Math.sin(angle) * radius;
            double y = summonPos.y + Math.random() * particleConfig.heightVariation();

            serverLevel.sendParticles(particleConfig.primaryParticle(),
                    x, y, z, 1, 0, particleConfig.baseVerticalSpeed(), 0, particleConfig.baseSpeed());

            if (Math.random() < particleConfig.secondaryParticleChance()) {
                serverLevel.sendParticles(particleConfig.secondaryParticle(),
                        x, y, z, 1, 0.1, 0.1, 0.1, 0.01);
            }
        }

        serverLevel.sendParticles(particleConfig.enchantParticle(),
                summonPos.x, summonPos.y, summonPos.z, particleConfig.enchantCount(),
                particleConfig.baseSpread(), particleConfig.baseSpread(), particleConfig.baseSpread(),
                particleConfig.enchantSpeed());

        serverLevel.sendParticles(particleConfig.flashParticle(),
                summonPos.x, summonPos.y, summonPos.z, particleConfig.flashCount(), 0, 0, 0, 0);
    }

    private void spawnEnhancedCastParticles(ServerLevel serverLevel, Vec3 summonPos, float powerModifier,
                                            boolean hasAffinityBonus, boolean isCriticalCast, SummonSpellConfig config) {
        ParticleConfig particleConfig = config.particles();

        int adjustedParticleCount = Math.round(particleConfig.baseParticleCount() * powerModifier);
        double adjustedSpread = particleConfig.baseSpread() * Math.max(0.5, powerModifier);
        double maxRadius = (particleConfig.baseRadius() + particleConfig.radiusVariation()) * powerModifier;

        if (isCriticalCast) {
            adjustedParticleCount = Math.round(adjustedParticleCount * particleConfig.criticalParticleMultiplier());
        }

        for (int i = 0; i < adjustedParticleCount; i++) {
            double angle = 2 * Math.PI * i / adjustedParticleCount;
            double radius = particleConfig.baseRadius() + Math.random() * (maxRadius - particleConfig.baseRadius());
            double x = summonPos.x + Math.cos(angle) * radius;
            double z = summonPos.z + Math.sin(angle) * radius;
            double y = summonPos.y + Math.random() * particleConfig.heightVariation() * powerModifier;

            serverLevel.sendParticles(particleConfig.primaryParticle(),
                    x, y, z, 1, 0, particleConfig.baseVerticalSpeed() * powerModifier, 0,
                    particleConfig.baseSpeed() * powerModifier);

            if (Math.random() < particleConfig.secondaryParticleChance() * powerModifier) {
                serverLevel.sendParticles(particleConfig.secondaryParticle(),
                        x, y, z, 1, 0.1, 0.1, 0.1, 0.01);
            }
        }

        if (hasAffinityBonus || isCriticalCast) {
            spawnAffinityParticles(serverLevel, summonPos, powerModifier, config);
        }

        spawnSpecialParticles(serverLevel, summonPos, powerModifier, config);

        serverLevel.sendParticles(particleConfig.enchantParticle(),
                summonPos.x, summonPos.y, summonPos.z, Math.round(particleConfig.enchantCount() * powerModifier),
                adjustedSpread, adjustedSpread, adjustedSpread, particleConfig.enchantSpeed());

        serverLevel.sendParticles(particleConfig.flashParticle(),
                summonPos.x, summonPos.y, summonPos.z,
                isCriticalCast ? particleConfig.flashCount() * 2 : particleConfig.flashCount(),
                0, 0, 0, 0);
    }

    protected void spawnAffinityParticles(ServerLevel serverLevel, Vec3 summonPos, float powerModifier, SummonSpellConfig config) {
        ParticleConfig particleConfig = config.particles();
        if (particleConfig.affinityParticle() != null) {
            for (int i = 0; i < particleConfig.affinityParticleCount(); i++) {
                double angle = i * Math.PI / (particleConfig.affinityParticleCount() / 2.0);
                double radius = particleConfig.affinityRadius() + i * particleConfig.affinityRadiusIncrement();
                double x = summonPos.x + Math.cos(angle) * radius;
                double z = summonPos.z + Math.sin(angle) * radius;
                double y = summonPos.y + particleConfig.affinityHeightFunction().apply(angle, i);

                serverLevel.sendParticles(particleConfig.affinityParticle(),
                        x, y, z, 2, 0.05, 0.05, 0.05, 0.02);
            }
        }
    }

    protected void spawnSpecialParticles(ServerLevel serverLevel, Vec3 summonPos, float powerModifier, SummonSpellConfig config) {
        ParticleConfig particleConfig = config.particles();
        if (particleConfig.specialParticle() != null) {
            for (int i = 0; i < Math.round(particleConfig.specialParticleCount() * powerModifier); i++) {
                double angle = i * Math.PI / (particleConfig.specialParticleCount() / 2.0);
                double radius = (particleConfig.specialRadius() + i * particleConfig.specialRadiusIncrement()) * powerModifier;
                double x = summonPos.x + Math.cos(angle) * radius;
                double z = summonPos.z + Math.sin(angle) * radius;
                double y = summonPos.y + i * particleConfig.specialHeightIncrement();

                serverLevel.sendParticles(particleConfig.specialParticle(),
                        x, y, z, 1, 0, 0, 0, particleConfig.specialSpeed());
            }
        }
    }
}