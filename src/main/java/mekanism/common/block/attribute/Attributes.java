package mekanism.common.block.attribute;

import java.util.function.ToIntFunction;
import mekanism.common.block.attribute.Attribute.TileAttribute;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;

public class Attributes {

    public static final Attribute ACTIVE = new AttributeStateActive(0);
    public static final Attribute ACTIVE_LIGHT = new AttributeStateActive(8);
    public static final Attribute ACTIVE_FULL_LIGHT = new AttributeStateActive(15);
    public static final Attribute COMPARATOR = new AttributeComparator();
    public static final Attribute INVENTORY = new AttributeInventory();
    //TODO - 1.18: Re-evaluate this attribute, it seems to be currently unused?
    public static final Attribute MULTIBLOCK = new AttributeMultiblock();
    public static final Attribute REDSTONE = new AttributeRedstone();
    public static final Attribute SECURITY = new AttributeSecurity();

    private Attributes() {
    }

    /** If a block supports security. */
    public static class AttributeSecurity implements Attribute {

        private AttributeSecurity() {
        }
    }

    /** If a block has an inventory. */
    public static class AttributeInventory implements Attribute {

        private AttributeInventory() {
        }
    }

    /** If a block supports comparators. */
    public static class AttributeComparator implements Attribute {

        private AttributeComparator() {
        }
    }

    /** If a block supports integration with computers. */
    public static class AttributeComputerIntegration implements Attribute {

        public final String name;

        public AttributeComputerIntegration(String name) {
            this.name = name;
        }
    }

    /** If a block has a redstone input configuration. */
    public static class AttributeRedstone implements Attribute {

        private AttributeRedstone() {
        }
    }

    /** If mobs can spawn on the block. */
    public static class AttributeMobSpawn implements Attribute {

        public static final StateArgumentPredicate<EntityType<?>> NEVER_PREDICATE = (state, reader, pos, entityType) -> false;
        public static final AttributeMobSpawn NEVER = new AttributeMobSpawn(NEVER_PREDICATE);
        //TODO: Make mob spawn denying on internal multiblocks smarter?
        public static final AttributeMobSpawn WHEN_NOT_FORMED = new AttributeMobSpawn((state, reader, pos, entityType) -> {
            TileEntityMultiblock<?> tile = WorldUtils.getTileEntity(TileEntityMultiblock.class, reader, pos);
            if (tile != null) {
                if (reader instanceof LevelReader levelReader && levelReader.isClientSide()) {
                    //If we are on the client just check if we are formed as we don't sync structure information
                    // to the client. This way the client is at least relatively accurate with what values
                    // it returns for if mobs can spawn
                    if (tile.getMultiblock().isFormed()) {
                        return false;
                    }
                } else if (tile.getMultiblock().isPositionInsideBounds(tile.getStructure(), pos.above())) {
                    //If the multiblock is formed and the position above this block is inside the bounds of the multiblock
                    // don't allow spawning on it.
                    return false;
                }
            }
            //Super implementation
            return state.isFaceSturdy(reader, pos, Direction.UP) && state.getLightEmission(reader, pos) < 14;
        });

        private final StateArgumentPredicate<EntityType<?>> spawningPredicate;

        public AttributeMobSpawn(StateArgumentPredicate<EntityType<?>> spawningPredicate) {
            this.spawningPredicate = spawningPredicate;
        }

        @Override
        public void adjustProperties(Properties props) {
            props.isValidSpawn(spawningPredicate);
        }
    }

    /** If this block is a part of a multiblock. */
    public static class AttributeMultiblock implements Attribute {

        private AttributeMultiblock() {
        }
    }

    /** If a block can emit redstone. */
    public static class AttributeRedstoneEmitter<TILE extends TileEntityMekanism> implements TileAttribute<TILE> {

        private final ToIntFunction<TILE> redstoneFunction;

        public AttributeRedstoneEmitter(ToIntFunction<TILE> redstoneFunction) {
            this.redstoneFunction = redstoneFunction;
        }

        public int getRedstoneLevel(TILE tile) {
            return redstoneFunction.applyAsInt(tile);
        }
    }

    /** Custom explosion resistance attribute. */
    public static class AttributeCustomResistance implements Attribute {

        private final float resistance;

        public AttributeCustomResistance(float resistance) {
            this.resistance = resistance;
            //TODO: Adjust properties instead of having the override?
        }

        public float getResistance() {
            return resistance;
        }
    }

    /** Light value attribute. */
    public static class AttributeLight implements Attribute {

        private final int light;

        public AttributeLight(int light) {
            this.light = light;
        }

        @Override
        public void adjustProperties(BlockBehaviour.Properties props) {
            BlockStateHelper.applyLightLevelAdjustments(props, state -> light);
        }
    }
}