package mekanism.client.gui.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

public class GuiSeismicVibrator extends GuiMekanismTile<TileEntitySeismicVibrator, MekanismTileContainer<TileEntitySeismicVibrator>> {

    public GuiSeismicVibrator(MekanismTileContainer<TileEntitySeismicVibrator> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 16, 23, 112, 40, () -> Arrays.asList(
              tile.getActive() ? MekanismLang.VIBRATING.translate() : MekanismLang.IDLE.translate(),
              MekanismLang.CHUNK.translate(tile.getBlockPos().getX() >> 4, tile.getBlockPos().getZ() >> 4)
        )));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer()));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}