package mekanism.common.util;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.IOwnerItem;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityMode;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Whether a given PlayerEntity is considered an Op.
     *
     * @param p - player to check
     *
     * @return if the player has operator privileges
     */
    public static boolean isOp(Player p) {
        return p instanceof ServerPlayer player && MekanismConfig.general.opsBypassRestrictions.get() && player.server.getPlayerList().isOp(player.getGameProfile());
    }

    public static boolean canAccess(Player player, Object object) {
        ISecurityObject security;
        if (object instanceof ItemStack stack) {
            if (!(stack.getItem() instanceof ISecurityItem) && stack.getItem() instanceof IOwnerItem ownerItem) {
                //If it is an owner item but not a security item make sure the owner matches
                if (!MekanismConfig.general.allowProtection.get() || isOp(player)) {
                    //If protection is disabled or the player is an op and bypass restrictions are enabled, access is always granted
                    return true;
                }
                UUID owner = ownerItem.getOwnerUUID(stack);
                return owner == null || owner.equals(player.getUUID());
            }
            security = wrapSecurityItem(stack);
        } else if (object instanceof ISecurityObject securityObject) {
            security = securityObject;
        } else {
            //The object doesn't have security so there are no security restrictions
            return true;
        }
        return !security.hasSecurity() || canAccess(security.getSecurityMode(), player, security.getOwnerUUID());
    }

    private static boolean canAccess(SecurityMode mode, Player player, UUID owner) {
        if (!MekanismConfig.general.allowProtection.get() || isOp(player)) {
            //If protection is disabled or the player is an op and bypass restrictions are enabled, access is always granted
            return true;
        }
        if (owner == null || player.getUUID().equals(owner)) {
            return true;
        }
        SecurityFrequency freq = getFrequency(owner);
        if (freq == null) {
            return true;
        }
        if (freq.isOverridden()) {
            mode = freq.getSecurityMode();
        }
        if (mode == SecurityMode.PUBLIC) {
            return true;
        } else if (mode == SecurityMode.TRUSTED) {
            return freq.getTrustedUUIDs().contains(player.getUUID());
        }
        return false;
    }

    @Nullable
    public static SecurityFrequency getFrequency(@Nullable UUID uuid) {
        return uuid == null ? null : FrequencyType.SECURITY.getManager(null).getFrequency(uuid);
    }

    public static void displayNoAccess(Player player) {
        player.sendMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_ACCESS), Util.NIL_UUID);
    }

    public static SecurityMode getSecurity(ISecurityObject security, Dist side) {
        if (!security.hasSecurity()) {
            return SecurityMode.PUBLIC;
        }
        if (side.isDedicatedServer()) {
            SecurityFrequency freq;
            if (security instanceof ISecurityTile securityTile) {
                freq = securityTile.getSecurity().getFrequency();
            } else {
                freq = getFrequency(security.getOwnerUUID());
            }
            if (freq != null && freq.isOverridden()) {
                return freq.getSecurityMode();
            }
        } else if (side.isClient()) {
            SecurityData data = MekanismClient.clientSecurityMap.get(security.getOwnerUUID());
            if (data != null && data.override) {
                return data.mode;
            }
        }
        return security.getSecurityMode();
    }

    public static boolean isOverridden(ISecurityObject security, Dist side) {
        if (!security.hasSecurity() || security.getOwnerUUID() == null) {
            return false;
        }
        if (side.isDedicatedServer()) {
            SecurityFrequency freq = getFrequency(security.getOwnerUUID());
            return freq != null && freq.isOverridden();
        }
        SecurityData data = MekanismClient.clientSecurityMap.get(security.getOwnerUUID());
        return data != null && data.override;
    }

    public static void claimItem(Player player, ItemStack stack) {
        if (stack.getItem() instanceof IOwnerItem ownerItem) {
            ownerItem.setOwnerUUID(stack, player.getUUID());
            Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(player.getUUID(), null));
            player.sendMessage(MekanismUtils.logFormat(MekanismLang.NOW_OWN), Util.NIL_UUID);
        }
    }

    /**
     * Only call this on the client
     */
    public static void addSecurityTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip) {
        if (stack.getItem() instanceof IOwnerItem ownerItem) {
            tooltip.add(OwnerDisplay.of(MekanismUtils.tryGetClientPlayer(), ownerItem.getOwnerUUID(stack)).getTextComponent());
        }
        ISecurityObject securityObject = SecurityUtils.wrapSecurityItem(stack);
        tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, SecurityUtils.getSecurity(securityObject, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(securityObject, Dist.CLIENT)) {
            tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
        }
    }

    public static ISecurityObject wrapSecurityItem(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ISecurityItem)) {
            return ISecurityObject.NO_SECURITY;
        }
        return new ISecurityObject() {

            @Nullable
            @Override
            public UUID getOwnerUUID() {
                return ((ISecurityItem) stack.getItem()).getOwnerUUID(stack);
            }

            @Nullable
            @Override
            public String getOwnerName() {
                UUID ownerUUID = getOwnerUUID();
                return ownerUUID == null ? null : MekanismClient.clientUUIDMap.get(ownerUUID);
            }

            @Override
            public SecurityMode getSecurityMode() {
                return ((ISecurityItem) stack.getItem()).getSecurity(stack);
            }

            @Override
            public void setSecurityMode(SecurityMode mode) {
                ((ISecurityItem) stack.getItem()).setSecurity(stack, mode);
            }
        };
    }

    /**
     * @apiNote Mainly for use on the client side when the stack potentially could change while our security object currently exists
     */
    public static ISecurityObject wrapSecurityItem(@Nonnull Supplier<ItemStack> stackSupplier) {
        ItemStack stack = stackSupplier.get();
        if (stack.isEmpty() || !(stack.getItem() instanceof ISecurityItem)) {
            return ISecurityObject.NO_SECURITY;
        }
        return new ISecurityObject() {

            private ItemStack getAndValidateStack() {
                ItemStack stack = stackSupplier.get();
                if (stack.isEmpty() || !(stack.getItem() instanceof ISecurityItem)) {
                    return ItemStack.EMPTY;
                }
                return stack;
            }

            @Override
            public boolean hasSecurity() {
                return !getAndValidateStack().isEmpty();
            }

            @Nullable
            @Override
            public UUID getOwnerUUID() {
                ItemStack stack = getAndValidateStack();
                if (stack.isEmpty()) {
                    return null;
                }
                return ((ISecurityItem) stack.getItem()).getOwnerUUID(stack);
            }

            @Nullable
            @Override
            public String getOwnerName() {
                UUID ownerUUID = getOwnerUUID();
                return ownerUUID == null ? null : MekanismClient.clientUUIDMap.get(ownerUUID);
            }

            @Override
            public SecurityMode getSecurityMode() {
                ItemStack stack = getAndValidateStack();
                if (stack.isEmpty()) {
                    return SecurityMode.PUBLIC;
                }
                return ((ISecurityItem) stack.getItem()).getSecurity(stack);
            }

            @Override
            public void setSecurityMode(SecurityMode mode) {
                ItemStack stack = getAndValidateStack();
                if (!stack.isEmpty()) {
                    ((ISecurityItem) stack.getItem()).setSecurity(stack, mode);
                }
            }
        };
    }
}