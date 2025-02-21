package mekanism.common.inventory.container.entity.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ISecurityContainer;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.CraftingMenu;

public class CraftingRobitContainer extends CraftingMenu implements IEntityContainer<EntityRobit>, ISecurityContainer {

    private final EntityRobit entity;

    public CraftingRobitContainer(int id, Inventory inv, EntityRobit robit) {
        super(id, inv, robit.getWorldPosCallable());
        this.entity = robit;
        entity.open(inv.player);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return entity.isAlive();
    }

    @Nonnull
    @Override
    public EntityRobit getEntity() {
        return entity;
    }

    @Nonnull
    @Override
    public MenuType<?> getType() {
        return MekanismContainerTypes.CRAFTING_ROBIT.get();
    }

    @Override
    public void removed(@Nonnull Player player) {
        super.removed(player);
        entity.close(player);
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return entity;
    }
}