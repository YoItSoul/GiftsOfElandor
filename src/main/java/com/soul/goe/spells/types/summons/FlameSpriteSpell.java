package com.soul.goe.spells.types.summons;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseSummonSpell;
import com.soul.goe.spells.config.SummonSpellConfig;
import com.soul.goe.spells.entities.summons.FlameSpriteEntity;
import com.soul.goe.spells.util.PositionCalculators;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FlameSpriteSpell extends BaseSummonSpell<FlameSpriteEntity> {

    @Override
    protected EntityType<FlameSpriteEntity> getEntityType() {
        return ModEntityRegistry.FLAME_SPRITE_PROJECTILE.get();
    }

    @Override
    protected SummonSpellConfig getSpellConfig() {
        return SummonSpellConfig.flame();
    }

    @Override
    protected FlameSpriteEntity createEntity(EntityType<FlameSpriteEntity> entityType, Level level, LivingEntity owner) {
        return new FlameSpriteEntity(entityType, level, owner);
    }

    @Override
    protected Vec3 calculateSummonPosition(Player caster, float stabilityModifier) {
        return PositionCalculators.leftSide(caster, stabilityModifier, getSpellConfig().maxPositionOffset(), 2.2f);
    }

    @Override
    public String getName() {
        return "Flame Sprite";
    }
}