package mekanism.common.block.property;

import java.util.Arrays;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraft.block.properties.PropertyEnum;

public class PropertyConnection extends PropertyEnum<ConnectionType> {

    protected PropertyConnection(String name) {
        super(name, ConnectionType.class, Arrays.asList(ConnectionType.values()));
    }

    /**
     * Create a new PropertyConnection with the given name
     */
    public static PropertyConnection create(String name) {
        return new PropertyConnection(name);
    }
}