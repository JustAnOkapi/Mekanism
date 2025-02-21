package mekanism.client.jei.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.machine.GuiChemicalCrystallizer;
import mekanism.client.gui.machine.GuiChemicalCrystallizer.IOreInfo;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChemicalCrystallizerRecipeCategory extends BaseRecipeCategory<ChemicalCrystallizerRecipe> {

    //Note: We use a weak hashmap so that when the recipe stops existing either due to disconnecting from the server
    // or because of a reload, then it can be properly garbage collected, but until then we keep track of the pairing
    // between the recipe and the ingredient group JEI has so that we can ensure we draw the correct information
    private final Map<ChemicalCrystallizerRecipe, IngredientTarget> ingredients = new WeakHashMap<>();
    private final OreInfo oreInfo = new OreInfo();
    private final GuiGauge<?> gauge;
    private final GuiSlot output;
    private final GuiSlot slurryOreSlot;

    public ChemicalCrystallizerRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_CRYSTALLIZER, 5, 3, 147, 79);
        gauge = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 4));
        addSlot(SlotType.INPUT, 8, 65).with(SlotOverlay.PLUS);
        output = addSlot(SlotType.OUTPUT, 129, 57);
        addSimpleProgress(ProgressType.LARGE_RIGHT, 53, 61);
        addElement(new GuiInnerScreen(this, 31, 13, 115, 42, () -> GuiChemicalCrystallizer.getScreenRenderStrings(this.oreInfo)));
        slurryOreSlot = addElement(new GuiSlot(SlotType.ORE, this, 128, 13).setRenderAboveSlots());
    }

    @Override
    public Class<? extends ChemicalCrystallizerRecipe> getRecipeClass() {
        return ChemicalCrystallizerRecipe.class;
    }

    @Override
    public void draw(ChemicalCrystallizerRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our ore info
        oreInfo.currentRecipe = recipe;
        IngredientTarget target = ingredients.get(recipe);
        if (target != null) {
            oreInfo.ingredient = target.ingredientGroup.getGuiIngredients().get(0);
            oreInfo.itemIngredient = target.itemIngredientGroup == null ? null : target.itemIngredientGroup.getGuiIngredients().get(1);
        }
        super.draw(recipe, matrixStack, mouseX, mouseY);
        oreInfo.currentRecipe = null;
        oreInfo.ingredient = null;
        oreInfo.itemIngredient = null;
    }

    @Override
    public void setIngredients(ChemicalCrystallizerRecipe recipe, IIngredients ingredients) {
        ChemicalStackIngredient<?, ?> input = recipe.getInput();
        if (input instanceof GasStackIngredient ingredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(ingredient.getRepresentations()));
        } else if (input instanceof InfusionStackIngredient ingredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_INFUSION, Collections.singletonList(ingredient.getRepresentations()));
        } else if (input instanceof PigmentStackIngredient ingredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_PIGMENT, Collections.singletonList(ingredient.getRepresentations()));
        } else if (input instanceof SlurryStackIngredient ingredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_SLURRY, Collections.singletonList(ingredient.getRepresentations()));
        }
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ChemicalCrystallizerRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        initItem(itemStacks, 0, false, output, recipe.getOutputDefinition());
        ChemicalStackIngredient<?, ?> input = recipe.getInput();
        if (input instanceof GasStackIngredient ingredient) {
            initChemical(recipeLayout, recipe, MekanismJEI.TYPE_GAS, ingredient, null);
        } else if (input instanceof InfusionStackIngredient ingredient) {
            initChemical(recipeLayout, recipe, MekanismJEI.TYPE_INFUSION, ingredient, null);
        } else if (input instanceof PigmentStackIngredient ingredient) {
            initChemical(recipeLayout, recipe, MekanismJEI.TYPE_PIGMENT, ingredient, null);
        } else if (input instanceof SlurryStackIngredient ingredient) {
            Set<Tag<Item>> tags = new HashSet<>();
            for (SlurryStack slurryStack : ingredient.getRepresentations()) {
                Slurry slurry = slurryStack.getType();
                if (!slurry.isIn(MekanismTags.Slurries.DIRTY)) {
                    Tag<Item> oreTag = slurry.getOreTag();
                    if (oreTag != null) {
                        tags.add(oreTag);
                    }
                }
            }
            if (tags.size() == 1) {
                initChemical(recipeLayout, recipe, MekanismJEI.TYPE_SLURRY, ingredient, itemStacks);
                //TODO: Eventually come up with a better way to do this to allow for if there outputs based on the input and multiple input types
                tags.stream().findFirst().ifPresent(tag -> initItem(itemStacks, 1, false, slurryOreSlot,
                      tag.getValues().stream().map(ItemStack::new).toList()));
            } else {
                initChemical(recipeLayout, recipe, MekanismJEI.TYPE_SLURRY, ingredient, null);
            }
        }
    }

    private <STACK extends ChemicalStack<?>> void initChemical(IRecipeLayout recipeLayout, ChemicalCrystallizerRecipe recipe, IIngredientType<STACK> type,
          ChemicalStackIngredient<?, STACK> ingredient, @Nullable IGuiItemStackGroup itemIngredientGroup) {
        IGuiIngredientGroup<STACK> stacks = recipeLayout.getIngredientsGroup(type);
        initChemical(stacks, 0, true, gauge, ingredient.getRepresentations());
        this.ingredients.put(recipe, new IngredientTarget(stacks, itemIngredientGroup));
    }

    private record IngredientTarget(IGuiIngredientGroup<? extends ChemicalStack<?>> ingredientGroup, @Nullable IGuiItemStackGroup itemIngredientGroup) {
    }

    private static class OreInfo implements IOreInfo {

        @Nullable
        private ChemicalCrystallizerRecipe currentRecipe;
        @Nullable
        private IGuiIngredient<? extends ChemicalStack<?>> ingredient;
        @Nullable
        private IGuiIngredient<ItemStack> itemIngredient;

        @Nonnull
        @Override
        public BoxedChemicalStack getInputChemical() {
            if (ingredient == null) {
                return BoxedChemicalStack.EMPTY;
            }
            ChemicalStack<?> displayedIngredient = ingredient.getDisplayedIngredient();
            return displayedIngredient == null || displayedIngredient.isEmpty() ? BoxedChemicalStack.EMPTY : BoxedChemicalStack.box(displayedIngredient);
        }

        @Nullable
        @Override
        public ChemicalCrystallizerRecipe getRecipe() {
            return currentRecipe;
        }

        @Override
        @Nonnull
        public ItemStack getRenderStack() {
            if (itemIngredient == null) {
                return ItemStack.EMPTY;
            }
            ItemStack displayedIngredient = itemIngredient.getDisplayedIngredient();
            return displayedIngredient == null ? ItemStack.EMPTY : displayedIngredient;
        }
    }
}