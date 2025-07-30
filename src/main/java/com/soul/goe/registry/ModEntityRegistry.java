package com.soul.goe.registry;

import com.soul.goe.Goe;
import com.soul.goe.spells.entities.FireballEntity;
import com.soul.goe.spells.entities.FireboltEntity;
import com.soul.goe.spells.entities.RayOfFrostEntity;
import com.soul.goe.spells.entities.ConeOfColdEntity;
import com.soul.goe.spells.entities.ShockingGraspEntity;
import com.soul.goe.spells.entities.LightningBoltEntity;
import com.soul.goe.spells.entities.MagicMissileEntity;
import com.soul.goe.spells.entities.FrostSpriteEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Goe.MODID);

    public static final Supplier<EntityType<FireboltEntity>> FIREBOLT_PROJECTILE =
            ENTITY_TYPES.register("firebolt_projectile", () ->
                    EntityType.Builder.<FireboltEntity>of(FireboltEntity::new, MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(10)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Goe.MODID, "firebolt_projectile")))
            );

    public static final Supplier<EntityType<FireballEntity>> FIREBALL_PROJECTILE =
            ENTITY_TYPES.register("fireball_projectile", () ->
                    EntityType.Builder.<FireballEntity>of(FireballEntity::new, MobCategory.MISC)
                            .sized(0.625F, 0.625F)
                            .clientTrackingRange(20)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Goe.MODID, "fireball_projectile")))
            );

    public static final Supplier<EntityType<RayOfFrostEntity>> RAY_OF_FROST_PROJECTILE =
            ENTITY_TYPES.register("ray_of_frost_projectile", () ->
                    EntityType.Builder.<RayOfFrostEntity>of(RayOfFrostEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(15)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Goe.MODID, "ray_of_frost_projectile")))
            );

    public static final Supplier<EntityType<ConeOfColdEntity>> CONE_OF_COLD_PROJECTILE =
            ENTITY_TYPES.register("cone_of_cold_projectile", () ->
                    EntityType.Builder.<ConeOfColdEntity>of(ConeOfColdEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(25)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Goe.MODID, "cone_of_cold_projectile")))
            );

    public static final Supplier<EntityType<ShockingGraspEntity>> SHOCKING_GRASP_PROJECTILE =
            ENTITY_TYPES.register("shocking_grasp_projectile", () ->
                    EntityType.Builder.<ShockingGraspEntity>of(ShockingGraspEntity::new, MobCategory.MISC)
                            .sized(0.2F, 0.2F)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Goe.MODID, "cone_of_cold_projectile")))
            );

    public static final Supplier<EntityType<LightningBoltEntity>> LIGHTNING_BOLT_PROJECTILE =
            ENTITY_TYPES.register("lightning_bolt_projectile", () ->
                    EntityType.Builder.<LightningBoltEntity>of(LightningBoltEntity::new, MobCategory.MISC)
                            .sized(1.5F, 1.5F)
                            .clientTrackingRange(30)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Goe.MODID, "lightning_bolt_projectile")))
            );

    public static final Supplier<EntityType<MagicMissileEntity>> MAGIC_MISSILE_PROJECTILE =
            ENTITY_TYPES.register("magic_missile_projectile", () ->
                    EntityType.Builder.<MagicMissileEntity>of(MagicMissileEntity::new, MobCategory.MISC)
                            .sized(0.15F, 0.15F)
                            .clientTrackingRange(20)
                            .updateInterval(2)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Goe.MODID, "magic_missile_projectile")))
            );

    public static final Supplier<EntityType<FrostSpriteEntity>> FROST_SPRITE_PROJECTILE =
            ENTITY_TYPES.register("frost_sprite_projectile", () ->
                    EntityType.Builder.<FrostSpriteEntity>of(FrostSpriteEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Goe.MODID, "frost_sprite_projectile")))
            );

}