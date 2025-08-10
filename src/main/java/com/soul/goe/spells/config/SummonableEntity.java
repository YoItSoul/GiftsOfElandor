package com.soul.goe.spells.config;

public interface SummonableEntity {
    void setDamage(float damage);
    void setDuration(int duration);
    void setCriticalChance(float chance);
    void setCriticalMultipliers(float damageMultiplier, float durationMultiplier);
    void setWandStats(float power, float stability, float durability);
    void setIsCriticalCast(boolean isCriticalCast);
}