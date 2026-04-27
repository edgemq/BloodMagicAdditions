package com.edgemq.bmaddon.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEItemKey;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.registry.BMAddonItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class BloodAltarPatternDecoder implements IPatternDetailsDecoder {
    public static final BloodAltarPatternDecoder INSTANCE = new BloodAltarPatternDecoder();

    private BloodAltarPatternDecoder() {
    }

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        return !stack.isEmpty()
                && stack.is(BMAddonItems.BLOOD_ALTAR_PATTERN.get())
                && BloodAltarPatternItem.isEncoded(stack);
    }

    @Nullable
    @Override
    public IPatternDetails decodePattern(AEItemKey what, Level level) {
        if (what == null || what.getItem() != BMAddonItems.BLOOD_ALTAR_PATTERN.get()) {
            return null;
        }

        ItemStack stack = what.toStack();

        return decodePattern(stack, level, false);
    }

    @Nullable
    @Override
    public IPatternDetails decodePattern(ItemStack stack, Level level, boolean tryRecovery) {
        if (!isEncodedPattern(stack)) {
            return null;
        }

        return BloodAltarPatternDetails.create(level, stack).orElse(null);
    }
}