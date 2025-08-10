package com.soul.goe.spells.types.summons;

import com.soul.goe.registry.ModEntityRegistry;
import com.soul.goe.spells.base.BaseSummonSpell;
import com.soul.goe.spells.config.SummonSpellConfig;
import com.soul.goe.spells.entities.summons.DragonSpriteEntity;
import com.soul.goe.spells.util.PositionCalculators;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DragonSpriteSpell extends BaseSummonSpell<DragonSpriteEntity> {

    @Override
    protected EntityType<DragonSpriteEntity> getEntityType() {
        return ModEntityRegistry.DRAGON_SPRITE_PROJECTILE.get();
    }

    @Override
    protected SummonSpellConfig getSpellConfig() {
        return SummonSpellConfig.dragon();
    }

    @Override
    protected DragonSpriteEntity createEntity(EntityType<DragonSpriteEntity> entityType, Level level, LivingEntity owner) {
        return new DragonSpriteEntity(entityType, level, owner);
    }

    @Override
    protected Vec3 calculateSummonPosition(Player caster, float stabilityModifier) {
        return PositionCalculators.aboveCaster(caster, stabilityModifier, getSpellConfig().maxPositionOffset());
    }

    @Override
    public String getName() {
        return "Dragon Sprite";
    }
}