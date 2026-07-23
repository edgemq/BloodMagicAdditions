package com.edgemq.bmaddon;

import com.edgemq.bmaddon.config.BMAddonCommonConfig;
import com.edgemq.bmaddon.event.BMAddonModEvents;
import com.edgemq.bmaddon.network.BMAddonNetwork;
import com.edgemq.bmaddon.registry.BMAddonBlockEntities;
import com.edgemq.bmaddon.registry.BMAddonBlocks;
import com.edgemq.bmaddon.registry.BMAddonItems;
import com.edgemq.bmaddon.registry.BMAddonMenus;
import com.edgemq.bmaddon.registry.BMAddonSlotSemantics;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(BMAddon.MODID)
public class BMAddon {
    public static final String MODID = "bmaddon";

    public BMAddon(IEventBus modEventBus, ModContainer container) {
        BMAddonSlotSemantics.init();

        BMAddonBlocks.register(modEventBus);
        BMAddonItems.register(modEventBus);
        BMAddonBlockEntities.register(modEventBus);
        BMAddonMenus.register(modEventBus);
        modEventBus.addListener(BMAddonModEvents::registerCapabilities);

        BMAddonNetwork.register();

        container.registerConfig(
                ModConfig.Type.COMMON,
                BMAddonCommonConfig.SPEC
        );
    }
}
