package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;

public class NucleosynthesizingIRecipe extends NucleosynthesizingRecipe {

    public NucleosynthesizingIRecipe(ResourceLocation id, ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output, int duration) {
        super(id, itemInput, gasInput, output, duration);
    }

    @Nonnull
    @Override
    public RecipeType<NucleosynthesizingRecipe> getType() {
        return MekanismRecipeType.NUCLEOSYNTHESIZING;
    }

    @Nonnull
    @Override
    public RecipeSerializer<NucleosynthesizingRecipe> getSerializer() {
        return MekanismRecipeSerializers.NUCLEOSYNTHESIZING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER.getItemStack();
    }
}