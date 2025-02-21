package mekanism.common.command.builders;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

public class BuildCommand {

    private static final SimpleCommandExceptionType MISS = new SimpleCommandExceptionType(MekanismLang.COMMAND_ERROR_BUILD_MISS.translate());

    private BuildCommand() {
    }

    public static final ArgumentBuilder<CommandSourceStack, ?> COMMAND =
          Commands.literal("build")
                .requires(cs -> cs.hasPermission(2) && cs.getEntity() instanceof ServerPlayer)
                .then(Commands.literal("remove")
                      .executes(ctx -> {
                          CommandSourceStack source = ctx.getSource();
                          BlockHitResult result = MekanismUtils.rayTrace(source.getPlayerOrException(), 100);
                          if (result.getType() == HitResult.Type.MISS) {
                              throw MISS.create();
                          }
                          destroy(source.getLevel(), result.getBlockPos());
                          source.sendSuccess(MekanismLang.COMMAND_BUILD_REMOVED.translateColored(EnumColor.GRAY), true);
                          return 0;
                      }));

    public static void register(String name, ILangEntry localizedName, StructureBuilder builder) {
        COMMAND.then(Commands.literal(name)
              .executes(ctx -> {
                  CommandSourceStack source = ctx.getSource();
                  BlockHitResult result = MekanismUtils.rayTrace(source.getPlayerOrException(), 100);
                  if (result.getType() == HitResult.Type.MISS) {
                      throw MISS.create();
                  }
                  BlockPos pos = result.getBlockPos().relative(Direction.UP);
                  builder.build(source.getLevel(), pos);
                  source.sendSuccess(MekanismLang.COMMAND_BUILD_BUILT.translateColored(EnumColor.GRAY, EnumColor.INDIGO, localizedName), true);
                  return 0;
              }));
    }

    private static void destroy(Level world, BlockPos pos) throws CommandSyntaxException {
        Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap<>();
        if (!isMekanismBlock(world, chunkMap, pos)) {
            //If we didn't hit a mekanism block throw an error that we missed
            throw MISS.create();
        }
        Set<BlockPos> traversed = new HashSet<>();
        Queue<BlockPos> openSet = new ArrayDeque<>();
        openSet.add(pos);
        traversed.add(pos);
        while (!openSet.isEmpty()) {
            BlockPos ptr = openSet.poll();
            if (isMekanismBlock(world, chunkMap, ptr)) {
                Clearable.tryClear(WorldUtils.getTileEntity(world, chunkMap, ptr));
                world.removeBlock(ptr, false);
                for (Direction side : EnumUtils.DIRECTIONS) {
                    BlockPos offset = ptr.relative(side);
                    if (traversed.add(offset)) {
                        openSet.add(offset);
                    }
                }
            }
        }
    }

    private static boolean isMekanismBlock(@Nullable LevelAccessor world, @Nonnull Long2ObjectMap<ChunkAccess> chunkMap, @Nonnull BlockPos pos) {
        Optional<BlockState> state = WorldUtils.getBlockState(world, chunkMap, pos);
        return state.isPresent() && state.get().getBlock().getRegistryName().getNamespace().startsWith(Mekanism.MODID);
    }
}
