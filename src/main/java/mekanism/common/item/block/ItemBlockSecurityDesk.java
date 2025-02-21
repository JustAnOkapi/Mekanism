package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.basic.BlockSecurityDesk;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class ItemBlockSecurityDesk extends ItemBlockTooltip<BlockSecurityDesk> implements IItemSustainedInventory {

    public ItemBlockSecurityDesk(BlockSecurityDesk block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties());
    }

    @Override
    public void addDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, boolean advanced) {
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Override
    public boolean placeBlock(@Nonnull BlockPlaceContext context, @Nonnull BlockState state) {
        if (!WorldUtils.isValidReplaceableBlock(context.getLevel(), context.getClickedPos().above())) {
            //If there is not enough room, fail
            return false;
        }
        return super.placeBlock(context, state);
    }
}