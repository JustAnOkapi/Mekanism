package mekanism.common.loot.table;

import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import net.minecraft.data.loot.FishingLoot;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseFishingLootTables extends FishingLoot {

    @Override
    public abstract void accept(@Nonnull BiConsumer<ResourceLocation, Builder> consumer);
}