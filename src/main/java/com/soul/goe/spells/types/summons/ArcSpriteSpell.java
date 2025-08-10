package com.soul.goe.spells.types.summons;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseSummonSpell;
import com.soul.goe.spells.config.SummonSpellConfig;
import com.soul.goe.spells.entities.summons.ArcSpriteEntity;
import com.soul.goe.spells.util.PositionCalculators;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ArcSpriteSpell extends BaseSummonSpell<ArcSpriteEntity> {

    @Override
    protected EntityType<ArcSpriteEntity> getEntityType() {
        return ModEntityRegistry.ARC_SPRITE_PROJECTILE.get();
    }

    @Override
    protected SummonSpellConfig getSpellConfig() {
        return SummonSpellConfig.arc();
    }

    @Override
    protected ArcSpriteEntity createEntity(EntityType<ArcSpriteEntity> entityType, Level level, LivingEntity owner) {
        return new ArcSpriteEntity(entityType, level, owner);
    }

    @Override
    protected Vec3 calculateSummonPosition(Player caster, float stabilityModifier) {
        return PositionCalculators.behindLeft(caster, stabilityModifier, getSpellConfig().maxPositionOffset(), 2.0f);
    }

    @Override
    public String getName() {
        return "Arc Sprite";
    }
}