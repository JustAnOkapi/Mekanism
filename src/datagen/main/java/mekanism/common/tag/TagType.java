package mekanism.common.tag;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public final class TagType<TYPE extends IForgeRegistryEntry<TYPE>> {

    public static final TagType<Item> ITEM = new TagType<>("Item", () -> ForgeRegistries.ITEMS);
    public static final TagType<Block> BLOCK = new TagType<>("Block", () -> ForgeRegistries.BLOCKS);
    public static final TagType<EntityType<?>> ENTITY_TYPE = new TagType<>("Entity Type", () -> ForgeRegistries.ENTITIES);
    public static final TagType<Fluid> FLUID = new TagType<>("Fluid", () -> ForgeRegistries.FLUIDS);
    public static final TagType<Enchantment> ENCHANTMENT = new TagType<>("Enchantment", () -> ForgeRegistries.ENCHANTMENTS);
    public static final TagType<Potion> POTION = new TagType<>("Potion", () -> ForgeRegistries.POTIONS);
    public static final TagType<MobEffect> MOB_EFFECTS = new TagType<>("Mob Effect", () -> ForgeRegistries.MOB_EFFECTS);
    public static final TagType<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new TagType<>("Block Entity Type", () -> ForgeRegistries.BLOCK_ENTITIES);
    public static final TagType<Gas> GAS = new TagType<>("Gas", MekanismAPI::gasRegistry);
    public static final TagType<InfuseType> INFUSE_TYPE = new TagType<>("Infuse Type", MekanismAPI::infuseTypeRegistry);
    public static final TagType<Pigment> PIGMENT = new TagType<>("Pigment", MekanismAPI::pigmentRegistry);
    public static final TagType<Slurry> SLURRY = new TagType<>("Slurry", MekanismAPI::slurryRegistry);

    private final NonNullSupplier<IForgeRegistry<TYPE>> registry;
    private final String name;

    private TagType(String name, NonNullSupplier<IForgeRegistry<TYPE>> registry) {
        this.name = name;
        this.registry = registry;
    }

    public String getName() {
        return name;
    }

    public IForgeRegistry<TYPE> getRegistry() {
        return registry.get();
    }
}