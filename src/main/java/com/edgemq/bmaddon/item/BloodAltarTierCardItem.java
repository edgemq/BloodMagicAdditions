package com.edgemq.bmaddon.item;

import appeng.items.materials.UpgradeCardItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class BloodAltarTierCardItem extends UpgradeCardItem {
    private final int tier;

    public BloodAltarTierCardItem(int tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.translatable("tooltip.bmaddon.blood_altar_tier_card.tier", tier));
        tooltip.add(Component.translatable("tooltip.bmaddon.blood_altar_tier_card.description"));
    }
}