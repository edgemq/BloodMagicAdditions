package com.edgemq.bmaddon.mixin;

import appeng.core.definitions.AEItems;
import appeng.api.inventories.InternalInventory;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.registry.BMAddonItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PatternEncodingLogic.class, remap = false)
public abstract class PatternEncodingLogicMixin {
    @Shadow
    @Final
    private AppEngInternalInventory blankPatternInv;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void bmaddon$allowBloodAltarPatternInBlankPatternInventory(CallbackInfo callback) {
        blankPatternInv.setFilter(new IAEItemFilter() {
            @Override
            public boolean allowInsert(InternalInventory inventory, int slot, ItemStack stack) {
                return AEItems.BLANK_PATTERN.is(stack) || isBlankBloodAltarPattern(stack);
            }
        });
    }

    private static boolean isBlankBloodAltarPattern(ItemStack stack) {
        return !stack.isEmpty()
                && stack.is(BMAddonItems.BLOOD_ALTAR_PATTERN.get())
                && !BloodAltarPatternItem.isEncoded(stack);
    }
}
