package mekanism.client.gui.element;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;

public class GuiGraph extends GuiTexturedElement {

    private static final int TEXTURE_WIDTH = 3;
    private static final int TEXTURE_HEIGHT = 2;

    private final LongList graphData = new LongArrayList();
    private final GraphDataHandler dataHandler;

    private long currentScale = 10;
    private boolean fixedScale = false;

    public GuiGraph(IGuiWrapper gui, int x, int y, int width, int height, GraphDataHandler handler) {
        super(MekanismUtils.getResource(ResourceType.GUI, "graph.png"), gui, x, y, width, height);
        dataHandler = handler;
    }

    public void enableFixedScale(long scale) {
        fixedScale = true;
        currentScale = scale;
    }

    public void setMinScale(long minScale) {
        currentScale = minScale;
    }

    public void addData(long data) {
        if (graphData.size() == width - 2) {
            graphData.removeLong(0);
        }

        graphData.add(data);
        if (!fixedScale) {
            for (long i : graphData) {
                if (i > currentScale) {
                    currentScale = i;
                }
            }
        }
    }

    @Override
    public void drawBackground(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        //Draw Black and border
        renderBackgroundTexture(matrix, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
        RenderSystem.setShaderTexture(0, getResource());
        //Draw the graph
        int size = graphData.size();
        int x = this.x + 1;
        int y = this.y + 1;
        int height = this.height - 2;
        for (int i = 0; i < size; i++) {
            long data = Math.min(currentScale, graphData.getLong(i));
            int relativeHeight = (int) (data * height / (double) currentScale);
            blit(matrix, x + i, y + height - relativeHeight, 0, 0, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            //TODO - 1.18: Test this
            //RenderSystem.shadeModel(GL11.GL_SMOOTH);
            //RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

            RenderSystem.setShaderColor(1, 1, 1, 0.2F + 0.8F * i / size);
            blit(matrix, x + i, y + height - relativeHeight, 1, 0, 1, relativeHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            int hoverIndex = mouseX - getButtonX();
            if (hoverIndex == i && mouseY >= getButtonY() && mouseY < getButtonY() + height) {
                RenderSystem.setShaderColor(1, 1, 1, 0.5F);
                blit(matrix, x + i, y, 2, 0, 1, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                MekanismRenderer.resetColor();
                blit(matrix, x + i, y + height - relativeHeight, 0, 1, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }

            MekanismRenderer.resetColor();
            RenderSystem.disableBlend();
            //RenderSystem.enableAlphaTest();
        }
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        int hoverIndex = mouseX - relativeX;
        if (hoverIndex >= 0 && hoverIndex < graphData.size()) {
            displayTooltip(matrix, dataHandler.getDataDisplay(graphData.getLong(hoverIndex)), mouseX, mouseY);
        }
    }

    public interface GraphDataHandler {

        Component getDataDisplay(long data);
    }
}