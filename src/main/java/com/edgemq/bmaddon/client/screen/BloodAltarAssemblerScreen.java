package com.edgemq.bmaddon.client.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.menu.BloodAltarAssemblerMenu;
import com.edgemq.bmaddon.menu.slot.BloodAltarPatternSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BloodAltarAssemblerScreen extends AEBaseScreen<BloodAltarAssemblerMenu> {
    public BloodAltarAssemblerScreen(
            BloodAltarAssemblerMenu menu,
            Inventory playerInventory,
            Component title,
            ScreenStyle style
    ) {
        super(menu, playerInventory, title, style);
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (shouldRenderPatternOutputInstead(slot)) {
            renderPatternOutputInstead(guiGraphics, slot);
            return;
        }

        super.renderSlot(guiGraphics, slot);
    }

    private boolean shouldRenderPatternOutputInstead(Slot slot) {
        if (!Screen.hasShiftDown()) {
            return false;
        }

        if (!(slot instanceof BloodAltarPatternSlot)) {
            return false;
        }

        ItemStack patternStack = slot.getItem();

        if (patternStack.isEmpty()) {
            return false;
        }

        if (!BloodAltarPatternItem.isEncoded(patternStack)) {
            return false;
        }

        ItemStack outputPreview = BloodAltarPatternItem.getOutputPreview(patternStack);

        return !outputPreview.isEmpty();
    }

    private void renderPatternOutputInstead(GuiGraphics guiGraphics, Slot slot) {
        ItemStack patternStack = slot.getItem();
        ItemStack outputPreview = BloodAltarPatternItem.getOutputPreview(patternStack);

        if (outputPreview.isEmpty()) {
            return;
        }

        /*
         * renderSlot работает в GUI-relative координатах.
         * Поэтому используем slot.x / slot.y напрямую.
         */
        int x = slot.x;
        int y = slot.y;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);

        guiGraphics.renderItem(outputPreview, x, y);
        guiGraphics.renderItemDecorations(this.font, outputPreview, x, y);

        guiGraphics.pose().popPose();
    }
}