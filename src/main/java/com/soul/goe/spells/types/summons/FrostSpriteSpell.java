package com.soul.goe.spells.types.summons;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseSummonSpell;
import com.soul.goe.spells.config.SummonSpellConfig;
import com.soul.goe.spells.entities.summons.FrostSpriteEntity;
import com.soul.goe.spells.util.PositionCalculators;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FrostSpriteSpell extends BaseSummonSpell<FrostSpriteEntity> {

    @Override
    protected EntityType<FrostSpriteEntity> getEntityType() {
        return ModEntityRegistry.FROST_SPRITE_PROJECTILE.get();
    }

    @Override
    protected SummonSpellConfig getSpellConfig() {
        return SummonSpellConfig.frost();
    }

    @Override
    protected FrostSpriteEntity createEntity(EntityType<FrostSpriteEntity> entityType, Level level, LivingEntity owner) {
        return new FrostSpriteEntity(entityType, level, owner);
    }

    @Override
    protected Vec3 calculateSummonPosition(Player caster, float stabilityModifier) {
        return PositionCalculators.rightSide(caster, stabilityModifier, getSpellConfig().maxPositionOffset(), 2.0f);
    }

    @Override
    public String getName() {
        return "Frost Sprite";
    }
}