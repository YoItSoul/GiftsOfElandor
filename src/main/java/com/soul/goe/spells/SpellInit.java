package com.soul.goe.spells;

import com.soul.goe.spells.types.*;
import net.minecraft.world.item.Items;

public class SpellInit {
    public static void registerSpells() {
        SpellRegistry.registerSpell(new SpellData(
                "fire_bolt",
                new FireboltSpell(),
                20
        ));

        SpellRegistry.registerSpell(new SpellData(
                "ray_of_frost",
                new RayOfFrostSpell(),
                20
        ));

        SpellRegistry.registerSpell(new SpellData(
                "shocking_grasp",
                new ShockingGraspSpell(),
                20
        ));

        SpellRegistry.registerSpell(new SpellData(
                "magic_missile",
                new MagicMissileSpell(),
                30,
                SpellCost.builder()
                        .addCost(Items.GLOWSTONE_DUST, 1)
                        .addCost(Items.GOLD_NUGGET, 2)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "lunge",
                new LungeSpell(),
                40,
                SpellCost.builder()
                        .addCost(Items.SUGAR, 2)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "levitate",
                new LevitateSpell(),
                60,
                SpellCost.builder()
                        .addCost(Items.FEATHER, 2)
                        .addCost(Items.GOLD_NUGGET, 1)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "fireball",
                new FireballSpell(),
                80,
                SpellCost.builder()
                        .addCost(Items.GUNPOWDER, 2)
                        .addCost(Items.BLAZE_POWDER, 1)
                        .addCost(Items.GOLD_NUGGET, 3)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "lightning_bolt",
                new LightningBoltSpell(),
                100,
                SpellCost.builder()
                        .addCost(Items.COPPER_INGOT, 1)
                        .addCost(Items.REDSTONE, 4)
                        .addCost(Items.GOLD_NUGGET, 2)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "frost_sprite",
                new FrostSpriteSpell(),
                100,
                SpellCost.builder()
                        .addCost(Items.PACKED_ICE, 1)
                        .addCost(Items.SOUL_SAND, 1)
                        .addCost(Items.GOLD_NUGGET, 2)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "arc_sprite",
                new ArcSpriteSpell(),
                100,
                SpellCost.builder()
                        .addCost(Items.COPPER_INGOT, 1)
                        .addCost(Items.SOUL_SAND, 1)
                        .addCost(Items.GOLD_NUGGET, 2)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "flame_sprite",
                new FlameSpriteSpell(),
                100,
                SpellCost.builder()
                        .addCost(Items.MAGMA_CREAM, 1)
                        .addCost(Items.SOUL_SAND, 1)
                        .addCost(Items.GOLD_NUGGET, 2)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "cone_of_cold",
                new ConeOfColdSpell(),
                120,
                SpellCost.builder()
                        .addCost(Items.BLUE_ICE, 1)
                        .addCost(Items.LAPIS_LAZULI, 2)
                        .addCost(Items.GOLD_INGOT, 1)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "dragon_sprite",
                new DragonSpriteSpell(),
                180,
                SpellCost.builder()
                        .addCost(Items.DRAGON_BREATH, 1)
                        .addCost(Items.SOUL_SAND, 2)
                        .addCost(Items.GOLD_INGOT, 1)
                        .addCost(Items.EMERALD, 1)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "fly",
                new FlySpell(),
                200,
                SpellCost.builder()
                        .addCost(Items.PHANTOM_MEMBRANE, 2)
                        .addCost(Items.FEATHER, 4)
                        .addCost(Items.GOLD_INGOT, 2)
                        .addCost(Items.DIAMOND, 1)
        ));
    }
}