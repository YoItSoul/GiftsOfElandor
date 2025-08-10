package com.soul.goe.items.custom;

import com.soul.goe.spells.util.SpellData;
import com.soul.goe.spells.util.SpellRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.Optional;

public class Spell extends Item {
    private final String spellId;
    private final int maxUses;
    private final int durabilityPerUse;
    private final boolean hasFoil;

    public Spell(Properties properties, String spellId, int maxUses) {
        super(properties.durability(maxUses));
        this.spellId = spellId;
        this.maxUses = maxUses;
        this.durabilityPerUse = 1;
        this.hasFoil = false;
    }

    public Spell(Properties properties, String spellId, int maxUses, int durabilityPerUse) {
        super(properties.durability(maxUses));
        this.spellId = spellId;
        this.maxUses = maxUses;
        this.durabilityPerUse = durabilityPerUse;
        this.hasFoil = false;
    }

    public Spell(Properties properties, String spellId, int maxUses, boolean hasFoil) {
        super(properties.durability(maxUses));
        this.spellId = spellId;
        this.maxUses = maxUses;
        this.durabilityPerUse = 1;
        this.hasFoil = hasFoil;
    }

    public Spell(Properties properties, String spellId, int maxUses, int durabilityPerUse, boolean hasFoil) {
        super(properties.durability(maxUses));
        this.spellId = spellId;
        this.maxUses = maxUses;
        this.durabilityPerUse = durabilityPerUse;
        this.hasFoil = hasFoil;
    }

    public String getSpellId() {
        return spellId;
    }

    public Optional<SpellData> getSpellData() {
        return SpellRegistry.getSpell(spellId);
    }

    public boolean canCast(ItemStack stack) {
        return stack.getDamageValue() < stack.getMaxDamage();
    }

    public void consumeUse(ItemStack stack, Player player) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            stack.hurtAndBreak(durabilityPerUse, serverLevel, player, item -> {});
        }
    }

    public int getRemainingUses(ItemStack stack) {
        return Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasFoil || super.isFoil(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / (float)stack.getMaxDamage());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float)stack.getMaxDamage() - (float)stack.getDamageValue()) / (float)stack.getMaxDamage());
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public static Optional<String> getSpellIdFromStack(ItemStack stack) {
        if (stack.getItem() instanceof Spell spellItem) {
            return Optional.of(spellItem.getSpellId());
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("SpellId")) {
                return Optional.of(tag.getString("SpellId"));
            }
        }

        return Optional.empty();
    }

    public static ItemStack createSpellStack(Item item, String spellId) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        tag.putString("SpellId", spellId);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
}