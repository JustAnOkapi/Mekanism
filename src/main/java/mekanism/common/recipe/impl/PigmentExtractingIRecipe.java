package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;

public class PigmentExtractingIRecipe extends ItemStackToPigmentRecipe {

    public PigmentExtractingIRecipe(ResourceLocation id, ItemStackIngredient input, PigmentStack output) {
        super(id, input, output);
    }

    @Nonnull
    @Override
    public RecipeType<ItemStackToPigmentRecipe> getType() {
        return MekanismRecipeType.PIGMENT_EXTRACTING;
    }

    @Nonnull
    @Override
    public RecipeSerializer<ItemStackToPigmentRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_EXTRACTING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.PIGMENT_EXTRACTOR.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PIGMENT_EXTRACTOR.getItemStack();
    }
}