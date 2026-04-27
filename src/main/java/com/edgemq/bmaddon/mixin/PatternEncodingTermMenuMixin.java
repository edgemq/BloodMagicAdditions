package com.edgemq.bmaddon.mixin;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.core.definitions.AEItems;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import com.edgemq.bmaddon.ae2.BloodAltarPatternEncodingHelper;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.registry.BMAddonItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = PatternEncodingTermMenu.class, remap = false)
public abstract class PatternEncodingTermMenuMixin {
    @Shadow
    @Final
    private RestrictedInputSlot blankPatternSlot;

    @Shadow
    @Final
    private RestrictedInputSlot encodedPatternSlot;

    @Shadow
    @Nullable
    private ItemStack encodePattern() {
        throw new AssertionError();
    }

    @Inject(
            method = "encode",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void bmaddon$encodeUsingBloodAltarPattern(CallbackInfo callback) {
        PatternEncodingTermMenu menu = (PatternEncodingTermMenu) (Object) this;

        if (menu.getPlayer().level().isClientSide()) {
            return;
        }

        if (!shouldUseBloodAltarPatternContainer()) {
            return;
        }

        ItemStack encodedPattern = encodePattern();

        if (!isEncodedBloodAltarPattern(encodedPattern)) {
            callback.cancel();
            return;
        }

        ItemStack encodeOutput = encodedPatternSlot.getItem();

        if (!encodeOutput.isEmpty()
                && !PatternDetailsHelper.isEncodedPattern(encodeOutput)
                && !AEItems.BLANK_PATTERN.isSameAs(encodeOutput)
                && !isBloodAltarPattern(encodeOutput)) {
            callback.cancel();
            return;
        }

        if (encodeOutput.isEmpty()) {
            ItemStack blankPattern = blankPatternSlot.getItem();

            if (!isBlankBloodAltarPattern(blankPattern)) {
                callback.cancel();
                return;
            }

            blankPattern.shrink(1);

            if (blankPattern.isEmpty()) {
                blankPatternSlot.set(ItemStack.EMPTY);
            }
        }

        encodedPatternSlot.set(encodedPattern);
        callback.cancel();
    }

    @Inject(
            method = "encodeProcessingPattern",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void bmaddon$replaceProcessingPatternWithBloodAltarPattern(
            CallbackInfoReturnable<ItemStack> callback
    ) {
        if (!shouldUseBloodAltarPatternContainer()) {
            return;
        }

        ItemStack originalPattern = callback.getReturnValue();

        if (originalPattern == null || originalPattern.isEmpty()) {
            return;
        }

        PatternEncodingTermMenu menu = (PatternEncodingTermMenu) (Object) this;
        Level level = menu.getPlayer().level();

        BloodAltarPatternEncodingHelper.tryConvertAe2ProcessingPattern(originalPattern, level)
                .ifPresent(callback::setReturnValue);
    }

    @Inject(
            method = "isPattern",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void bmaddon$treatBloodAltarPatternAsBlankPattern(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (isBlankBloodAltarPattern(stack)) {
            callback.setReturnValue(true);
        }
    }

    @Inject(
            method = "clearPattern",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void bmaddon$clearBloodAltarPatternToBloodAltarBlankPattern(CallbackInfo callback) {
        ItemStack encodedPattern = encodedPatternSlot.getItem();

        if (!isBloodAltarPattern(encodedPattern)) {
            return;
        }

        encodedPatternSlot.set(new ItemStack(
                BMAddonItems.BLOOD_ALTAR_PATTERN.get(),
                encodedPattern.getCount()
        ));

        callback.cancel();
    }

    private boolean shouldUseBloodAltarPatternContainer() {
        ItemStack blankPattern = blankPatternSlot.getItem();
        ItemStack encodedPattern = encodedPatternSlot.getItem();

        return isBlankBloodAltarPattern(blankPattern)
                || isBloodAltarPattern(encodedPattern);
    }

    private static boolean isBloodAltarPattern(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BMAddonItems.BLOOD_ALTAR_PATTERN.get());
    }

    private static boolean isBlankBloodAltarPattern(ItemStack stack) {
        return isBloodAltarPattern(stack) && !BloodAltarPatternItem.isEncoded(stack);
    }

    private static boolean isEncodedBloodAltarPattern(@Nullable ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.is(BMAddonItems.BLOOD_ALTAR_PATTERN.get())
                && BloodAltarPatternItem.isEncoded(stack);
    }
}