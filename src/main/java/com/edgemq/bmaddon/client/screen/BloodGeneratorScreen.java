package com.edgemq.bmaddon.client.screen;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.menu.BloodGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class BloodGeneratorScreen extends AbstractContainerScreen<BloodGeneratorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            BMAddon.MODID,
            "textures/gui/blood_generator.png"
    );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int GUI_WIDTH = 212;

    /*
     * Важно:
     * У твоей текстуры служебная полоска крови лежит внизу, начиная примерно с y = 186.
     * Поэтому фон GUI рисуем только до 186, иначе эта полоска появляется снизу экрана.
     */
    private static final int GUI_HEIGHT = 186;

    private static final int GUI_OFFSET_X = -10;

    /*
     * Строка крови на GUI:
     * x: 45 -> 175
     * y: 77 -> 83
     */
    private static final int BLOOD_BAR_AREA_X = 46;
    private static final int BLOOD_BAR_AREA_Y = 79;
    private static final int BLOOD_BAR_AREA_WIDTH = 130;
    private static final int BLOOD_BAR_AREA_HEIGHT = 6;

    /*
     * Иконка крови стоит вплотную слева от заполнения.
     * Значит общий индикатор начинается на 5 пикселей левее строки крови.
     */
    private static final int BLOOD_INDICATOR_X = 41;
    private static final int BLOOD_INDICATOR_Y = 78;

    /*
     * Источник индикатора в текстуре:
     * иконка крови: x 0..4
     * заполнение крови: x 5..133
     */
    private static final int BLOOD_INDICATOR_TEXTURE_U = 0;
    private static final int BLOOD_INDICATOR_TEXTURE_V = 186;

    private static final int BLOOD_ICON_WIDTH = 5;
    private static final int BLOOD_FILL_WIDTH = 129;
    private static final int BLOOD_INDICATOR_HEIGHT = 5;

    public BloodGeneratorScreen(BloodGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

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
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(
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

        renderBloodIndicator(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderBloodTooltip(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderBloodIndicator(GuiGraphics guiGraphics) {
        float ratio = this.menu.getBloodRatio();

        if (ratio <= 0.0F) {
            return;
        }

        int filledWidth = Math.round(BLOOD_FILL_WIDTH * ratio);
        int totalWidth = BLOOD_ICON_WIDTH + filledWidth;

        if (totalWidth <= BLOOD_ICON_WIDTH) {
            totalWidth = BLOOD_ICON_WIDTH;
        }

        guiGraphics.blit(
                TEXTURE,
                this.leftPos + BLOOD_INDICATOR_X,
                this.topPos + BLOOD_INDICATOR_Y,
                BLOOD_INDICATOR_TEXTURE_U,
                BLOOD_INDICATOR_TEXTURE_V,
                totalWidth,
                BLOOD_INDICATOR_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private void renderBloodTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isMouseOverBloodBar(mouseX, mouseY)) {
            return;
        }

        guiGraphics.renderComponentTooltip(
                this.font,
                List.of(Component.translatable(
                        "tooltip.bmaddon.blood_generator.blood",
                        this.menu.getBloodAmount(),
                        this.menu.getBloodCapacity()
                )),
                mouseX,
                mouseY
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
