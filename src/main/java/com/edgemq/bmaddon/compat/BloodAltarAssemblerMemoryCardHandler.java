package com.edgemq.bmaddon.compat;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.inventories.InternalInventory;
import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.blockentity.BloodAltarAssemblerBlockEntity;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;


@EventBusSubscriber(modid = BMAddon.MODID)
public final class BloodAltarAssemblerMemoryCardHandler {

    private static final String TAG_BLOOD_PATTERNS = "BMAddonBloodPatterns";

    private BloodAltarAssemblerMemoryCardHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();


        if (level.isClientSide()) {
            return;
        }

        ItemStack heldItem = event.getItemStack();

        if (!(heldItem.getItem() instanceof IMemoryCard)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(event.getPos());

        if (!(blockEntity instanceof BloodAltarAssemblerBlockEntity assembler)) {
            return;
        }

        Player player = event.getEntity();

        if (player.isShiftKeyDown()) {
            copyBloodPatterns(assembler, heldItem);
        } else {
            pasteBloodPatterns(assembler, heldItem, player);
        }
    }

    private static void copyBloodPatterns(BloodAltarAssemblerBlockEntity assembler, ItemStack memoryCardStack) {
    }

    private static void pasteBloodPatterns(
            BloodAltarAssemblerBlockEntity assembler,
            ItemStack memoryCardStack,
            Player player
    ) {
        ListTag list = new ListTag();
        InternalInventory patternInventory = assembler.getPatternInventory();
        boolean creative = player.isCreative();
        boolean missingAny = false;
        boolean pastedAny = false;

        for (int slot = 0; slot < list.size() && slot < patternInventory.size(); slot++) {
            CompoundTag entry = list.getCompound(slot);

            if (entry.isEmpty()) {
                continue;
            }


            if (!patternInventory.getStackInSlot(slot).isEmpty()) {
                continue;
            }

            ItemStack wanted = ItemStack.EMPTY;

            if (wanted.isEmpty()) {
                continue;
            }

            ItemStack toInsert = resolvePatternForInsertion(player, wanted, creative);

            if (toInsert == null || toInsert.isEmpty()) {
                missingAny = true;
                continue;
            }

            patternInventory.setItemDirect(slot, toInsert);
            pastedAny = true;
        }

        if (pastedAny) {
            assembler.saveChanges();
        }

        if (missingAny) {
            player.displayClientMessage(
                    Component.translatable("chat.bmaddon.memory_card.missing_blank_patterns"),
                    true
            );
        }
    }


    private static ItemStack resolvePatternForInsertion(Player player, ItemStack wanted, boolean creative) {
        if (creative) {
            return wanted.copy();
        }

        if (consumeBlankPattern(player, wanted)) {
            return wanted.copy();
        }

        return null;
    }


    private static boolean consumeBlankPattern(Player player, ItemStack wanted) {
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);

            if (candidate.isEmpty() || candidate.getItem() != wanted.getItem()) {
                continue;
            }


            if (BloodAltarPatternItem.isEncoded(candidate)) {
                continue;
            }

            candidate.shrink(1);
            inventory.setChanged();
            return true;
        }

        return false;
    }
}
