package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient;

public class ChemicalInputCache<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends MekanismRecipe>
      extends BaseInputCache<CHEMICAL, STACK, ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, ChemicalStackIngredient<CHEMICAL, STACK> inputIngredient) {
        if (inputIngredient instanceof ChemicalStackIngredient.SingleIngredient<CHEMICAL, STACK> single) {
            CHEMICAL input = single.getInputRaw();
            addInputCache(input, recipe);
        } else if (inputIngredient instanceof ChemicalStackIngredient.TaggedIngredient<CHEMICAL, STACK> tagged) {
            for (CHEMICAL input : tagged.getRawInput()) {
                addInputCache(input, recipe);
            }
        } else if (inputIngredient instanceof ChemicalStackIngredient.MultiIngredient<CHEMICAL, STACK, ?> multi) {
            return multi.forEachIngredient(ingredient -> mapInputs(recipe, ingredient));
        } else {
            //This should never really happen as we don't really allow for custom ingredients especially for networking,
            // but if it does add it as a fallback
            return true;
        }
        return false;
    }

    @Override
    protected CHEMICAL createKey(STACK stack) {
        return stack.getType();
    }

    @Override
    public boolean isEmpty(STACK input) {
        return input.isEmpty();
    }
}