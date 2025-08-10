package com.soul.goe.spells.config;

public interface SpellEntity {
    void setDamage(float damage);
    void setCriticalChance(float chance);
    void setIsCriticalCast(boolean isCriticalCast);
    void setWandStats(float power, float stability, float durability);

    default void setMaxRange(int range) {}
    default void setDuration(int duration) {}
    default void setCriticalMultipliers(float damageMultiplier, float durationMultiplier) {}
}