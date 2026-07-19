package com.edgemq.bmaddon.event;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.localization.GuiText;
import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.ae2.BloodAltarPatternDecoder;
import com.edgemq.bmaddon.config.BMAddonCommonConfig;
import com.edgemq.bmaddon.network.BMAddonNetwork;
import com.edgemq.bmaddon.registry.BMAddonItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(
        modid = BMAddon.MODID,
        bus = EventBusSubscriber.Bus.MOD
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
                registerBloodMagicSpeedCardAe2Upgrades();
                registeredUpgrades = true;
            }
        });
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                com.edgemq.bmaddon.registry.BMAddonBlockEntities.BLOOD_GENERATOR.get(),
                (blockEntity, side) -> blockEntity.getEnergyHandler()
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                com.edgemq.bmaddon.registry.BMAddonBlockEntities.BLOOD_GENERATOR.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler()
        );
    }

    private static void registerBloodAltarAssemblerUpgrades() {
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_TIER_CARD_2.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 1);
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_TIER_CARD_3.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 1);
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_TIER_CARD_4.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 1);
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_TIER_CARD_5.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 1);

        Upgrades.add(AEItems.SPEED_CARD, BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 4);
        Upgrades.add(BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 4);
        Upgrades.add(BMAddonItems.BLOOD_ALTAR_PARALLEL_CARD.get(), BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get(), 4);
    }

    private static void registerBloodMagicSpeedCardAe2Upgrades() {
        String itemIoBusGroup = GuiText.IOBuses.getTranslationKey();



        Upgrades.add(BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get(), AEBlocks.IO_PORT, 3);

        Upgrades.add(BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get(), AEParts.IMPORT_BUS, 4, itemIoBusGroup);
        Upgrades.add(BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get(), AEParts.EXPORT_BUS, 4, itemIoBusGroup);

        Upgrades.add(BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get(), AEItems.MATTER_CANNON, 4);

        Upgrades.add(BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get(), AEBlocks.MOLECULAR_ASSEMBLER, 5);
        Upgrades.add(BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get(), AEBlocks.INSCRIBER, 4);
        Upgrades.add(BMAddonItems.BLOOD_MAGIC_SPEED_CARD.get(), AEBlocks.VIBRATION_CHAMBER, 3);
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
