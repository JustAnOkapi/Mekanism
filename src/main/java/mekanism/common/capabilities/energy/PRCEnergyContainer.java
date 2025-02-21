package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PRCEnergyContainer extends MachineEnergyContainer<TileEntityPressurizedReactionChamber> {

    public static PRCEnergyContainer input(TileEntityPressurizedReactionChamber tile) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new PRCEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile);
    }

    private PRCEnergyContainer(FloatingLong maxEnergy, FloatingLong energyPerTick, Predicate<@NonNull AutomationType> canExtract,
          Predicate<@NonNull AutomationType> canInsert, TileEntityPressurizedReactionChamber tile) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile);
    }

    @Override
    public FloatingLong getBaseEnergyPerTick() {
        return super.getBaseEnergyPerTick().add(tile.getRecipeEnergyRequired());
    }
}