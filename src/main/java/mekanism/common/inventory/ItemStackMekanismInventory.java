package mekanism.common.inventory;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;

/**
 * Helper class for implementing handling of inventories for items
 */
public abstract class ItemStackMekanismInventory implements IMekanismInventory {

    private final List<IInventorySlot> slots;
    @Nonnull
    protected final ItemStack stack;

    protected ItemStackMekanismInventory(@Nonnull ItemStack stack) {
        this.stack = stack;
        this.slots = getInitialInventory();
        if (!stack.isEmpty() && stack.getItem() instanceof IItemSustainedInventory sustainedInventory) {
            DataHandlerUtils.readContainers(getInventorySlots(null), sustainedInventory.getInventory(stack));
        }
    }

    protected abstract List<IInventorySlot> getInitialInventory();

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return slots;
    }

    @Override
    public void onContentsChanged() {
        if (!stack.isEmpty() && stack.getItem() instanceof IItemSustainedInventory sustainedInventory) {
            sustainedInventory.setInventory(DataHandlerUtils.writeContainers(getInventorySlots(null)), stack);
        }
    }
}