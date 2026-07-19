package com.edgemq.bmaddon.mixin;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import com.edgemq.bmaddon.registry.BMAddonItems;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Upgrades.class, remap = false)
public abstract class UpgradesMixin {
    @Inject(
            method = "getMaxInstallable",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void bmaddon$allowBloodMagicSpeedCardWhereAe2SpeedCardWorks(
            ItemLike card,
            ItemLike upgradableItem,
            CallbackInfoReturnable<Integer> callback
    ) {
        if (card.asItem() != BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get()) {
            return;
        }

        int speedCardMax = Upgrades.getMaxInstallable(AEItems.SPEED_CARD, upgradableItem);

        if (speedCardMax > 0) {
            callback.setReturnValue(speedCardMax);
        }
    }
}