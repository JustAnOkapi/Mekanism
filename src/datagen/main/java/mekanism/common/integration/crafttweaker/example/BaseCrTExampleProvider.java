package mekanism.common.integration.crafttweaker.example;

import com.blamejared.crafttweaker.api.bracket.CommandStringDisplayable;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.fluid.MCFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker.api.tag.manager.TagManagerFluid;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import com.blamejared.crafttweaker.api.util.random.Percentaged;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.ChemicalIngredientDeserializer;
import mekanism.api.recipes.inputs.chemical.ChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.integration.crafttweaker.example.component.CrTImportsComponent;
import mekanism.common.integration.crafttweaker.recipe.handler.MekanismRecipeHandler;
import mekanism.common.integration.crafttweaker.tag.CrTChemicalTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTGasTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTInfuseTypeTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTPigmentTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTSlurryTagManager;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class BaseCrTExampleProvider implements DataProvider {

    public static final Map<String, Map<String, List<String>>> PARAMETER_NAMES = new HashMap<>();

    private final Map<Class<?>, ConversionTracker> supportedConversions = new HashMap<>();
    private final Map<String, CrTExampleBuilder<?>> examples = new LinkedHashMap<>();
    private final Map<Class<?>, String> nameLookupOverrides = new HashMap<>();
    private final ExistingFileHelper existingFileHelper;
    private final DataGenerator gen;
    private final String modid;

    protected BaseCrTExampleProvider(DataGenerator gen, ExistingFileHelper existingFileHelper, String modid) {
        this.gen = gen;
        this.existingFileHelper = existingFileHelper;
        this.modid = modid;
        addNameLookupOverride(String.class, "string");
        addPrimitiveInfo(Byte.TYPE, Byte.class, "byte");
        addPrimitiveInfo(Short.TYPE, Short.class, "short");
        addPrimitiveInfo(Integer.TYPE, Integer.class, "int");
        addPrimitiveInfo(Long.TYPE, Long.class, "long");
        addPrimitiveInfo(Float.TYPE, Float.class, "float");
        addPrimitiveInfo(Double.TYPE, Double.class, "double");
        addNameLookupOverride(Boolean.TYPE, "bool");
        addPrimitiveInfo(Boolean.TYPE, Boolean.class, "bool");
        addNameLookupOverride(Character.class, "char");
        addSupportedConversion(Character.TYPE, Character.class, (imports, c) -> "'" + c + "'");
        addSupportedConversion(IItemStack.class, ItemStack.class, (imports, stack) -> ItemStackUtil.getCommandString(stack));
        addSupportedConversion(IFluidStack.class, FluidStack.class, (imports, stack) -> new MCFluidStack(stack).getCommandString());
        addSupportedConversion(Percentaged.class, IItemStack.class, WeightedItemStack.class,
              (imports, stack) -> new MCItemStack(stack.stack).percent(stack.chance).getCommandString(),
              (imports, stack) -> {
                  if (stack.chance == 1) {
                      return ItemStackUtil.getCommandString(stack.stack);
                  }
                  return null;
              }
        );
        addSupportedConversion(FloatingLong.class, FloatingLong.class, (imports, fl) -> {
            String path = imports.addImport(CrTConstants.CLASS_FLOATING_LONG);
            if (fl.getDecimal() == 0 && fl.getValue() > Integer.MAX_VALUE) {
                return path + ".createFromUnsigned(" + fl + ")";
            }
            return path + ".create(" + fl + ")";
        }, (imports, fl) -> {
            if (fl.getDecimal() == 0) {
                //No decimal, don't bother printing it
                return fl.toString(0);
            }
            //Trim any trailing zeros rather than printing them out
            return fl.toString().replaceAll("0*$", "");
        });
        addItemStackIngredientSupport();
        addFluidStackIngredientSupport();
        addSupportedChemical(GasStack.class, ICrTGasStack.class, GasStackIngredient.class, CrTConstants.CLASS_GAS_STACK_INGREDIENT, CrTGasStack::new,
              CrTGasTagManager.INSTANCE, ChemicalIngredientDeserializer.GAS);
        addSupportedChemical(InfusionStack.class, ICrTInfusionStack.class, InfusionStackIngredient.class, CrTConstants.CLASS_INFUSION_STACK_INGREDIENT,
              CrTInfusionStack::new, CrTInfuseTypeTagManager.INSTANCE, ChemicalIngredientDeserializer.INFUSION);
        addSupportedChemical(PigmentStack.class, ICrTPigmentStack.class, PigmentStackIngredient.class, CrTConstants.CLASS_PIGMENT_STACK_INGREDIENT,
              CrTPigmentStack::new, CrTPigmentTagManager.INSTANCE, ChemicalIngredientDeserializer.PIGMENT);
        addSupportedChemical(SlurryStack.class, ICrTSlurryStack.class, SlurryStackIngredient.class, CrTConstants.CLASS_SLURRY_STACK_INGREDIENT,
              CrTSlurryStack::new, CrTSlurryTagManager.INSTANCE, ChemicalIngredientDeserializer.SLURRY);
        if (PARAMETER_NAMES.isEmpty()) {
            //Lazy initialize the parameter names, ideally we would find a better time to do this and
            // support multiple instances better but for now this will work
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("crafttweaker_param_names.txt")))) {
                String line;
                Map<String, List<String>> methods = null;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        if (line.startsWith("Class: ")) {
                            methods = new HashMap<>();
                            PARAMETER_NAMES.put(line.substring(7), methods);
                        } else {
                            String[] parts = line.split("=");
                            methods.put(parts[0], Arrays.stream(parts[1].split(",")).toList());
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void addNameLookupOverride(Class<?> clazz, String name) {
        if (nameLookupOverrides.containsKey(clazz)) {
            throw new RuntimeException("A name lookup override for '" + clazz.getSimpleName() + "' has already been registered.");
        }
        nameLookupOverrides.put(clazz, name);
    }

    protected void addPrimitiveInfo(Class<?> primitiveClass, Class<?> objectClass, String name) {
        addNameLookupOverride(objectClass, name);
        addSupportedConversion(primitiveClass, objectClass, (imports, primitive) -> primitive.toString());
    }

    @SafeVarargs
    protected final <ACTUAL> void addSupportedConversion(Class<?> crtClass, Class<? extends ACTUAL> actualClass,
          BiFunction<CrTImportsComponent, ? super ACTUAL, String>... conversions) {
        addSupportedConversion(crtClass, null, actualClass, conversions);
    }

    @SafeVarargs
    protected final <ACTUAL> void addSupportedConversionWithAlt(Class<?> crtClass, Class<?> altCrTClass, Class<? extends ACTUAL> actualClass,
          BiFunction<CrTImportsComponent, ? super ACTUAL, String>... conversions) {
        addSupportedConversion(crtClass, actualClass, conversions);
        addSupportedConversion(altCrTClass, actualClass, conversions);
    }

    @SafeVarargs
    protected final <ACTUAL> void addSupportedConversion(Class<?> crtClass, @Nullable Class<?> generic, Class<? extends ACTUAL> actualClass,
          BiFunction<CrTImportsComponent, ? super ACTUAL, String>... conversions) {
        supportedConversions.computeIfAbsent(crtClass, clazz -> new ConversionTracker()).add(generic, new ClassConversionInfo<>(actualClass, Arrays.asList(conversions)));
    }

    @Nullable
    private List<ClassConversionInfo<?>> getConversions(Class<?> crtClass, @Nullable Class<?> crtGenerics) {
        ConversionTracker conversionTracker = supportedConversions.get(crtClass);
        return conversionTracker != null ? conversionTracker.getConversions(crtGenerics) : null;
    }

    public boolean supportsConversion(Class<?> crtClass, @Nullable Class<?> crtGenerics, Class<?> actualClass) {
        List<ClassConversionInfo<?>> conversions = getConversions(crtClass, crtGenerics);
        return conversions != null && conversions.stream().anyMatch(conversionInfo -> conversionInfo.actualClass.isAssignableFrom(actualClass));
    }

    public <ACTUAL> List<String> getConversionRepresentations(Class<?> crtClass, @Nullable Class<?> crtGenerics, CrTImportsComponent imports, ACTUAL actual) {
        Class<?> actualClass = actual.getClass();
        List<ClassConversionInfo<?>> conversions = getConversions(crtClass, crtGenerics);
        if (conversions != null) {
            List<String> representations = new ArrayList<>();
            for (ClassConversionInfo<?> conversionInfo : conversions) {
                if (conversionInfo.actualClass.isAssignableFrom(actualClass)) {
                    for (BiFunction<CrTImportsComponent, ?, String> stringFunction : conversionInfo.conversions) {
                        String representation = ((BiFunction<CrTImportsComponent, ? super ACTUAL, String>) stringFunction).apply(imports, actual);
                        if (representation != null) {
                            //We use null to represent things we can't represent and then don't add them here
                            representations.add(representation);
                        }
                    }
                }
            }
            if (!representations.isEmpty()) {
                //If we have any representations try returning them
                return representations;
            }
            //Otherwise, try seeing if we have a default type we can fall back to
        }
        if (crtClass.isAssignableFrom(actualClass)) {
            if (actual instanceof String) {
                return Collections.singletonList("\"" + actual + "\"");
            } else if (actual instanceof Character) {
                return Collections.singletonList("'" + actual + "'");
            } else if (actual instanceof Number || actual instanceof Boolean) {
                return Collections.singletonList(actual.toString());
            }
        }
        return Collections.emptyList();
    }

    public String getCrTClassName(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class to lookup CrT class name of cannot be null.");
        return nameLookupOverrides.getOrDefault(clazz, clazz.getSimpleName());
    }

    public boolean recipeExists(ResourceLocation location) {
        return existingFileHelper.exists(location, PackType.SERVER_DATA, ".json", "recipes");
    }

    protected abstract void addExamples();

    /**
     * Creates and adds a CraftTweaker example script builder with the file located by data/modid/scripts/fileName.json
     *
     * @param fileName Name of the file, must be a valid resource location path.
     *
     * @return Builder
     */
    protected CrTExampleBuilder<?> exampleBuilder(String fileName) {
        Objects.requireNonNull(fileName, "Example Builder ID cannot be null.");
        if (!isValidNamespace(fileName)) {
            throw new IllegalArgumentException("'" + fileName + "' is not a valid path, must be [a-z0-9/._-]");
        }
        if (examples.containsKey(fileName)) {
            throw new RuntimeException("Example '" + fileName + "' has already been registered.");
        }
        CrTExampleBuilder<?> exampleBuilder = new CrTExampleBuilder<>(this, fileName);
        examples.put(fileName, exampleBuilder);
        existingFileHelper.trackGenerated(new ResourceLocation(modid, fileName), PackType.SERVER_DATA, ".zs", "scripts");
        return exampleBuilder;
    }

    @Override
    public void run(@Nonnull HashCache cache) {
        examples.clear();
        addExamples();
        for (Map.Entry<String, CrTExampleBuilder<?>> entry : examples.entrySet()) {
            String examplePath = entry.getKey();
            Path path = gen.getOutputFolder().resolve("data/" + modid + "/scripts/" + examplePath + ".zs");
            try {
                save(cache, entry.getValue().build(), path);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't save example script: " + examplePath, e);
            }
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return "CraftTweaker Examples: " + modid;
    }

    private void addItemStackIngredientSupport() {
        addSupportedConversion(ItemStackIngredient.class, ItemStackIngredient.class, this::getIngredientRepresentation,
              (imports, ingredient) -> {
                  if (ingredient instanceof ItemStackIngredient.Single single) {
                      JsonObject serialized = ingredient.serialize().getAsJsonObject();
                      return MekanismRecipeHandler.basicImplicitIngredient(single.getInputRaw(), GsonHelper.getAsInt(serialized, JsonConstants.AMOUNT, 1),
                            serialized.get(JsonConstants.INGREDIENT));
                  }
                  return null;
              });
    }

    private String getIngredientRepresentation(CrTImportsComponent imports, ItemStackIngredient ingredient) {
        if (ingredient instanceof ItemStackIngredient.Single single) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            //While it is easier to compare types of some stuff using the inner ingredient, some things
            // are easier to get the information of out of the serialized ingredient
            JsonObject serializedIngredient = serialized.getAsJsonObject(JsonConstants.INGREDIENT);
            Ingredient vanillaIngredient = single.getInputRaw();
            int amount = GsonHelper.getAsInt(serialized, JsonConstants.AMOUNT, 1);
            String representation = null;
            if (amount > 1) {
                //Special case handling for when we would want to use a different constructor
                if (vanillaIngredient.isVanilla() && !serializedIngredient.isJsonArray() && serializedIngredient.has(JsonConstants.ITEM)) {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(serializedIngredient, JsonConstants.ITEM)));
                    representation = ItemStackUtil.getCommandString(new ItemStack(item, amount));
                    amount = 1;
                } else if (vanillaIngredient instanceof NBTIngredient) {
                    ItemStack stack = CraftingHelper.getItemStack(serializedIngredient, true);
                    stack.setCount(amount);
                    representation = ItemStackUtil.getCommandString(stack);
                    amount = 1;
                }
            }
            if (representation == null) {
                representation = IIngredient.fromIngredient(vanillaIngredient).getCommandString();
            }
            String path = imports.addImport(CrTConstants.CLASS_ITEM_STACK_INGREDIENT);
            if (amount == 1) {
                return path + ".from(" + representation + ")";
            }
            return path + ".from(" + representation + ", " + amount + ")";
        } else if (ingredient instanceof ItemStackIngredient.Multi multiIngredient) {
            StringBuilder builder = new StringBuilder(imports.addImport(CrTConstants.CLASS_ITEM_STACK_INGREDIENT) + ".createMulti(");
            if (!multiIngredient.forEachIngredient(i -> {
                String rep = getIngredientRepresentation(imports, i);
                if (rep == null) {
                    return true;
                }
                builder.append(rep).append(", ");
                return false;
            })) {
                //Remove trailing comma and space
                builder.setLength(builder.length() - 2);
                builder.append(")");
                return builder.toString();
            }
        }
        return null;
    }

    private void addFluidStackIngredientSupport() {
        addSupportedConversion(FluidStackIngredient.class, FluidStackIngredient.class, this::getIngredientRepresentation,
              (imports, ingredient) -> {
                  if (ingredient instanceof FluidStackIngredient.Single) {
                      JsonObject serialized = ingredient.serialize().getAsJsonObject();
                      return new MCFluidStack(SerializerHelper.deserializeFluid(serialized)).getCommandString();
                  } else if (ingredient instanceof FluidStackIngredient.Tagged) {
                      JsonObject serialized = ingredient.serialize().getAsJsonObject();
                      return TagManagerFluid.INSTANCE.getTag(serialized.get(JsonConstants.TAG).getAsString())
                            .withAmount(serialized.getAsJsonPrimitive(JsonConstants.AMOUNT).getAsInt()).getCommandString();
                  }
                  return null;
              });
    }

    private String getIngredientRepresentation(CrTImportsComponent imports, FluidStackIngredient ingredient) {
        if (ingredient instanceof FluidStackIngredient.Single) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            String stackRepresentation = new MCFluidStack(SerializerHelper.deserializeFluid(serialized)).getCommandString();
            return imports.addImport(CrTConstants.CLASS_FLUID_STACK_INGREDIENT) + ".from(" + stackRepresentation + ")";
        } else if (ingredient instanceof FluidStackIngredient.Tagged) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            String tagRepresentation = TagManagerFluid.INSTANCE.getTag(serialized.get(JsonConstants.TAG).getAsString()).getCommandString();
            return imports.addImport(CrTConstants.CLASS_FLUID_STACK_INGREDIENT) + ".from(" + tagRepresentation + ", " + serialized.getAsJsonPrimitive(JsonConstants.AMOUNT) + ")";
        } else if (ingredient instanceof FluidStackIngredient.Multi multiIngredient) {
            StringBuilder builder = new StringBuilder(imports.addImport(CrTConstants.CLASS_FLUID_STACK_INGREDIENT) + ".createMulti(");
            if (!multiIngredient.forEachIngredient(i -> {
                String rep = getIngredientRepresentation(imports, i);
                if (rep == null) {
                    return true;
                }
                builder.append(rep).append(", ");
                return false;
            })) {
                //Remove trailing comma and space
                builder.setLength(builder.length() - 2);
                builder.append(")");
                return builder.toString();
            }
        }
        return null;
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void addSupportedChemical(Class<STACK> stackClass,
          Class<? extends ICrTChemicalStack<CHEMICAL, STACK, ?>> stackCrTClass, Class<? extends ChemicalStackIngredient<CHEMICAL, STACK>> ingredientClass,
          String ingredientType, Function<STACK, CommandStringDisplayable> singleDescription, CrTChemicalTagManager<CHEMICAL> tagManager,
          ChemicalIngredientDeserializer<CHEMICAL, STACK, ?> deserializer) {
        addSupportedConversionWithAlt(ICrTChemicalStack.class, stackCrTClass, stackClass, (imports, stack) -> singleDescription.apply(stack).getCommandString());
        addSupportedConversionWithAlt(ChemicalStackIngredient.class, ingredientClass, ingredientClass,
              (imports, ingredient) -> getIngredientRepresentation(ingredient, imports.addImport(ingredientType), deserializer, singleDescription, tagManager),
              (imports, ingredient) -> {
                  if (ingredient instanceof ChemicalStackIngredient.SingleIngredient) {
                      JsonObject serialized = ingredient.serialize().getAsJsonObject();
                      return singleDescription.apply(deserializer.deserializeStack(serialized)).getCommandString();
                  } else if (ingredient instanceof ChemicalStackIngredient.TaggedIngredient) {
                      JsonObject serialized = (JsonObject) ingredient.serialize();
                      long amount = serialized.getAsJsonPrimitive(JsonConstants.AMOUNT).getAsLong();
                      if (amount > 0 && amount <= Integer.MAX_VALUE) {
                          return tagManager.getTag(serialized.get(JsonConstants.TAG).getAsString()).withAmount((int) amount).getCommandString();
                      }
                  }
                  return null;
              });
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> String getIngredientRepresentation(
          ChemicalStackIngredient<CHEMICAL, STACK> ingredient, String ingredientType, ChemicalIngredientDeserializer<CHEMICAL, STACK, ?> deserializer,
          Function<STACK, CommandStringDisplayable> singleDescription, CrTChemicalTagManager<CHEMICAL> tagManager) {
        if (ingredient instanceof ChemicalStackIngredient.SingleIngredient) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            String stackRepresentation = singleDescription.apply(deserializer.deserializeStack(serialized)).getCommandString();
            return ingredientType + ".from(" + stackRepresentation + ")";
        } else if (ingredient instanceof ChemicalStackIngredient.TaggedIngredient) {
            JsonObject serialized = ingredient.serialize().getAsJsonObject();
            String tagRepresentation = tagManager.getTag(serialized.get(JsonConstants.TAG).getAsString()).getCommandString();
            return ingredientType + ".from(" + tagRepresentation + ", " + serialized.getAsJsonPrimitive(JsonConstants.AMOUNT) + ")";
        } else if (ingredient instanceof ChemicalStackIngredient.MultiIngredient<CHEMICAL, STACK, ?> multiIngredient) {
            StringBuilder builder = new StringBuilder(ingredientType + ".createMulti(");
            if (!multiIngredient.forEachIngredient(i -> {
                String rep = getIngredientRepresentation(i, ingredientType, deserializer, singleDescription, tagManager);
                if (rep == null) {
                    return true;
                }
                builder.append(rep).append(", ");
                return false;
            })) {
                //Remove trailing comma and space
                builder.setLength(builder.length() - 2);
                builder.append(")");
                return builder.toString();
            }
        }
        return null;
    }

    /**
     * Basically a copy of {@link DataProvider#save(Gson, HashCache, JsonElement, Path)} but it takes the contents as a string instead of serializes json using GSON.
     */
    private static void save(HashCache cache, String contents, Path path) throws IOException {
        String sha1 = SHA1.hashUnencodedChars(contents).toString();
        if (!Objects.equals(cache.getHash(path), sha1) || !Files.exists(path)) {
            Files.createDirectories(path.getParent());
            try (BufferedWriter bufferedwriter = Files.newBufferedWriter(path)) {
                bufferedwriter.write(contents);
            }
        }
        cache.putNew(path, sha1);
    }

    private static boolean isValidNamespace(String namespaceIn) {
        for (int i = 0; i < namespaceIn.length(); i++) {
            if (!ResourceLocation.isAllowedInResourceLocation(namespaceIn.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    protected static class WeightedItemStack {

        private final ItemStack stack;
        private final double chance;

        public WeightedItemStack(ItemLike item) {
            this(item, 1);
        }

        public WeightedItemStack(ItemLike item, double chance) {
            this(new ItemStack(item), chance);
        }

        public WeightedItemStack(ItemStack stack, double chance) {
            this.stack = stack;
            this.chance = chance;
        }
    }

    private record ClassConversionInfo<ACTUAL>(Class<? extends ACTUAL> actualClass, List<BiFunction<CrTImportsComponent, ? super ACTUAL, String>> conversions) {
    }

    private static class ConversionTracker {

        private final Map<Class<?>, List<ClassConversionInfo<?>>> genericConversions = new HashMap<>();
        private final List<ClassConversionInfo<?>> conversions = new ArrayList<>();

        public void add(@Nullable Class<?> generic, ClassConversionInfo<?> conversionInfo) {
            if (generic == null) {
                conversions.add(conversionInfo);
            } else {
                genericConversions.computeIfAbsent(generic, g -> new ArrayList<>()).add(conversionInfo);
            }
        }

        @Nullable
        public List<ClassConversionInfo<?>> getConversions(@Nullable Class<?> generic) {
            if (generic != null) {
                List<ClassConversionInfo<?>> conversions = genericConversions.get(generic);
                if (conversions != null) {
                    return conversions;
                }
            }
            return conversions;
        }
    }
}