package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

public class LapisLazuliMaterialDefaults extends BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 224;
    }

    @Override
    public float getAxeDamage() {
        return 4;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -2.9F;
    }

    @Override
    public int getUses() {
        return 128;
    }

    @Override
    public float getSpeed() {
        return 9;
    }

    @Override
    public float getAttackDamageBonus() {
        return 1;
    }

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public int getCommonEnchantability() {
        return 32;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public int getDurabilityForSlot(@Nonnull EquipmentSlot slotType) {
        switch (slotType) {
            case FEET:
                return 130;
            case LEGS:
                return 150;
            case CHEST:
                return 160;
            case HEAD:
                return 110;
        }
        return 0;
    }

    @Override
    public int getDefenseForSlot(@Nonnull EquipmentSlot slotType) {
        switch (slotType) {
            case FEET:
                return 1;
            case LEGS:
                return 3;
            case CHEST:
                return 4;
            case HEAD:
                return 1;
        }
        return 0;
    }

    @Nonnull
    @Override
    public String getConfigCommentName() {
        return "Lapis Lazuli";
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "lapis_lazuli";
    }

    @Nullable
    @Override
    public Tag<Block> getTag() {
        return ToolsTags.Blocks.NEEDS_LAPIS_LAZULI_TOOL;
    }

    @Nonnull
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_DIAMOND;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.of(Tags.Items.GEMS_LAPIS);
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}