package mekanism.client.lang;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.lang.FormatSplitter.Component;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;

public abstract class BaseLanguageProvider extends LanguageProvider {

    private final ConvertibleLanguageProvider[] altProviders;
    private final String modid;

    public BaseLanguageProvider(DataGenerator gen, String modid) {
        super(gen, modid, "en_us");
        this.modid = modid;
        altProviders = new ConvertibleLanguageProvider[]{
              new UpsideDownLanguageProvider(gen, modid)
        };
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    protected void add(IHasTranslationKey key, String value) {
        if (key instanceof IBlockProvider blockProvider) {
            Block block = blockProvider.getBlock();
            if (Attribute.has(block, AttributeGui.class) && !Attribute.get(block, AttributeGui.class).hasCustomName()) {
                add(Util.makeDescriptionId("container", block.getRegistryName()), value);
            }
        }
        add(key.getTranslationKey(), value);
    }

    protected void add(IBlockProvider blockProvider, String value, String containerName) {
        Block block = blockProvider.getBlock();
        if (Attribute.has(block, AttributeGui.class) && !Attribute.get(block, AttributeGui.class).hasCustomName()) {
            add(Util.makeDescriptionId("container", block.getRegistryName()), containerName);
            add(blockProvider.getTranslationKey(), value);
        } else {
            throw new IllegalArgumentException("Block " + blockProvider.getRegistryName() + " does not have a container name set.");
        }
    }

    protected void add(IModuleDataProvider<?> moduleDataProvider, String name, String description) {
        ModuleData<?> moduleData = moduleDataProvider.getModuleData();
        add(moduleData.getTranslationKey(), name);
        add(moduleData.getDescriptionTranslationKey(), description);
    }

    protected void addFluid(FluidRegistryObject<?, ?, ?, ?> fluidRO, String name) {
        add(fluidRO.getStillFluid().getAttributes().getTranslationKey(), name);
        add(fluidRO.getFlowingFluid().getAttributes().getTranslationKey(), "Flowing " + name);
        add(fluidRO.getBlock(), name);
        add(fluidRO.getBucket(), name + " Bucket");
    }

    @Override
    public void add(@Nonnull String key, @Nonnull String value) {
        super.add(key, value);
        if (altProviders.length > 0) {
            List<Component> splitEnglish = FormatSplitter.split(value);
            for (ConvertibleLanguageProvider provider : altProviders) {
                provider.convert(key, splitEnglish);
            }
        }
    }

    @Override
    public void run(@Nonnull HashCache cache) throws IOException {
        super.run(cache);
        if (altProviders.length > 0) {
            for (ConvertibleLanguageProvider provider : altProviders) {
                provider.run(cache);
            }
        }
    }
}