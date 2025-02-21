package mekanism.client.gui.element.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class GuiVerticalPowerBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation ENERGY_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "vertical_power.png");
    private static final int texWidth = 4;
    private static final int texHeight = 52;

    private final double heightScale;

    public GuiVerticalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y) {
        this(gui, container, x, y, texHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y, int desiredHeight) {
        this(gui, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return EnergyDisplay.of(container).getTextComponent();
            }

            @Override
            public double getLevel() {
                return container.getEnergy().divideToLevel(container.getMaxEnergy());
            }
        }, x, y, desiredHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        this(gui, handler, x, y, texHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int desiredHeight) {
        super(ENERGY_BAR, gui, handler, x, y, texWidth, desiredHeight, false);
        heightScale = desiredHeight / (double) texHeight;
    }

    @Override
    protected void renderBarOverlay(PoseStack matrix, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int displayInt = (int) (handlerLevel * texHeight);
        if (displayInt > 0) {
            int scaled = calculateScaled(heightScale, displayInt);
            blit(matrix, x + 1, y + height - 1 - scaled, texWidth, scaled, 0, 0, texWidth, displayInt, texWidth, texHeight);
        }
    }
}