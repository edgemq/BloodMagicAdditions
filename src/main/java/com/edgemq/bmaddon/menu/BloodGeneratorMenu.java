package com.edgemq.bmaddon.menu;

import com.edgemq.bmaddon.blockentity.BloodGeneratorBlockEntity;
import com.edgemq.bmaddon.registry.BMAddonBlocks;
import com.edgemq.bmaddon.registry.BMAddonMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BloodGeneratorMenu extends AbstractContainerMenu {
    /*
     * Эти координаты отвечают именно за предметы в слотах.
     * GUI-текстура двигается в BloodGeneratorScreen через leftPos,
     * а предметы относительно GUI двигаются здесь.
     */
    private static final int PLAYER_INVENTORY_X = 26;
    private static final int PLAYER_INVENTORY_Y = 104;

    private static final int HOTBAR_X = 26;
    private static final int HOTBAR_Y = 162;

    private final ContainerLevelAccess access;
    private final ContainerData data;

    public BloodGeneratorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, buffer.readBlockPos()));
    }

    public BloodGeneratorMenu(int containerId, Inventory playerInventory, BloodGeneratorBlockEntity blockEntity) {
        super(BMAddonMenus.BLOOD_GENERATOR.get(), containerId);

        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.data = blockEntity.createContainerData();

        addDataSlots(this.data);
        addPlayerInventorySlots(playerInventory);
    }

    private static BloodGeneratorBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);

        if (blockEntity instanceof BloodGeneratorBlockEntity bloodGenerator) {
            return bloodGenerator;
        }

        throw new IllegalStateException("Expected BloodGeneratorBlockEntity at " + pos);
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        PLAYER_INVENTORY_X + column * 18,
                        PLAYER_INVENTORY_Y + row * 18
                ));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    playerInventory,
                    column,
                    HOTBAR_X + column * 18,
                    HOTBAR_Y
            ));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, BMAddonBlocks.BLOOD_GENERATOR.get());
    }

    public int getEnergyStored() {
        return data.get(BloodGeneratorBlockEntity.DATA_ENERGY_STORED);
    }

    public int getMaxEnergyStored() {
        return data.get(BloodGeneratorBlockEntity.DATA_MAX_ENERGY_STORED);
    }

    public int getBloodAmount() {
        return data.get(BloodGeneratorBlockEntity.DATA_BLOOD_AMOUNT);
    }

    public int getBloodCapacity() {
        return data.get(BloodGeneratorBlockEntity.DATA_BLOOD_CAPACITY);
    }

    public float getEnergyRatio() {
        int max = getMaxEnergyStored();

        if (max <= 0) {
            return 0.0F;
        }

        return Math.min(1.0F, Math.max(0.0F, (float) getEnergyStored() / (float) max));
    }

    public float getBloodRatio() {
        int max = getBloodCapacity();

        if (max <= 0) {
            return 0.0F;
        }

        return Math.min(1.0F, Math.max(0.0F, (float) getBloodAmount() / (float) max));
    }
}