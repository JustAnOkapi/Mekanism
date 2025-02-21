package mekanism.additions.common;

import mekanism.additions.client.AdditionsBlockStateProvider;
import mekanism.additions.client.AdditionsItemModelProvider;
import mekanism.additions.client.AdditionsLangProvider;
import mekanism.additions.client.AdditionsSoundProvider;
import mekanism.additions.common.loot.AdditionsLootProvider;
import mekanism.additions.common.recipe.AdditionsRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = MekanismAdditions.MODID, bus = Bus.MOD)
public class AdditionsDataGenerator {

    private AdditionsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new AdditionsLangProvider(gen));
            gen.addProvider(new AdditionsSoundProvider(gen, existingFileHelper));
            //Let the blockstate provider see models generated by the item model provider
            AdditionsItemModelProvider itemModelProvider = new AdditionsItemModelProvider(gen, existingFileHelper);
            gen.addProvider(itemModelProvider);
            gen.addProvider(new AdditionsBlockStateProvider(gen, itemModelProvider.existingFileHelper));
        }
        if (event.includeServer()) {
            //Server side data generators
            gen.addProvider(new AdditionsTagProvider(gen, existingFileHelper));
            gen.addProvider(new AdditionsLootProvider(gen));
            gen.addProvider(new AdditionsRecipeProvider(gen, existingFileHelper));
        }
    }
}