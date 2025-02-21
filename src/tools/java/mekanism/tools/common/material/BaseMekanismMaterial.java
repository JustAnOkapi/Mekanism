package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.tools.common.MekanismTools;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public abstract class BaseMekanismMaterial extends IItemTierHelper implements IArmorMaterialHelper, IPaxelMaterial {

    @Nullable
    @Override
    public abstract Tag<Block> getTag();//Force this to be implemented

    public abstract int getShieldDurability();

    public float getSwordDamage() {
        return 3;
    }

    public float getSwordAtkSpeed() {
        return -2.4F;
    }

    public float getShovelDamage() {
        return 1.5F;
    }

    public float getShovelAtkSpeed() {
        return -3.0F;
    }

    public abstract float getAxeDamage();

    public abstract float getAxeAtkSpeed();

    public float getPickaxeDamage() {
        return 1;
    }

    public float getPickaxeAtkSpeed() {
        return -2.8F;
    }

    public float getHoeDamage() {
        //Default to match the vanilla hoe's implementation of being negative the attack damage of the material
        return -getAttackDamageBonus();
    }

    public float getHoeAtkSpeed() {
        return getAttackDamageBonus() - 3.0F;
    }

    @Override
    public float getPaxelDamage() {
        return getAxeDamage() + 1;
    }

    @Override
    public int getPaxelMaxUses() {
        return 2 * getUses();
    }

    @Override
    public float getPaxelEfficiency() {
        return getSpeed();
    }

    @Override
    public int getPaxelEnchantability() {
        return getCommonEnchantability();
    }

    @Nonnull
    public abstract String getRegistryPrefix();

    //Recombine the methods that are split in such a way as to make it so the compiler can reobfuscate them properly
    public abstract int getCommonEnchantability();

    public boolean burnsInFire() {
        return true;
    }

    @Override
    public int getItemEnchantability() {
        return getCommonEnchantability();
    }

    @Override
    public int getArmorEnchantability() {
        return getCommonEnchantability();
    }

    @Nonnull
    public abstract Ingredient getCommonRepairMaterial();

    @Nonnull
    @Override
    public Ingredient getItemRepairMaterial() {
        return getCommonRepairMaterial();
    }

    @Nonnull
    @Override
    public Ingredient getArmorRepairMaterial() {
        return getCommonRepairMaterial();
    }

    @Nonnull
    @Override
    public String getName() {
        return MekanismTools.MODID + ":" + getRegistryPrefix();
    }
}