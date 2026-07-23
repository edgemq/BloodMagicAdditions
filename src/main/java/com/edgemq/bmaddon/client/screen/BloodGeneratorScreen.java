package com.edgemq.bmaddon.client.screen;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.config.BMAddonCommonConfig;
import com.edgemq.bmaddon.menu.BloodGeneratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class BloodGeneratorScreen extends AbstractContainerScreen<BloodGeneratorMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            BMAddon.MODID,
            "textures/gui/blood_generator.png"
    );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int GUI_WIDTH = 212;
    private static final int GUI_HEIGHT = 186;
    private static final int GUI_OFFSET_X = -10;

    private static final int BLOOD_BAR_AREA_X = 46;
    private static final int BLOOD_BAR_AREA_Y = 79;
    private static final int BLOOD_BAR_AREA_WIDTH = 130;
    private static final int BLOOD_BAR_AREA_HEIGHT = 6;

    private static final int BLOOD_INDICATOR_X = 41;
    private static final int BLOOD_INDICATOR_Y = 78;
    private static final int BLOOD_INDICATOR_TEXTURE_U = 0;
    private static final int BLOOD_INDICATOR_TEXTURE_V = 186;
    private static final int BLOOD_ICON_WIDTH = 5;
    private static final int BLOOD_FILL_WIDTH = 129;
    private static final int BLOOD_INDICATOR_HEIGHT = 5;
    private static final int GENERATION_TEXT_X = 46;
    private static final int GENERATION_TEXT_Y = 91;
    private static final int GENERATION_TEXT_COLOR = 0x2F2F2F;

    public BloodGeneratorScreen(BloodGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GUI_WIDTH, GUI_HEIGHT);

        this.titleLabelX = 0;
        this.titleLabelY = 0;
        this.inventoryLabelX = 0;
        this.inventoryLabelY = 0;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos += GUI_OFFSET_X;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        extractBloodIndicator(guiGraphics);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(
                this.font,
                Component.translatable(
                        "screen.bmaddon.blood_generator.generation_value",
                        BMAddonCommonConfig.BLOOD_GENERATOR_LIFE_ESSENCE_PER_OPERATION.get(),
                        BMAddonCommonConfig.BLOOD_GENERATOR_ENERGY_PER_OPERATION.get(),
                        BMAddonCommonConfig.BLOOD_GENERATOR_WORK_INTERVAL_TICKS.get()
                ),
                GENERATION_TEXT_X,
                GENERATION_TEXT_Y,
                GENERATION_TEXT_COLOR,
                false
        );
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (isMouseOverBloodBar(mouseX, mouseY)) {
            guiGraphics.setComponentTooltipForNextFrame(
                    this.font,
                    List.of(Component.translatable(
                            "tooltip.bmaddon.blood_generator.blood",
                            this.menu.getBloodAmount(),
                            this.menu.getBloodCapacity()
                    )),
                    mouseX,
                    mouseY
            );
            return;
        }

        super.extractTooltip(guiGraphics, mouseX, mouseY);
    }

    private void extractBloodIndicator(GuiGraphicsExtractor guiGraphics) {
        float ratio = this.menu.getBloodRatio();

        if (ratio <= 0.0F) {
            return;
        }

        int filledWidth = Math.round(BLOOD_FILL_WIDTH * ratio);
        int totalWidth = Math.max(BLOOD_ICON_WIDTH, BLOOD_ICON_WIDTH + filledWidth);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos + BLOOD_INDICATOR_X,
                this.topPos + BLOOD_INDICATOR_Y,
                BLOOD_INDICATOR_TEXTURE_U,
                BLOOD_INDICATOR_TEXTURE_V,
                totalWidth,
                BLOOD_INDICATOR_HEIGHT,
                totalWidth,
                BLOOD_INDICATOR_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private boolean isMouseOverBloodBar(int mouseX, int mouseY) {
        int x = this.leftPos + BLOOD_BAR_AREA_X;
        int y = this.topPos + BLOOD_BAR_AREA_Y;

        return mouseX >= x
                && mouseX < x + BLOOD_BAR_AREA_WIDTH
                && mouseY >= y
                && mouseY < y + BLOOD_BAR_AREA_HEIGHT;
    }
}
