package com.edgemq.bmaddon.client.screen;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import com.edgemq.bmaddon.menu.BloodAltarAssemblerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BloodAltarAssemblerScreen extends AEBaseScreen<BloodAltarAssemblerMenu> {
    public BloodAltarAssemblerScreen(
            BloodAltarAssemblerMenu menu,
            Inventory playerInventory,
            Component title,
            ScreenStyle style
    ) {
        super(menu, playerInventory, title, style);
    }
}
