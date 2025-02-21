package mekanism.common.command.builders;

import java.util.function.Consumer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class StructureBuilder {

    protected final int sizeX, sizeY, sizeZ;

    protected StructureBuilder(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    protected abstract void build(Level world, BlockPos start);

    protected void buildFrame(Level world, BlockPos start) {
        buildPartialFrame(world, start, -1);
    }

    protected void buildPartialFrame(Level world, BlockPos start, int cutoff) {
        for (int x = 0; x < sizeX; x++) {
            if (x > cutoff && x < sizeX - 1 - cutoff) {
                world.setBlockAndUpdate(start.offset(x, 0, 0), getCasing().defaultBlockState());
                world.setBlockAndUpdate(start.offset(x, sizeY - 1, 0), getCasing().defaultBlockState());
                world.setBlockAndUpdate(start.offset(x, 0, sizeZ - 1), getCasing().defaultBlockState());
                world.setBlockAndUpdate(start.offset(x, sizeY - 1, sizeZ - 1), getCasing().defaultBlockState());
            }
        }
        for (int y = 0; y < sizeY; y++) {
            if (y > cutoff && y < sizeY - 1 - cutoff) {
                world.setBlockAndUpdate(start.offset(0, y, 0), getCasing().defaultBlockState());
                world.setBlockAndUpdate(start.offset(sizeX - 1, y, 0), getCasing().defaultBlockState());
                world.setBlockAndUpdate(start.offset(0, y, sizeZ - 1), getCasing().defaultBlockState());
                world.setBlockAndUpdate(start.offset(sizeX - 1, y, sizeZ - 1), getCasing().defaultBlockState());
            }
        }
        for (int z = 0; z < sizeZ; z++) {
            if (z > cutoff && z < sizeZ - 1 - cutoff) {
                world.setBlockAndUpdate(start.offset(0, 0, z), getCasing().defaultBlockState());
                world.setBlockAndUpdate(start.offset(sizeX - 1, 0, z), getCasing().defaultBlockState());
                world.setBlockAndUpdate(start.offset(0, sizeY - 1, z), getCasing().defaultBlockState());
                world.setBlockAndUpdate(start.offset(sizeX - 1, sizeY - 1, z), getCasing().defaultBlockState());
            }
        }
    }

    protected void buildWalls(Level world, BlockPos start) {
        for (int x = 1; x < sizeX - 1; x++) {
            for (int z = 1; z < sizeZ - 1; z++) {
                BlockPos pos = new BlockPos(x, 0, z);
                world.setBlockAndUpdate(start.offset(pos), getFloorBlock(pos).defaultBlockState());
                pos = new BlockPos(x, sizeY - 1, z);
                world.setBlockAndUpdate(start.offset(pos), getRoofBlock(pos).defaultBlockState());
            }
        }
        for (int y = 1; y < sizeY - 1; y++) {
            for (int x = 1; x < sizeZ - 1; x++) {
                BlockPos pos = new BlockPos(x, y, 0);
                world.setBlockAndUpdate(start.offset(pos), getWallBlock(pos).defaultBlockState());
                pos = new BlockPos(x, y, sizeZ - 1);
                world.setBlockAndUpdate(start.offset(pos), getWallBlock(pos).defaultBlockState());
            }
            for (int z = 1; z < sizeZ - 1; z++) {
                BlockPos pos = new BlockPos(0, y, z);
                world.setBlockAndUpdate(start.offset(pos), getWallBlock(pos).defaultBlockState());
                pos = new BlockPos(sizeZ - 1, y, z);
                world.setBlockAndUpdate(start.offset(pos), getWallBlock(pos).defaultBlockState());
            }
        }
    }

    protected void buildInteriorLayers(Level world, BlockPos start, int yMin, int yMax, Block block) {
        for (int y = yMin; y <= yMax; y++) {
            buildInteriorLayer(world, start, y, block);
        }
    }

    protected void buildInteriorLayer(Level world, BlockPos start, int yLevel, Block block) {
        for (int x = 1; x < sizeX - 1; x++) {
            for (int z = 1; z < sizeZ - 1; z++) {
                world.setBlockAndUpdate(start.offset(x, yLevel, z), block.defaultBlockState());
            }
        }
    }

    protected void buildPlane(Level world, BlockPos start, int x1, int z1, int x2, int z2, int yLevel, Block block) {
        for (int x = x1; x < x2 - 1; x++) {
            for (int z = z1; z < z2 - 1; z++) {
                world.setBlockAndUpdate(start.offset(x, yLevel, z), block.defaultBlockState());
            }
        }
    }

    protected void buildColumn(Level world, BlockPos start, BlockPos pos, int height, Block block) {
        for (int y = 0; y < height; y++) {
            world.setBlockAndUpdate(start.offset(pos).offset(0, y, 0), block.defaultBlockState());
        }
    }

    protected <T extends BlockEntity> void buildColumn(Level world, BlockPos start, BlockPos pos, int height, Block block, Class<T> tileClass, Consumer<T> tileConsumer) {
        for (int y = 0; y < height; y++) {
            BlockPos position = start.offset(pos).offset(0, y, 0);
            world.setBlockAndUpdate(position, block.defaultBlockState());
            T tile = WorldUtils.getTileEntity(tileClass, world, position);
            if (tile != null) {
                tileConsumer.accept(tile);
            }
        }
    }

    protected Block getWallBlock(BlockPos pos) {
        return MekanismBlocks.STRUCTURAL_GLASS.getBlock();
    }

    protected Block getFloorBlock(BlockPos pos) {
        return getCasing();
    }

    protected Block getRoofBlock(BlockPos pos) {
        return getWallBlock(pos);
    }

    protected abstract Block getCasing();
}
