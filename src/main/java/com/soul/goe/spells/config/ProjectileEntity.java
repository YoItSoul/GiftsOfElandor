package com.soul.goe.spells.config;

public interface ProjectileEntity {
    void setDamage(float damage);
    void setMaxRange(int range);
    void setCriticalChance(float chance);
    void setIsCriticalCast(boolean isCriticalCast);
    void setWandStats(float power, float stability, float durability);
}
