package com.edgemq.bmaddon.event;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.ae2.BloodAltarPatternDecoder;
import com.edgemq.bmaddon.blockentity.BloodAltarAssemblerBlockEntity;
import com.edgemq.bmaddon.config.BMAddonCommonConfig;
import com.edgemq.bmaddon.network.BMAddonNetwork;
import com.edgemq.bmaddon.registry.BMAddonItems;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(
        modid = BMAddon.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class BMAddonModEvents {
    private static boolean registeredPatternDecoder;
    private static boolean registeredUpgrades;

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (!registeredPatternDecoder) {
                PatternDetailsHelper.registerDecoder(BloodAltarPatternDecoder.INSTANCE);
                registeredPatternDecoder = true;
            }

            if (!registeredUpgrades) {
                registerBloodAltarAssemblerUpgrades();
                registeredUpgrades = true;
            }
        });
    }

    private static void registerBloodAltarAssemblerUpgrades() {
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_TIER_CARD_2.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 1);
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_TIER_CARD_3.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 1);
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_TIER_CARD_4.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 1);
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_TIER_CARD_5.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 1);

        Upgrades.add(AEItems.SPEED_CARD, BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 4);
        Upgrades.add(
                BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get(),
                BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(),
                BloodAltarAssemblerBlockEntity.UPGRADE_SLOT_COUNT
        );
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_PARALLEL_CARD.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 4);
    }

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (isOurCommonConfig(event.getConfig())) {
            BMAddonNetwork.sendConfigToAllPlayers();
        }
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (isOurCommonConfig(event.getConfig())) {
            BMAddonNetwork.sendConfigToAllPlayers();
        }
    }

    private static boolean isOurCommonConfig(ModConfig config) {
        return config.getType() == ModConfig.Type.COMMON && config.getSpec() == BMAddonCommonConfig.SPEC;
    }

    private BMAddonModEvents() {
    }
}
