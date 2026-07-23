package com.edgemq.bmaddon.client.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.menu.BloodAltarAssemblerMenu;
import com.edgemq.bmaddon.menu.slot.BloodAltarPatternSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

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
    public void extractSlot(GuiGraphicsExtractor guiGraphics, Slot slot, int mouseX, int mouseY) {
        if (shouldExtractPatternOutputInstead(slot)) {
            extractPatternOutputInstead(guiGraphics, slot);
            return;
        }

        super.extractSlot(guiGraphics, slot, mouseX, mouseY);
    }

    private boolean shouldExtractPatternOutputInstead(Slot slot) {
        if (!hasShiftDown()) {
            return false;
        }

        if (!(slot instanceof BloodAltarPatternSlot)) {
            return false;
        }

        ItemStack patternStack = slot.getItem();

        if (patternStack.isEmpty() || !BloodAltarPatternItem.isEncoded(patternStack)) {
            return false;
        }

        return !BloodAltarPatternItem.getOutputPreview(patternStack).isEmpty();
    }

    private static boolean hasShiftDown() {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private void extractPatternOutputInstead(GuiGraphicsExtractor guiGraphics, Slot slot) {
        ItemStack outputPreview = BloodAltarPatternItem.getOutputPreview(slot.getItem());

        if (outputPreview.isEmpty()) {
            return;
        }

        guiGraphics.item(outputPreview, slot.x, slot.y);
        guiGraphics.itemDecorations(this.font, outputPreview, slot.x, slot.y);
    }
}
