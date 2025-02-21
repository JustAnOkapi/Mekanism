package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class ItemBlockQIOComponent extends ItemBlockMachine {

    public ItemBlockQIOComponent(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void addStats(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, boolean advanced) {
        MekanismUtils.addFrequencyToTileTooltip(stack, FrequencyType.QIO, tooltip);
    }
}