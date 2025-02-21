package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;

class MetallurgicInfuserRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "metallurgic_infusing/";
        addMetallurgicInfuserAlloyRecipes(consumer, basePath + "alloy/");
        addMetallurgicInfuserMossyRecipes(consumer, basePath + "mossy/");
        //Dirt -> mycelium
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.DIRT),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.FUNGI, 10),
              new ItemStack(Blocks.MYCELIUM)
        ).build(consumer, Mekanism.rl(basePath + "dirt_to_mycelium"));
        //Netherrack -> crimson nylium
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Tags.Items.NETHERRACK),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.FUNGI, 10),
              new ItemStack(Blocks.CRIMSON_NYLIUM)
        ).build(consumer, Mekanism.rl(basePath + "netherrack_to_crimson_nylium"));
        //Crimson nylium -> warped nylium
        //Note: We use crimson as the base so that it is easy to "specify" which output is desired
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.CRIMSON_NYLIUM),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.FUNGI, 10),
              new ItemStack(Blocks.WARPED_NYLIUM)
        ).build(consumer, Mekanism.rl(basePath + "crimson_nylium_to_warped_nylium"));
        //Dirt -> podzol
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.DIRT),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.PODZOL)
        ).build(consumer, Mekanism.rl(basePath + "dirt_to_podzol"));
        //Sand -> dirt
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Tags.Items.SAND),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.DIRT)
        ).build(consumer, Mekanism.rl(basePath + "sand_to_dirt"));
        //Blackstone -> Gilded Blackstone
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.BLACKSTONE),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.GOLD, 100),
              new ItemStack(Blocks.GILDED_BLACKSTONE)
        ).build(consumer, Mekanism.rl(basePath + "blackstone_to_gilded_blackstone"));
    }

    private void addMetallurgicInfuserAlloyRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Infused
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Tags.Items.INGOTS_IRON),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.REDSTONE, 10),
              MekanismItems.INFUSED_ALLOY.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "infused"));
        //Reinforced
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.ALLOYS_INFUSED),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.DIAMOND, 20),
              MekanismItems.REINFORCED_ALLOY.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "reinforced"));
        //Atomic
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.ALLOYS_REINFORCED),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.REFINED_OBSIDIAN, 40),
              MekanismItems.ATOMIC_ALLOY.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "atomic"));
    }

    private void addMetallurgicInfuserMossyRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Cobblestone
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.COBBLESTONE),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.MOSSY_COBBLESTONE)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone"));
        //Cobblestone slab
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.COBBLESTONE_SLAB),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.MOSSY_COBBLESTONE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_slab"));
        //Cobblestone stairs
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.COBBLESTONE_STAIRS),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.MOSSY_COBBLESTONE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_stairs"));
        //Cobblestone wall
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.COBBLESTONE_WALL),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.MOSSY_COBBLESTONE_WALL)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_wall"));

        //Stone brick
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.STONE_BRICKS),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.MOSSY_STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "stone_brick"));
        //Stone brick slab
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.STONE_BRICK_SLAB),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.MOSSY_STONE_BRICK_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "stone_brick_slab"));
        //Stone brick stairs
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.STONE_BRICK_STAIRS),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.MOSSY_STONE_BRICK_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stone_brick_stairs"));
        //Stone brick wall
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Blocks.STONE_BRICK_WALL),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Blocks.MOSSY_STONE_BRICK_WALL)
        ).build(consumer, Mekanism.rl(basePath + "stone_brick_wall"));
    }
}