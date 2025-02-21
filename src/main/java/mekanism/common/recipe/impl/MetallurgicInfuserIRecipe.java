package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;

public class MetallurgicInfuserIRecipe extends MetallurgicInfuserRecipe {

    public MetallurgicInfuserIRecipe(ResourceLocation id, ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        super(id, itemInput, infusionInput, output);
    }

    @Nonnull
    @Override
    public RecipeType<MetallurgicInfuserRecipe> getType() {
        return MekanismRecipeType.METALLURGIC_INFUSING;
    }

    @Nonnull
    @Override
    public RecipeSerializer<MetallurgicInfuserRecipe> getSerializer() {
        return MekanismRecipeSerializers.METALLURGIC_INFUSING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.METALLURGIC_INFUSER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.METALLURGIC_INFUSER.getItemStack();
    }
}