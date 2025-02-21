package mekanism.common.network.to_client.container.property;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class BlockPosPropertyData extends PropertyData {

    @Nullable
    private final BlockPos value;

    public BlockPosPropertyData(short property, @Nullable BlockPos value) {
        super(PropertyType.BLOCK_POS, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        if (value == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeBlockPos(value);
        }
    }
}