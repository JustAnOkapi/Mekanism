package mekanism.api.chemical.slurry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.ISlurryProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IRegistryDelegate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SlurryStack extends ChemicalStack<Slurry> {

    /**
     * Empty SlurryStack instance.
     */
    public static final SlurryStack EMPTY = new SlurryStack(MekanismAPI.EMPTY_SLURRY, 0);

    /**
     * Creates a new SlurryStack with a defined slurry type and quantity.
     *
     * @param slurryProvider - provides the slurry type of the stack
     * @param amount         - amount of the slurry to be referenced in this SlurryStack
     */
    public SlurryStack(ISlurryProvider slurryProvider, long amount) {
        super(slurryProvider.getChemical(), amount);
    }

    public SlurryStack(SlurryStack stack, long amount) {
        this(stack.getType(), amount);
    }

    @Override
    protected IRegistryDelegate<Slurry> getDelegate(Slurry slurry) {
        if (MekanismAPI.slurryRegistry().getKey(slurry) == null) {
            MekanismAPI.logger.fatal("Failed attempt to create a SlurryStack for an unregistered Slurry {} (type {})", slurry.getRegistryName(),
                  slurry.getClass().getName());
            throw new IllegalArgumentException("Cannot create a SlurryStack from an unregistered Slurry");
        }
        return slurry.delegate;
    }

    @Override
    protected Slurry getEmptyChemical() {
        return MekanismAPI.EMPTY_SLURRY;
    }

    /**
     * Returns the SlurryStack stored in the defined tag compound, or null if it doesn't exist.
     *
     * @param nbtTags - tag compound to read from
     *
     * @return SlurryStack stored in the tag compound
     */
    public static SlurryStack readFromNBT(@Nullable CompoundTag nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return EMPTY;
        }
        Slurry type = Slurry.readFromNBT(nbtTags);
        if (type.isEmptyType()) {
            return EMPTY;
        }
        long amount = nbtTags.getLong(NBTConstants.AMOUNT);
        if (amount <= 0) {
            return EMPTY;
        }
        return new SlurryStack(type, amount);
    }

    public static SlurryStack readFromPacket(FriendlyByteBuf buf) {
        Slurry slurry = buf.readRegistryId();
        long amount = buf.readVarLong();
        if (slurry.isEmptyType()) {
            return EMPTY;
        }
        return new SlurryStack(slurry, amount);
    }

    /**
     * Returns a copied form of this SlurryStack.
     *
     * @return copied SlurryStack
     */
    @Override
    public SlurryStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new SlurryStack(this, getAmount());
    }
}