package com.edgemq.bmaddon.item;

import appeng.items.materials.UpgradeCardItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class BloodAltarParallelCardItem extends UpgradeCardItem {
    public BloodAltarParallelCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.bmaddon.blood_altar_parallel_card.description"));
    }
}
