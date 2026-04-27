package com.edgemq.bmaddon.registry;

import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import com.edgemq.bmaddon.BMAddon;

public final class BMAddonSlotSemantics {
    public static final SlotSemantic BLOOD_ALTAR_PATTERN = SlotSemantics.register(
            BMAddon.MODID + ":BLOOD_ALTAR_PATTERN",
            false
    );

    public static void init() {
    }

    private BMAddonSlotSemantics() {
    }
}