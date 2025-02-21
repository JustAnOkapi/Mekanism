package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToEnergyRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

class EnergyConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "energy_conversion/";
        FloatingLong redstoneEnergy = FloatingLong.createConst(10_000);
        addEnergyConversionRecipe(consumer, basePath, "redstone", Tags.Items.DUSTS_REDSTONE, redstoneEnergy);
        addEnergyConversionRecipe(consumer, basePath, "redstone_block", Tags.Items.STORAGE_BLOCKS_REDSTONE, redstoneEnergy.multiply(9));
    }

    private void addEnergyConversionRecipe(Consumer<FinishedRecipe> consumer, String basePath, String name, Tag<Item> inputTag, FloatingLong output) {
        ItemStackToEnergyRecipeBuilder.energyConversion(
              ItemStackIngredient.from(inputTag),
              output
        ).build(consumer, Mekanism.rl(basePath + name));
    }
}