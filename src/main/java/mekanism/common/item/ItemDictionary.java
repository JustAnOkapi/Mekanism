package mekanism.common.item;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class ItemDictionary extends Item {

    public ItemDictionary(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(MekanismLang.DESCRIPTION_DICTIONARY.translate());
        } else {
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile != null || !player.isShiftKeyDown()) {
                //If there is a tile at the position or the player is not sneaking
                // grab the tags of the block and the tile
                if (!world.isClientSide) {
                    Set<ResourceLocation> blockTags = world.getBlockState(pos).getBlock().getTags();
                    Set<ResourceLocation> tileTags = tile == null ? Collections.emptySet() : tile.getType().getTags();
                    if (blockTags.isEmpty() && tileTags.isEmpty()) {
                        player.sendMessage(MekanismUtils.logFormat(MekanismLang.DICTIONARY_NO_KEY), Util.NIL_UUID);
                    } else {
                        //Note: We handle checking they are not empty in sendTagsToPlayer, so that we only display one if one is empty
                        sendTagsToPlayer(player, MekanismLang.DICTIONARY_BLOCK_TAGS_FOUND, blockTags);
                        sendTagsToPlayer(player, MekanismLang.DICTIONARY_TILE_ENTITY_TYPE_TAGS_FOUND, tileTags);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            if (!player.level.isClientSide) {
                sendTagsOrEmptyToPlayer(player, MekanismLang.DICTIONARY_ENTITY_TYPE_TAGS_FOUND, entity.getType().getTags());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide()) {
                MekanismContainerTypes.DICTIONARY.tryOpenGui((ServerPlayer) player, hand, stack);
            }
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        } else {
            BlockHitResult result = MekanismUtils.rayTrace(player, ClipContext.Fluid.ANY);
            if (result.getType() != Type.MISS) {
                Block block = world.getBlockState(result.getBlockPos()).getBlock();
                if (block instanceof LiquidBlock liquidBlock) {
                    if (!world.isClientSide()) {
                        sendTagsOrEmptyToPlayer(player, MekanismLang.DICTIONARY_FLUID_TAGS_FOUND, liquidBlock.getFluid().getTags());
                    }
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
                }
            }
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    private void sendTagsOrEmptyToPlayer(Player player, ILangEntry tagsFoundEntry, Set<ResourceLocation> tags) {
        if (tags.isEmpty()) {
            player.sendMessage(MekanismUtils.logFormat(MekanismLang.DICTIONARY_NO_KEY), Util.NIL_UUID);
        } else {
            sendTagsToPlayer(player, tagsFoundEntry, tags);
        }
    }

    private void sendTagsToPlayer(Player player, ILangEntry tagsFoundEntry, Set<ResourceLocation> tags) {
        if (!tags.isEmpty()) {
            player.sendMessage(MekanismUtils.logFormat(tagsFoundEntry), Util.NIL_UUID);
            for (ResourceLocation tag : tags) {
                player.sendMessage(MekanismLang.DICTIONARY_KEY.translateColored(EnumColor.DARK_GREEN, tag), Util.NIL_UUID);
            }
        }
    }
}