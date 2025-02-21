package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.base.IData;
import com.blamejared.crafttweaker.api.data.base.converter.JSONConverter;
import com.blamejared.crafttweaker.api.tag.MCTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.tag.CrTInfuseTypeTagManager;
import net.minecraft.tags.Tag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = InfusionStackIngredient.class, zenCodeName = CrTConstants.CLASS_INFUSION_STACK_INGREDIENT)
public class CrTInfusionStackIngredient {

    private CrTInfusionStackIngredient() {
    }

    /**
     * Creates a {@link InfusionStackIngredient} that matches a given infuse type and amount.
     *
     * @param instance Infuse type to match
     * @param amount   Amount needed
     *
     * @return A {@link InfusionStackIngredient} that matches a given infuse type and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient from(InfuseType instance, long amount) {
        CrTIngredientHelper.assertValid(instance, amount, "InfusionStackIngredients", "infuse type");
        return InfusionStackIngredient.from(instance, amount);
    }

    /**
     * Creates a {@link InfusionStackIngredient} that matches a given infusion stack.
     *
     * @param instance Infusion stack to match
     *
     * @return A {@link InfusionStackIngredient} that matches a given infusion stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient from(ICrTInfusionStack instance) {
        CrTIngredientHelper.assertValid(instance, "InfusionStackIngredients");
        return InfusionStackIngredient.from(instance.getImmutableInternal());
    }

    /**
     * Creates a {@link InfusionStackIngredient} that matches a given infuse type tag with a given amount.
     *
     * @param infuseTypeTag Tag to match
     * @param amount        Amount needed
     *
     * @return A {@link InfusionStackIngredient} that matches a given infuse type tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient from(MCTag<InfuseType> infuseTypeTag, long amount) {
        Tag<InfuseType> tag = CrTIngredientHelper.assertValidAndGet(infuseTypeTag, amount, CrTInfuseTypeTagManager.INSTANCE::getInternal, "InfusionStackIngredients");
        return InfusionStackIngredient.from(tag, amount);
    }

    /**
     * Creates a {@link InfusionStackIngredient} that matches a given infuse type tag with amount.
     *
     * @param infuseTypeTag Tag and amount to match
     *
     * @return A {@link InfusionStackIngredient} that matches a given infuse type tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient from(Many<MCTag<InfuseType>> infuseTypeTag) {
        return from(infuseTypeTag.getData(), infuseTypeTag.getAmount());
    }

    /**
     * Combines multiple {@link InfusionStackIngredient}s into a single {@link InfusionStackIngredient}.
     *
     * @param ingredients Ingredients to combine
     *
     * @return A single {@link InfusionStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.StaticExpansionMethod
    public static InfusionStackIngredient createMulti(InfusionStackIngredient... ingredients) {
        return CrTIngredientHelper.createMulti("InfusionStackIngredients", InfusionStackIngredient::createMulti, ingredients);
    }

    /**
     * Converts this {@link InfusionStackIngredient} into JSON ({@link IData}).
     *
     * @return {@link InfusionStackIngredient} as JSON.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static IData asIData(InfusionStackIngredient _this) {
        return JSONConverter.convert(_this.serialize());
    }

    /**
     * Checks if a given {@link ICrTInfusionStack} has a type match for this {@link InfusionStackIngredient}. Type matches ignore stack size.
     *
     * @param type Type to check for a match
     *
     * @return {@code true} if the type is supported by this {@link InfusionStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean testType(InfusionStackIngredient _this, ICrTInfusionStack type) {
        return _this.testType(type.getInternal());
    }

    /**
     * Checks if a given {@link ICrTInfusionStack} matches this {@link InfusionStackIngredient}. (Checks size for >=)
     *
     * @param stack Stack to check for a match
     *
     * @return {@code true} if the stack fulfills the requirements for this {@link InfusionStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean test(InfusionStackIngredient _this, ICrTInfusionStack stack) {
        return _this.test(stack.getInternal());
    }

    /**
     * Gets a list of valid instances for this {@link InfusionStackIngredient}, may not include all or may be empty depending on how complex the ingredient is as the
     * internal version is mostly used for JEI display purposes.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("representations")
    public static List<ICrTInfusionStack> getRepresentations(InfusionStackIngredient _this) {
        return CrTUtils.convert(_this.getRepresentations(), CrTInfusionStack::new);
    }

    /**
     * OR's this {@link InfusionStackIngredient} with another {@link InfusionStackIngredient} to create a multi {@link InfusionStackIngredient}
     *
     * @param other {@link InfusionStackIngredient} to combine with.
     *
     * @return Multi {@link InfusionStackIngredient} that matches both the source {@link InfusionStackIngredient} and the OR'd {@link InfusionStackIngredient}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.OR)
    public static InfusionStackIngredient or(InfusionStackIngredient _this, InfusionStackIngredient other) {
        return InfusionStackIngredient.createMulti(_this, other);
    }
}