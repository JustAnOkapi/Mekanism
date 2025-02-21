package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.common.lib.security.IOwnerItem;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityMode;
import net.minecraft.world.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SecurityInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = stack -> stack.getItem() instanceof IOwnerItem;

    public static SecurityInventorySlot unlock(Supplier<UUID> ownerSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(ownerSupplier, "Owner supplier cannot be null");
        return new SecurityInventorySlot(stack -> ((IOwnerItem) stack.getItem()).getOwnerUUID(stack) == null, stack -> {
            UUID ownerUUID = ((IOwnerItem) stack.getItem()).getOwnerUUID(stack);
            return ownerUUID != null && ownerUUID.equals(ownerSupplier.get());
        }, listener, x, y);
    }

    public static SecurityInventorySlot lock(@Nullable IContentsListener listener, int x, int y) {
        return new SecurityInventorySlot(stack -> ((IOwnerItem) stack.getItem()).getOwnerUUID(stack) != null,
              stack -> ((IOwnerItem) stack.getItem()).getOwnerUUID(stack) == null, listener, x, y);
    }

    private SecurityInventorySlot(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
    }

    public void unlock(UUID ownerUUID) {
        if (!isEmpty() && current.getItem() instanceof IOwnerItem item) {
            UUID stackOwner = item.getOwnerUUID(current);
            if (stackOwner != null && stackOwner.equals(ownerUUID)) {
                item.setOwnerUUID(current, null);
                if (item instanceof ISecurityItem securityItem) {
                    securityItem.setSecurity(current, SecurityMode.PUBLIC);
                }
            }
        }
    }

    public void lock(UUID ownerUUID, SecurityFrequency frequency) {
        if (!isEmpty() && current.getItem() instanceof IOwnerItem item) {
            UUID stackOwner = item.getOwnerUUID(current);
            if (stackOwner == null) {
                item.setOwnerUUID(current, stackOwner = ownerUUID);
            }
            if (stackOwner.equals(ownerUUID) && item instanceof ISecurityItem securityItem) {
                securityItem.setSecurity(current, frequency.getSecurityMode());
            }
        }
    }
}