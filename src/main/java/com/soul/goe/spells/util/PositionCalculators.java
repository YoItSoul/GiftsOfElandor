package com.soul.goe.spells.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PositionCalculators {

    public static Vec3 leftSide(Player caster, float stabilityModifier, float maxOffset, float baseOffset) {
        Vec3 casterPos = caster.position();
        Vec3 lookDir = caster.getLookAngle();
        Vec3 leftVector = lookDir.cross(new Vec3(0, -1, 0)).normalize();

        float positionOffset = Math.max(0.0f, (1.0f - stabilityModifier) * maxOffset);
        float finalOffset = baseOffset;

        if (positionOffset > 0.0f) {
            double offsetX = (caster.getRandom().nextGaussian() * positionOffset);
            double offsetZ = (caster.getRandom().nextGaussian() * positionOffset);
            finalOffset += (float) Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        }

        Vec3 offset = leftVector.scale(finalOffset);

        return new Vec3(
                casterPos.x + offset.x,
                casterPos.y + 1.5,
                casterPos.z + offset.z
        );
    }

    public static Vec3 rightSide(Player caster, float stabilityModifier, float maxOffset, float baseOffset) {
        Vec3 casterPos = caster.position();
        Vec3 lookDir = caster.getLookAngle();
        Vec3 rightVector = lookDir.cross(new Vec3(0, 1, 0)).normalize();

        float positionOffset = Math.max(0.0f, (1.0f - stabilityModifier) * maxOffset);
        float finalOffset = baseOffset;

        if (positionOffset > 0.0f) {
            double offsetX = (caster.getRandom().nextGaussian() * positionOffset);
            double offsetZ = (caster.getRandom().nextGaussian() * positionOffset);
            finalOffset += (float) Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        }

        Vec3 offset = rightVector.scale(finalOffset);

        return new Vec3(
                casterPos.x + offset.x,
                casterPos.y + 1.5,
                casterPos.z + offset.z
        );
    }

    public static Vec3 behindLeft(Player caster, float stabilityModifier, float maxOffset, float baseOffset) {
        Vec3 casterPos = caster.position();
        Vec3 lookDir = caster.getLookAngle();
        Vec3 rightVector = lookDir.cross(new Vec3(0, 1, 0)).normalize();

        float positionOffset = Math.max(0.0f, (1.0f - stabilityModifier) * maxOffset);
        float finalOffset = -baseOffset;

        if (positionOffset > 0.0f) {
            double offsetX = (caster.getRandom().nextGaussian() * positionOffset);
            double offsetZ = (caster.getRandom().nextGaussian() * positionOffset);
            finalOffset -= (float) Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        }

        Vec3 offset = rightVector.scale(finalOffset);

        return new Vec3(
                casterPos.x + offset.x,
                casterPos.y + 1.5,
                casterPos.z + offset.z
        );
    }

    public static Vec3 aboveCaster(Player caster, float stabilityModifier, float maxOffset) {
        Vec3 casterPos = caster.position();

        float positionOffset = Math.max(0.0f, (1.0f - stabilityModifier) * maxOffset);

        if (positionOffset > 0.0f) {
            double offsetX = (caster.getRandom().nextGaussian() * positionOffset);
            double offsetZ = (caster.getRandom().nextGaussian() * positionOffset);

            return new Vec3(
                    casterPos.x + offsetX,
                    casterPos.y + 2.5,
                    casterPos.z + offsetZ
            );
        }

        return new Vec3(casterPos.x, casterPos.y + 2.5, casterPos.z);
    }

    public static Vec3 inFrontOf(Player caster, float stabilityModifier, float maxOffset, float distance) {
        Vec3 casterPos = caster.position();
        Vec3 lookDir = caster.getLookAngle();

        float positionOffset = Math.max(0.0f, (1.0f - stabilityModifier) * maxOffset);

        Vec3 basePosition = casterPos.add(lookDir.scale(distance));

        if (positionOffset > 0.0f) {
            double offsetX = (caster.getRandom().nextGaussian() * positionOffset);
            double offsetZ = (caster.getRandom().nextGaussian() * positionOffset);

            return new Vec3(
                    basePosition.x + offsetX,
                    basePosition.y + 1.5,
                    basePosition.z + offsetZ
            );
        }

        return new Vec3(basePosition.x, basePosition.y + 1.5, basePosition.z);
    }
}