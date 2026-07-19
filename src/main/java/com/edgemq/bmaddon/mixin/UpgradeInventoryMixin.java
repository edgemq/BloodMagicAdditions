package com.edgemq.bmaddon.mixin;

import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.core.definitions.AEItems;
import com.edgemq.bmaddon.item.BloodMagicSpeedCardItem;
import com.edgemq.bmaddon.registry.BMAddonItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "appeng.api.upgrades.UpgradeInventory", remap = false)
public abstract class UpgradeInventoryMixin {
    @Inject(
            method = "getInstalledUpgrades",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void bmaddon$countBloodMagicSpeedCardAsTwoSpeedCards(
            ItemLike upgradeCard,
            CallbackInfoReturnable<Integer> callback
    ) {
        if (upgradeCard.asItem() != AEItems.SPEED_CARD.asItem()) {
            return;
        }

        InternalInventory inventory = (InternalInventory) (Object) this;
        IUpgradeInventory upgradeInventory = (IUpgradeInventory) (Object) this;

        int bloodMagicSpeedCards = 0;

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);

            if (!stack.isEmpty() && stack.is(BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get())) {
                bloodMagicSpeedCards += stack.getCount();
            }
        }

        if (bloodMagicSpeedCards <= 0) {
            return;
        }

        int maxSpeedCards = upgradeInventory.getMaxInstalled(AEItems.SPEED_CARD);
        int vanillaEquivalent = bloodMagicSpeedCards * BloodMagicSpeedCardItem.SPEED_CARD_EQUIVALENT;
        int total = callback.getReturnValue() + vanillaEquivalent;

        if (maxSpeedCards > 0) {
            total = Math.min(maxSpeedCards, total);
        }

        callback.setReturnValue(total);
    }
}