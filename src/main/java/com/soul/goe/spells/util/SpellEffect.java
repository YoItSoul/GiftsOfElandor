package com.soul.goe.spells.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class SpellEffect {
    public abstract void cast(Level level, Player caster);
    public abstract String getName();
}
