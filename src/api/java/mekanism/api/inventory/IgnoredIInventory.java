package mekanism.api.inventory;

import javax.annotation.Nonnull;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * NO-OP IInventory
 */
@MethodsReturnNonnullByDefault
public final class IgnoredIInventory implements Container {

    public static final IgnoredIInventory INSTANCE = new IgnoredIInventory();

    private IgnoredIInventory() {
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, @Nonnull ItemStack stack) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return false;
    }

    @Override
    public void clearContent() {
    }
}