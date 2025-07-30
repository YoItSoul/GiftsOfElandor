package com.soul.goe.spells;

import com.soul.goe.spells.types.*;
import net.minecraft.world.item.Items;

public class SpellInit {
    public static void registerSpells() {
        SpellRegistry.registerSpell(new SpellData(
                "fire_bolt",
                new FireboltSpell(),
                60
        ));

        SpellRegistry.registerSpell(new SpellData(
                "ray_of_frost",
                new RayOfFrostSpell(),
                60
        ));

        SpellRegistry.registerSpell(new SpellData(
                "shocking_grasp",
                new ShockingGraspSpell(),
                60
        ));

        SpellRegistry.registerSpell(new SpellData(
                "magic_missile",
                new MagicMissileSpell(),
                60,
                SpellCost.builder()
                        .addCost(Items.GLOWSTONE_DUST, 3)
                        .addCost(Items.GOLD_NUGGET, 5)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "fireball",
                new FireballSpell(),
                60,
                SpellCost.builder()
                        .addCost(Items.FIRE_CHARGE, 1)
                        .addCost(Items.GUNPOWDER, 1)
                        .addCost(Items.GOLD_INGOT, 3)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "cone_of_cold",
                new ConeOfColdSpell(),
                60,
                SpellCost.builder()
                        .addCost(Items.PACKED_ICE, 2)
                        .addCost(Items.SNOWBALL, 8)
                        .addCost(Items.DIAMOND, 1)
                        .addCost(Items.LAPIS_LAZULI, 5)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "lightning_bolt",
                new LightningBoltSpell(),
                60,
                SpellCost.builder()
                        .addCost(Items.COPPER_INGOT, 3)
                        .addCost(Items.REDSTONE, 8)
                        .addCost(Items.GOLD_INGOT, 2)
                        .addCost(Items.QUARTZ, 4)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "levitate",
                new LevitateSpell(),
                60,
                SpellCost.builder()
                        .addCost(Items.FEATHER, 4)
                        .addCost(Items.ENDER_PEARL, 1)
                        .addCost(Items.GOLD_INGOT, 2)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "lunge",
                new LungeSpell(),
                60,
                SpellCost.builder()
                        .addCost(Items.FEATHER, 4)

        ));

        SpellRegistry.registerSpell(new SpellData(
                "fly",
                new FlySpell(),
                60,
                SpellCost.builder()
                        .addCost(Items.ELYTRA, 1)
                        .addCost(Items.PHANTOM_MEMBRANE, 3)
                        .addCost(Items.DIAMOND, 2)
                        .addCost(Items.EMERALD, 1)
        ));

        SpellRegistry.registerSpell(new SpellData(
                "frost_sprite",
                new FrostSpriteSpell(),
                60,
                SpellCost.builder()
                        .addCost(Items.SNOWBALL, 4)
                        .addCost(Items.ICE, 2)
                        .addCost(Items.SOUL_SAND, 1)
                        .addCost(Items.GOLD_NUGGET, 3)
        ));

    }
}