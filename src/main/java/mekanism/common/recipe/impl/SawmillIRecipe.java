package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;

public class SawmillIRecipe extends SawmillRecipe {

    public SawmillIRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        super(id, input, mainOutput, secondaryOutput, secondaryChance);
    }

    @Nonnull
    @Override
    public RecipeType<SawmillRecipe> getType() {
        return MekanismRecipeType.SAWING;
    }

    @Nonnull
    @Override
    public RecipeSerializer<SawmillRecipe> getSerializer() {
        return MekanismRecipeSerializers.SAWING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.PRECISION_SAWMILL.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PRECISION_SAWMILL.getItemStack();
    }
}