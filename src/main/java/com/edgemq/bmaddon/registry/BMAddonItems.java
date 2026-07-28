package com.edgemq.bmaddon.registry;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.item.BMAddonBlockItem;
import com.edgemq.bmaddon.item.BloodAltarParallelCardItem;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.item.BloodAltarTierCardItem;
import com.edgemq.bmaddon.item.BloodMagicSpeedCardItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BMAddonItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.createItems(BMAddon.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BMAddon.MODID);

    public static final DeferredHolder<Item, Item> BLOOD_GENERATOR = ITEMS.register(
            "blood_generator",
            () -> new BMAddonBlockItem(
                    BMAddonBlocks.BLOOD_GENERATOR.get(),
                    new Item.Properties(),
                    "tooltip.bmaddon.blood_generator.description"
            )
    );

    public static final DeferredHolder<Item, Item> BLOOD_ALTAR_ASSEMBLER = ITEMS.register(
            "blood_altar_assembler",
            () -> new BMAddonBlockItem(
                    BMAddonBlocks.BLOOD_ALTAR_ASSEMBLER.get(),
                    new Item.Properties(),
                    "tooltip.bmaddon.blood_altar_assembler.description"
            )
    );

    public static final DeferredHolder<Item, Item> BLOOD_ALTAR_PATTERN = ITEMS.register(
            "blood_altar_pattern",
            () -> new BloodAltarPatternItem(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> BLOOD_ALTAR_TIER_CARD_2 = ITEMS.register(
            "blood_altar_tier_card_2",
            () -> new BloodAltarTierCardItem(2, new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> BLOOD_ALTAR_TIER_CARD_3 = ITEMS.register(
            "blood_altar_tier_card_3",
            () -> new BloodAltarTierCardItem(3, new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> BLOOD_ALTAR_TIER_CARD_4 = ITEMS.register(
            "blood_altar_tier_card_4",
            () -> new BloodAltarTierCardItem(4, new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> BLOOD_ALTAR_TIER_CARD_5 = ITEMS.register(
            "blood_altar_tier_card_5",
            () -> new BloodAltarTierCardItem(5, new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> BLOOD_ALTAR_PARALLEL_CARD = ITEMS.register(
            "blood_altar_parallel_card",
            () -> new BloodAltarParallelCardItem(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> BLOOD_MAGIC_SPEED_CARD = ITEMS.register(
            "blood_magic_speed_card",
            () -> new BloodMagicSpeedCardItem(new Item.Properties())
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.bmaddon.main"))
                    .icon(() -> new ItemStack(BLOOD_GENERATOR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(BLOOD_GENERATOR.get());
                        output.accept(BLOOD_ALTAR_ASSEMBLER.get());
                        output.accept(BLOOD_ALTAR_PATTERN.get());
                        output.accept(BLOOD_ALTAR_TIER_CARD_2.get());
                        output.accept(BLOOD_ALTAR_TIER_CARD_3.get());
                        output.accept(BLOOD_ALTAR_TIER_CARD_4.get());
                        output.accept(BLOOD_ALTAR_PARALLEL_CARD.get());
                        output.accept(BLOOD_MAGIC_SPEED_CARD.get());
                    })
                    .build()
    );

    public static boolean isBloodAltarTierCard(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BloodAltarTierCardItem;
    }

    public static int getBloodAltarTierFromCard(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        if (stack.getItem() instanceof BloodAltarTierCardItem tierCardItem) {
            return tierCardItem.getTier();
        }

        return 0;
    }

    public static boolean isBloodAltarParallelCard(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BLOOD_ALTAR_PARALLEL_CARD.get());
    }

    public static boolean isBloodMagicSpeedCard(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BLOOD_MAGIC_SPEED_CARD.get());
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }

    private BMAddonItems() {
    }
}
