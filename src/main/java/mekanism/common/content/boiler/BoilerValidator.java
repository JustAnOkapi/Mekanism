package mekanism.common.content.boiler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Optional;
import java.util.Set;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.multiblock.TileEntitySuperheatingElement;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;

public class BoilerValidator extends CuboidStructureValidator<BoilerMultiblockData> {

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MekanismBlockTypes.BOILER_CASING)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, MekanismBlockTypes.BOILER_VALVE)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
        if (super.validateInner(state, chunkMap, pos)) {
            return true;
        }
        return BlockType.is(state.getBlock(), MekanismBlockTypes.PRESSURE_DISPERSER, MekanismBlockTypes.SUPERHEATING_ELEMENT);
    }

    @Override
    public FormationResult postcheck(BoilerMultiblockData structure, Set<BlockPos> innerNodes, Long2ObjectMap<ChunkAccess> chunkMap) {
        Set<BlockPos> dispersers = new ObjectOpenHashSet<>();
        Set<BlockPos> elements = new ObjectOpenHashSet<>();
        for (BlockPos pos : innerNodes) {
            BlockEntity tile = WorldUtils.getTileEntity(world, chunkMap, pos);
            if (tile instanceof TileEntityPressureDisperser) {
                dispersers.add(pos);
            } else if (tile instanceof TileEntitySuperheatingElement) {
                structure.internalLocations.add(pos);
                elements.add(pos);
            }
        }
        //Ensure at least one disperser exists
        if (dispersers.isEmpty()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_NO_DISPERSER);
        }
        //Ensure that at least one superheating element exists
        if (elements.isEmpty()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_SUPERHEATING);
        }

        //Find a single disperser contained within this multiblock
        final BlockPos initDisperser = dispersers.iterator().next();

        //Ensure that a full horizontal plane of dispersers exists, surrounding the found disperser
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = 1; x < structure.length() - 1; x++) {
            for (int z = 1; z < structure.width() - 1; z++) {
                mutablePos.set(structure.renderLocation.getX() + x, initDisperser.getY(), structure.renderLocation.getZ() + z);
                TileEntityPressureDisperser tile = WorldUtils.getTileEntity(TileEntityPressureDisperser.class, world, chunkMap, mutablePos);
                if (tile == null) {
                    return FormationResult.fail(MekanismLang.BOILER_INVALID_MISSING_DISPERSER, mutablePos);
                }
                dispersers.remove(mutablePos);
            }
        }

        //If there are more dispersers than those on the plane found, the structure is invalid
        if (!dispersers.isEmpty()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_EXTRA_DISPERSER);
        }

        structure.superheatingElements = FormationProtocol.explore(elements.iterator().next(), coord ->
              coord.getY() < initDisperser.getY() && WorldUtils.getTileEntity(TileEntitySuperheatingElement.class, world, chunkMap, coord) != null);

        if (elements.size() > structure.superheatingElements) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_SUPERHEATING);
        }

        BlockPos initAir = null;
        BlockPos.MutableBlockPos mutableAir = new BlockPos.MutableBlockPos();
        int totalAir = 0;

        //Find the first available block in the structure for water storage (including casings)
        for (int x = structure.renderLocation.getX(); x < structure.renderLocation.getX() + structure.length(); x++) {
            for (int y = structure.renderLocation.getY(); y < initDisperser.getY(); y++) {
                for (int z = structure.renderLocation.getZ(); z < structure.renderLocation.getZ() + structure.width(); z++) {
                    mutableAir.set(x, y, z);
                    if (isAirOrFrame(chunkMap, mutableAir)) {
                        initAir = mutableAir.immutable();
                        totalAir++;
                    }
                }
            }
        }

        //Gradle build requires these fields to be final
        final BlockPos renderLocation = structure.renderLocation;
        final int volLength = structure.length();
        final int volWidth = structure.width();
        structure.setWaterVolume(FormationProtocol.explore(initAir, coord ->
              coord.getY() >= renderLocation.getY() - 1 && coord.getY() < initDisperser.getY() &&
              coord.getX() >= renderLocation.getX() && coord.getX() < renderLocation.getX() + volLength &&
              coord.getZ() >= renderLocation.getZ() && coord.getZ() < renderLocation.getZ() + volWidth &&
              isAirOrFrame(chunkMap, coord)));

        //Make sure all air blocks are connected
        if (totalAir > structure.getWaterVolume()) {
            return FormationResult.fail(MekanismLang.BOILER_INVALID_AIR_POCKETS);
        }

        int steamHeight = (structure.renderLocation.getY() + structure.height() - 2) - initDisperser.getY();
        structure.setSteamVolume(structure.width() * structure.length() * steamHeight);
        structure.upperRenderLocation = new BlockPos(structure.renderLocation.getX(), initDisperser.getY() + 1, structure.renderLocation.getZ());
        return FormationResult.SUCCESS;
    }

    private boolean isAirOrFrame(Long2ObjectMap<ChunkAccess> chunkMap, BlockPos airPos) {
        Optional<BlockState> stateOptional = WorldUtils.getBlockState(world, chunkMap, airPos);
        return (stateOptional.isPresent() && stateOptional.get().isAir()) ||
               isFrameCompatible(WorldUtils.getTileEntity(world, chunkMap, airPos));
    }
}