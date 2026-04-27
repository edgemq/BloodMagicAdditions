package com.edgemq.bmaddon;

import com.edgemq.bmaddon.config.BMAddonCommonConfig;
import com.edgemq.bmaddon.network.BMAddonNetwork;
import com.edgemq.bmaddon.registry.BMAddonBlockEntities;
import com.edgemq.bmaddon.registry.BMAddonBlocks;
import com.edgemq.bmaddon.registry.BMAddonItems;
import com.edgemq.bmaddon.registry.BMAddonMenus;
import com.edgemq.bmaddon.registry.BMAddonSlotSemantics;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BMAddon.MODID)
public class BMAddon {
    public static final String MODID = "bmaddon";

    public BMAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BMAddonSlotSemantics.init();

        BMAddonBlocks.register(modEventBus);
        BMAddonItems.register(modEventBus);
        BMAddonBlockEntities.register(modEventBus);
        BMAddonMenus.register(modEventBus);

        BMAddonNetwork.register();

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                BMAddonCommonConfig.SPEC,
                BMAddon.MODID + "-common.toml"
        );
    }
}