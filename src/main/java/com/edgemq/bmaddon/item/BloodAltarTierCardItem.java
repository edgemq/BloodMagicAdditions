package com.edgemq.bmaddon.item;

import appeng.items.materials.UpgradeCardItem;

public class BloodAltarTierCardItem extends UpgradeCardItem {
    private final int tier;

    public BloodAltarTierCardItem(int tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

}
