package com.soul.goe.spells.types.projectiles;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseProjectileSpell;
import com.soul.goe.spells.config.ProjectileSpellConfig;
import com.soul.goe.spells.entities.projectiles.MiningForceEntity;
import com.soul.goe.util.wands.WandStats;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MiningForceSpell extends BaseProjectileSpell<MiningForceEntity> {

    @Override
    public void cast(Level level, Player caster) {
        if (level.isClientSide()) return;

        if (!hasUsablePickaxe(caster)) {
            if (!hasAnyPickaxe(caster)) {
                caster.displayClientMessage(Component.literal("Mining Force requires a pickaxe in your inventory!"), true);
            } else {
                caster.displayClientMessage(Component.literal("Your pickaxe is too damaged to use with Mining Force!"), true);
            }
            return;
        }

        super.cast(level, caster);
    }

    private boolean hasUsablePickaxe(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof PickaxeItem) {
                int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
                if (remainingDurability > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasAnyPickaxe(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof PickaxeItem) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected EntityType<MiningForceEntity> getEntityType() {
        return ModEntityRegistry.MINING_FORCE_PROJECTILE.get();
    }

    @Override
    protected ProjectileSpellConfig getSpellConfig() {
        return createMiningForceConfig();
    }

    private ProjectileSpellConfig createMiningForceConfig() {
        return new ProjectileSpellConfig(
                "earth",
                1.0F,
                0.0F,
                80.0F,
                0.5f,
                1.0f,
                1.2f,
                0.1f,
                createMiningForceSoundConfig(),
                createMiningForceParticleConfig()
        );
    }

    private com.soul.goe.spells.config.ProjectileSoundConfig createMiningForceSoundConfig() {
        return new com.soul.goe.spells.config.ProjectileSoundConfig(
                net.minecraft.sounds.SoundEvents.GRAVEL_BREAK,
                net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT,
                0.9F, 1.1F, 2.0F, 0.8F, 2.0F,
                1.2f, 1.1f, 1.5F
        );
    }

    private com.soul.goe.spells.config.ProjectileParticleConfig createMiningForceParticleConfig() {
        return new com.soul.goe.spells.config.ProjectileParticleConfig(
                net.minecraft.core.particles.ParticleTypes.CRIT,
                net.minecraft.core.particles.ParticleTypes.CLOUD,
                net.minecraft.core.particles.ParticleTypes.FLASH,
                net.minecraft.core.particles.ParticleTypes.ENCHANT,
                12, 0.5, 0.3, 0.02, 1.5f,
                6, 1.0, 0.12, (angle, i) -> 0.0
        );
    }

    @Override
    protected MiningForceEntity createProjectile(EntityType<MiningForceEntity> entityType, Level level, LivingEntity owner) {
        return new MiningForceEntity(entityType, level, owner);
    }

    @Override
    protected Vec3 calculateLaunchDirection(Player caster, float stabilityModifier) {
        return calculateAccurateDirection(caster, stabilityModifier, getSpellConfig().maxAccuracyOffset());
    }

    @Override
    protected Vec3 calculateStartPosition(Player caster, Vec3 direction) {
        return getStandardStartPosition(caster, direction, 0.8f);
    }

    @Override
    protected void configureProjectile(MiningForceEntity projectile, ProjectileSpellConfig config, WandStats wandStats, boolean isCriticalCast) {
        if (wandStats == null) {
            projectile.setMiningSize(1);
            projectile.setMaxRange(80);
        } else {
            float powerModifier = wandStats.power();
            if (config.spellAffinity().equals(wandStats.affinity())) {
                powerModifier += config.affinityPowerBonus();
            }

            int miningSize = Math.min(3, Math.max(1, Math.round(powerModifier)));
            projectile.setMiningSize(miningSize);
            projectile.setMaxRange(Math.round(80 * wandStats.durability()));
            projectile.setCriticalChance(wandStats.critical());
            projectile.setIsCriticalCast(isCriticalCast);
            projectile.setWandStats(wandStats.power(), wandStats.stability(), wandStats.durability());
        }
    }

    @Override
    public String getName() {
        return "Mining Force";
    }
}