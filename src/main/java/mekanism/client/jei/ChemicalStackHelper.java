package mekanism.client.jei;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IEmptyStackProvider;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IEmptyGasProvider;
import mekanism.api.chemical.infuse.IEmptyInfusionProvider;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IEmptyPigmentProvider;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.IEmptySlurryProvider;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.util.ChemicalUtil;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public abstract class ChemicalStackHelper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IIngredientHelper<STACK>,
      IEmptyStackProvider<CHEMICAL, STACK> {

    @Nullable
    private IColorHelper colorHelper;

    void setColorHelper(IColorHelper colorHelper) {
        this.colorHelper = colorHelper;
    }

    protected abstract String getType();

    @Nullable
    @Override
    public STACK getMatch(Iterable<STACK> ingredients, @Nonnull STACK toMatch, UidContext context) {
        for (STACK stack : ingredients) {
            if (toMatch.isTypeEqual(stack)) {
                return stack;
            }
        }
        //JEI expects null to be returned if there is no match  so that it can filter hidden ingredients
        return null;
    }

    @Override
    public String getDisplayName(STACK ingredient) {
        return TextComponentUtil.build(ingredient).getString();
    }

    @Override
    public String getUniqueId(STACK ingredient, UidContext context) {
        return getType().toLowerCase(Locale.ROOT) + ":" + ingredient.getTypeRegistryName();
    }

    @Override
    public String getModId(STACK ingredient) {
        return ingredient.getTypeRegistryName().getNamespace();
    }

    @Override
    public Iterable<Integer> getColors(STACK ingredient) {
        if (colorHelper == null) {
            return IIngredientHelper.super.getColors(ingredient);
        }
        CHEMICAL chemical = ingredient.getType();
        //TODO: Does tint need alpha applied/factored in to getting the color, Either way this is waiting on https://github.com/mezz/JustEnoughItems/issues/1886
        return colorHelper.getColors(MekanismRenderer.getChemicalTexture(chemical), chemical.getTint(), 1);
    }

    @Override
    public String getResourceId(STACK ingredient) {
        return ingredient.getTypeRegistryName().getPath();
    }

    @Override
    public STACK copyIngredient(STACK ingredient) {
        return ChemicalUtil.copy(ingredient);
    }

    @Override
    public Collection<ResourceLocation> getTags(STACK ingredient) {
        return ingredient.getType().getTags();
    }

    @Override
    public String getErrorInfo(@Nullable STACK ingredient) {
        if (ingredient == null) {
            ingredient = getEmptyStack();
        }
        ToStringHelper toStringHelper = MoreObjects.toStringHelper(GasStack.class);
        CHEMICAL chemical = ingredient.getType();
        toStringHelper.add(getType(), chemical.isEmptyType() ? "none" : TextComponentUtil.build(chemical).getString());
        if (!ingredient.isEmpty()) {
            toStringHelper.add("Amount", ingredient.getAmount());
        }
        return toStringHelper.toString();
    }

    @Nullable
    protected MekanismRecipeType<? extends ItemStackToChemicalRecipe<CHEMICAL, STACK>, ?> getConversionRecipeType() {
        return null;
    }

    public List<ItemStack> getStacksFor(@Nonnull CHEMICAL type, boolean displayConversions) {
        if (type.isEmptyType()) {
            return Collections.emptyList();
        }
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return Collections.emptyList();
        }
        List<ItemStack> stacks = new ArrayList<>();
        //Always include the chemical tank of the type to portray that we accept items
        stacks.add(ChemicalUtil.getFullChemicalTank(ChemicalTankTier.BASIC, type));
        if (displayConversions) {
            //See if there are any chemical to item mappings
            MekanismRecipeType<? extends ItemStackToChemicalRecipe<CHEMICAL, STACK>, ?> recipeType = getConversionRecipeType();
            if (recipeType != null) {
                for (ItemStackToChemicalRecipe<CHEMICAL, STACK> recipe : recipeType.getRecipes(world)) {
                    if (recipe.getOutputDefinition().stream().anyMatch(output -> output.isTypeEqual(type))) {
                        stacks.addAll(recipe.getInput().getRepresentations());
                    }
                }
            }
        }
        return stacks;
    }

    public static class GasStackHelper extends ChemicalStackHelper<Gas, GasStack> implements IEmptyGasProvider {

        @Override
        protected String getType() {
            return "Gas";
        }

        @Override
        protected MekanismRecipeType<? extends ItemStackToChemicalRecipe<Gas, GasStack>, ?> getConversionRecipeType() {
            return MekanismRecipeType.GAS_CONVERSION;
        }

        @Override
        public IIngredientType<GasStack> getIngredientType() {
            return MekanismJEI.TYPE_GAS;
        }
    }

    public static class InfusionStackHelper extends ChemicalStackHelper<InfuseType, InfusionStack> implements IEmptyInfusionProvider {

        @Override
        protected String getType() {
            return "Infuse Type";
        }

        @Override
        protected MekanismRecipeType<? extends ItemStackToChemicalRecipe<InfuseType, InfusionStack>, ?> getConversionRecipeType() {
            return MekanismRecipeType.INFUSION_CONVERSION;
        }

        @Override
        public IIngredientType<InfusionStack> getIngredientType() {
            return MekanismJEI.TYPE_INFUSION;
        }
    }

    public static class PigmentStackHelper extends ChemicalStackHelper<Pigment, PigmentStack> implements IEmptyPigmentProvider {

        @Override
        protected String getType() {
            return "Pigment";
        }

        @Override
        public IIngredientType<PigmentStack> getIngredientType() {
            return MekanismJEI.TYPE_PIGMENT;
        }
    }

    public static class SlurryStackHelper extends ChemicalStackHelper<Slurry, SlurryStack> implements IEmptySlurryProvider {

        @Override
        protected String getType() {
            return "Slurry";
        }

        @Override
        public IIngredientType<SlurryStack> getIngredientType() {
            return MekanismJEI.TYPE_SLURRY;
        }
    }
}