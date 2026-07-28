package com.edgemq.bmaddon.mixin;

import com.breakinblocks.neovitae.common.recipe.AnointmentApplyRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnointmentApplyRecipe.class)
public abstract class AnointmentApplyRecipeMixin {
    @Mutable
    @Final
    @Shadow(remap = false)
    public static StreamCodec<RegistryFriendlyByteBuf, AnointmentApplyRecipe> STREAM_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void bmaddon$replaceAnointmentApplyStreamCodec(CallbackInfo ci) {
        STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                },
                buffer -> new AnointmentApplyRecipe()
        );
    }
}
