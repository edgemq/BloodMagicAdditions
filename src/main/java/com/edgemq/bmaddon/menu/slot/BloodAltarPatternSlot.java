package com.edgemq.bmaddon.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.AppEngSlot;
import com.edgemq.bmaddon.ae2.BloodAltarPatternMatcher;
import net.minecraft.world.item.ItemStack;

public class BloodAltarPatternSlot extends AppEngSlot {
    public BloodAltarPatternSlot(InternalInventory inventory, int slot) {
        super(inventory, slot);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return BloodAltarPatternMatcher.isSupportedPatternStack(stack)
                && super.mayPlace(stack);
    }
}