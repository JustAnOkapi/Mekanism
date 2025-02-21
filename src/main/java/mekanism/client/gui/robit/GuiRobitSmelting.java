package mekanism.client.gui.robit;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiRobitSmelting extends GuiRobit<RobitContainer> {

    public GuiRobitSmelting(RobitContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 1;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiProgress(robit::getScaledProgress, ProgressType.BAR, this, 78, 38).jeiCategories(MekanismBlocks.ENERGIZED_SMELTER.getRegistryName()));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        drawString(matrix, title, titleLabelX, titleLabelY, titleTextColor());
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.SMELTING;
    }
}