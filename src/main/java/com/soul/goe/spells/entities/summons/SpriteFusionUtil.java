package com.soul.goe.spells.entities.summons;

import com.soul.goe.registry.ModEntityRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class SpriteFusionUtil {
    private static final double FUSION_DETECTION_RADIUS = 15.0;
    private static final double FUSION_DISTANCE = 4.0;

    public static boolean canAttemptFusion(SpriteEntityBase sprite) {
        LivingEntity owner = (LivingEntity) sprite.getOwner();
        if (owner == null || !owner.isAlive()) return false;

        Level level = sprite.level();
        AABB searchArea = new AABB(sprite.position(), sprite.position()).inflate(FUSION_DETECTION_RADIUS);

        boolean hasFlame = false;
        boolean hasArc = false;
        boolean hasFrost = false;

        List<SpriteEntityBase> sprites = level.getEntitiesOfClass(SpriteEntityBase.class, searchArea);

        for (SpriteEntityBase otherSprite : sprites) {
            if (otherSprite.getOwner() != owner) continue;
            if (!otherSprite.isAlive()) continue;

            if (otherSprite instanceof FlameSpriteEntity) {
                hasFlame = true;
            } else if (otherSprite instanceof ArcSpriteEntity) {
                hasArc = true;
            } else if (otherSprite instanceof FrostSpriteEntity) {
                hasFrost = true;
            }
        }

        return hasFlame && hasArc && hasFrost;
    }

    public static boolean attemptFusion(SpriteEntityBase sprite) {
        if (!canAttemptFusion(sprite)) return false;

        LivingEntity owner = (LivingEntity) sprite.getOwner();
        Level level = sprite.level();
        AABB searchArea = new AABB(sprite.position(), sprite.position()).inflate(FUSION_DETECTION_RADIUS);

        FlameSpriteEntity flameSprite = null;
        ArcSpriteEntity arcSprite = null;
        FrostSpriteEntity frostSprite = null;

        List<SpriteEntityBase> sprites = level.getEntitiesOfClass(SpriteEntityBase.class, searchArea);

        for (SpriteEntityBase otherSprite : sprites) {
            if (otherSprite.getOwner() != owner) continue;
            if (!otherSprite.isAlive()) continue;

            if (otherSprite instanceof FlameSpriteEntity) {
                flameSprite = (FlameSpriteEntity) otherSprite;
            } else if (otherSprite instanceof ArcSpriteEntity) {
                arcSprite = (ArcSpriteEntity) otherSprite;
            } else if (otherSprite instanceof FrostSpriteEntity) {
                frostSprite = (FrostSpriteEntity) otherSprite;
            }
        }

        if (flameSprite != null && arcSprite != null && frostSprite != null) {
            Vec3 flamePos = flameSprite.position();
            Vec3 arcPos = arcSprite.position();
            Vec3 frostPos = frostSprite.position();

            double flameToArc = flamePos.distanceTo(arcPos);
            double flameToFrost = flamePos.distanceTo(frostPos);
            double arcToFrost = arcPos.distanceTo(frostPos);

            if (flameToArc <= FUSION_DISTANCE && flameToFrost <= FUSION_DISTANCE && arcToFrost <= FUSION_DISTANCE) {
                performFusion(flameSprite, arcSprite, frostSprite, owner);
                return true;
            }
        }

        return false;
    }

    private static void performFusion(FlameSpriteEntity flame, ArcSpriteEntity arc, FrostSpriteEntity frost, LivingEntity owner) {
        Level level = flame.level();
        Vec3 centerPos = flame.position().add(arc.position()).add(frost.position()).scale(1.0/3.0);

        DragonSpriteEntity dragon = new DragonSpriteEntity(ModEntityRegistry.DRAGON_SPRITE_PROJECTILE.get(), level, owner);
        dragon.setPos(centerPos.x, centerPos.y, centerPos.z);

        float avgDamage = (flame.getDamage() + arc.getDamage() + frost.getDamage()) / 3.0f;
        dragon.setDamage(avgDamage * 1.5f);

        level.addFreshEntity(dragon);

        flame.createFusionEffect();
        arc.createFusionEffect();
        frost.createFusionEffect();

        flame.discard();
        arc.discard();
        frost.discard();
    }

    public static SpriteEntityBase findNearestCompatibleSprite(SpriteEntityBase sprite) {
        if (!canAttemptFusion(sprite)) return null;

        LivingEntity owner = (LivingEntity) sprite.getOwner();
        if (owner == null || !owner.isAlive()) return null;

        Level level = sprite.level();
        AABB searchArea = new AABB(sprite.position(), sprite.position()).inflate(FUSION_DETECTION_RADIUS);

        List<SpriteEntityBase> sprites = level.getEntitiesOfClass(SpriteEntityBase.class, searchArea);

        SpriteEntityBase nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (SpriteEntityBase otherSprite : sprites) {
            if (otherSprite == sprite) continue;
            if (otherSprite.getOwner() != owner) continue;
            if (!otherSprite.isAlive()) continue;
            if (otherSprite.getClass() == sprite.getClass()) continue;

            double distance = sprite.distanceTo(otherSprite);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = otherSprite;
            }
        }

        return nearest;
    }
}