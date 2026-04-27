package com.edgemq.bmaddon.mixin;

import appeng.menu.slot.RestrictedInputSlot;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.registry.BMAddonItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "appeng.menu.slot.RestrictedInputSlot")
public abstract class RestrictedInputSlotMixin {
    @Shadow(remap = false)
    @Final
    private RestrictedInputSlot.PlacableItemType which;

    @Inject(
            method = "mayPlace(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("RETURN"),
            cancellable = true,
            remap = true
    )
    private void bmaddon$allowBloodAltarPatternInAe2PatternSlots(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (callback.getReturnValue()) {
            return;
        }

        if (which == RestrictedInputSlot.PlacableItemType.BLANK_PATTERN && isBlankBloodAltarPattern(stack)) {
            callback.setReturnValue(true);
            return;
        }

        if (which == RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN && isBloodAltarPattern(stack)) {
            callback.setReturnValue(true);
        }
    }

    private static boolean isBloodAltarPattern(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BMAddonItems.BLOOD_ALTAR_PATTERN.get());
    }

    private static boolean isBlankBloodAltarPattern(ItemStack stack) {
        return isBloodAltarPattern(stack) && !BloodAltarPatternItem.isEncoded(stack);
    }
}