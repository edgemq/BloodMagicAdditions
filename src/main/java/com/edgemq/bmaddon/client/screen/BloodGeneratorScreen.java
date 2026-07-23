package com.edgemq.bmaddon.client.screen;

import com.edgemq.bmaddon.menu.BloodGeneratorMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BloodGeneratorScreen extends AbstractContainerScreen<BloodGeneratorMenu> {
    private static final int GUI_OFFSET_X = -10;

    public BloodGeneratorScreen(BloodGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

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
}
