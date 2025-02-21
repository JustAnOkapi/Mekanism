package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.AutomationType;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator;
import mekanism.common.lib.transmitter.CompatibleTransmitterValidator.CompatibleFluidTransmitterValidator;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.MechanicalPipeUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class MechanicalPipe extends BufferedTransmitter<IFluidHandler, FluidNetwork, FluidStack, MechanicalPipe> implements IMekanismFluidHandler,
      IUpgradeableTransmitter<MechanicalPipeUpgradeData> {

    public final PipeTier tier;
    @Nonnull
    public FluidStack saveShare = FluidStack.EMPTY;
    private final List<IExtendedFluidTank> tanks;
    public final BasicFluidTank buffer;

    public MechanicalPipe(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.FLUID);
        this.tier = Attribute.getTier(blockProvider, PipeTier.class);
        //TODO: If we make fluids support longs then adjust this
        buffer = BasicFluidTank.create(MathUtils.clampToInt(getCapacity()), BasicFluidTank.alwaysFalse, BasicFluidTank.alwaysTrue, this);
        tanks = Collections.singletonList(buffer);
    }

    @Override
    public AcceptorCache<IFluidHandler> getAcceptorCache() {
        //Cast it here to make things a bit easier, as we know createAcceptorCache by default returns an object of type AcceptorCache
        return (AcceptorCache<IFluidHandler>) super.getAcceptorCache();
    }

    @Override
    public PipeTier getTier() {
        return tier;
    }

    @Override
    public void pullFromAcceptors() {
        Set<Direction> connections = getConnections(ConnectionType.PULL);
        if (!connections.isEmpty()) {
            for (IFluidHandler connectedAcceptor : getAcceptorCache().getConnectedAcceptors(connections)) {
                FluidStack received;
                //Note: We recheck the buffer each time in case we ended up accepting fluid somewhere
                // and our buffer changed and is no longer empty
                FluidStack bufferWithFallback = getBufferWithFallback();
                if (bufferWithFallback.isEmpty()) {
                    //If we don't have a fluid stored try pulling as much as we are able to
                    received = connectedAcceptor.drain(getAvailablePull(), FluidAction.SIMULATE);
                } else {
                    //Otherwise, try draining the same type of fluid we have stored requesting up to as much as we are able to pull
                    // We do this to better support multiple tanks in case the fluid we have stored we could pull out of a block's
                    // second tank but just asking to drain a specific amount
                    received = connectedAcceptor.drain(new FluidStack(bufferWithFallback, getAvailablePull()), FluidAction.SIMULATE);
                }
                if (!received.isEmpty() && takeFluid(received, Action.SIMULATE).isEmpty()) {
                    //If we received some fluid and are able to insert it all, then actually extract it and insert it into our thing.
                    // Note: We extract first after simulating ourselves because if the target gave a faulty simulation value, we want to handle it properly
                    // and not accidentally dupe anything, and we know our simulation we just performed on taking it is valid
                    takeFluid(connectedAcceptor.drain(received, FluidAction.EXECUTE), Action.EXECUTE);
                }
            }
        }
    }

    private int getAvailablePull() {
        if (hasTransmitterNetwork()) {
            return Math.min(tier.getPipePullAmount(), getTransmitterNetwork().fluidTank.getNeeded());
        }
        return Math.min(tier.getPipePullAmount(), buffer.getNeeded());
    }

    @Nullable
    @Override
    public MechanicalPipeUpgradeData getUpgradeData() {
        return new MechanicalPipeUpgradeData(redstoneReactive, getConnectionTypesRaw(), getShare());
    }

    @Override
    public boolean dataTypeMatches(@Nonnull TransmitterUpgradeData data) {
        return data instanceof MechanicalPipeUpgradeData;
    }

    @Override
    public void parseUpgradeData(@Nonnull MechanicalPipeUpgradeData data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        takeFluid(data.contents, Action.EXECUTE);
    }

    @Override
    public void read(@Nonnull CompoundTag nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains(NBTConstants.FLUID_STORED, Tag.TAG_COMPOUND)) {
            saveShare = FluidStack.loadFluidStackFromNBT(nbtTags.getCompound(NBTConstants.FLUID_STORED));
        } else {
            saveShare = FluidStack.EMPTY;
        }
        buffer.setStack(saveShare);
    }

    @Nonnull
    @Override
    public CompoundTag write(@Nonnull CompoundTag nbtTags) {
        super.write(nbtTags);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(this);
        }
        if (saveShare.isEmpty()) {
            nbtTags.remove(NBTConstants.FLUID_STORED);
        } else {
            nbtTags.put(NBTConstants.FLUID_STORED, saveShare.writeToNBT(new CompoundTag()));
        }
        return nbtTags;
    }

    @Override
    public boolean isValidAcceptor(BlockEntity tile, Direction side) {
        return super.isValidAcceptor(tile, side) && getAcceptorCache().isAcceptorAndListen(tile, side, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
    }

    @Override
    public CompatibleTransmitterValidator<IFluidHandler, FluidNetwork, MechanicalPipe> getNewOrphanValidator() {
        return new CompatibleFluidTransmitterValidator(this);
    }

    @Override
    public boolean isValidTransmitter(TileEntityTransmitter transmitter, Direction side) {
        if (super.isValidTransmitter(transmitter, side) && transmitter.getTransmitter() instanceof MechanicalPipe other) {
            FluidStack buffer = getBufferWithFallback();
            if (buffer.isEmpty() && hasTransmitterNetwork() && getTransmitterNetwork().getPrevTransferAmount() > 0) {
                buffer = getTransmitterNetwork().lastFluid;
            }
            FluidStack otherBuffer = other.getBufferWithFallback();
            if (otherBuffer.isEmpty() && other.hasTransmitterNetwork() && other.getTransmitterNetwork().getPrevTransferAmount() > 0) {
                otherBuffer = other.getTransmitterNetwork().lastFluid;
            }
            return buffer.isEmpty() || otherBuffer.isEmpty() || buffer.isFluidEqual(otherBuffer);
        }
        return false;
    }

    @Override
    public FluidNetwork createEmptyNetworkWithID(UUID networkID) {
        return new FluidNetwork(networkID);
    }

    @Override
    public FluidNetwork createNetworkByMerging(Collection<FluidNetwork> networks) {
        return new FluidNetwork(networks);
    }

    @Override
    protected boolean canHaveIncompatibleNetworks() {
        return true;
    }

    @Override
    public long getCapacity() {
        return tier.getPipeCapacity();
    }

    @Nonnull
    @Override
    public FluidStack releaseShare() {
        FluidStack ret = buffer.getFluid();
        buffer.setEmpty();
        return ret;
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isEmpty();
    }

    @Nonnull
    @Override
    public FluidStack getBufferWithFallback() {
        FluidStack buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Nonnull
    @Override
    public FluidStack getShare() {
        return buffer.getFluid();
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            FluidNetwork network = getTransmitterNetwork();
            if (!network.fluidTank.isEmpty() && !saveShare.isEmpty()) {
                int amount = saveShare.getAmount();
                MekanismUtils.logMismatchedStackSize(network.fluidTank.shrinkStack(amount, Action.EXECUTE), amount);
                buffer.setStack(saveShare);
            }
        }
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getFluidTanks(side);
        }
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        getTransmitterTile().markDirty(false);
    }

    /**
     * @return remainder
     */
    @Nonnull
    public FluidStack takeFluid(@Nonnull FluidStack fluid, Action action) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().fluidTank.insert(fluid, action, AutomationType.INTERNAL);
        }
        return buffer.insert(fluid, action, AutomationType.INTERNAL);
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull FluidNetwork network, @Nonnull CompoundTag tag) {
        super.handleContentsUpdateTag(network, tag);
        NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, network::setLastFluid);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> network.currentScale = scale);
    }
}