package com.edgemq.bmaddon.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.RestrictedInputSlot;
import com.edgemq.bmaddon.blockentity.BloodAltarAssemblerBlockEntity;
import com.edgemq.bmaddon.menu.slot.BloodAltarPatternSlot;
import com.edgemq.bmaddon.registry.BMAddonMenus;
import com.edgemq.bmaddon.registry.BMAddonSlotSemantics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BloodAltarAssemblerMenu extends AEBaseMenu {
    private final BloodAltarAssemblerBlockEntity host;

    public BloodAltarAssemblerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(
                BMAddonMenus.BLOOD_ALTAR_ASSEMBLER.get(),
                containerId,
                playerInventory,
                getBlockEntity(playerInventory, buffer.readBlockPos())
        );
    }

    public BloodAltarAssemblerMenu(
            int containerId,
            Inventory playerInventory,
            BloodAltarAssemblerBlockEntity host
    ) {
        this(
                BMAddonMenus.BLOOD_ALTAR_ASSEMBLER.get(),
                containerId,
                playerInventory,
                host
        );
    }

    public BloodAltarAssemblerMenu(
            MenuType<?> menuType,
            int containerId,
            Inventory playerInventory,
            BloodAltarAssemblerBlockEntity host
    ) {
        super(menuType, containerId, playerInventory, host);

        this.host = host;

        createPlayerInventorySlots(playerInventory);
        addPatternSlots();
        addTierCardSlots();
    }

    private static BloodAltarAssemblerBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);

        if (blockEntity instanceof BloodAltarAssemblerBlockEntity assembler) {
            return assembler;
        }

        throw new IllegalStateException("Expected BloodAltarAssemblerBlockEntity at " + pos);
    }

    private void addPatternSlots() {
        var patternInventory = host.getPatternInventory();

        for (int slot = 0; slot < BloodAltarAssemblerBlockEntity.PATTERN_SLOT_COUNT; slot++) {
            addSlot(
                    new BloodAltarPatternSlot(patternInventory, slot),
                    BMAddonSlotSemantics.BLOOD_ALTAR_PATTERN
            );
        }
    }

    private void addTierCardSlots() {
        var upgrades = host.getUpgrades();

        for (int slot = 0; slot < BloodAltarAssemblerBlockEntity.UPGRADE_SLOT_COUNT; slot++) {
            addSlot(
                    new RestrictedInputSlot(
                            RestrictedInputSlot.PlacableItemType.UPGRADES,
                            upgrades,
                            slot
                    ).setStackLimit(1),
                    SlotSemantics.UPGRADE
            );
        }
    }

    public BloodAltarAssemblerBlockEntity getHost() {
        return host;
    }

    public int getAltarTier() {
        return host.getAltarTier();
    }
}
