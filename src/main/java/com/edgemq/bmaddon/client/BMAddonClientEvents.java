package com.edgemq.bmaddon.client;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.client.screen.BloodGeneratorScreen;
import com.edgemq.bmaddon.registry.BMAddonMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = BMAddon.MODID,
        value = Dist.CLIENT
)
public final class BMAddonClientEvents {
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(BMAddonMenus.BLOOD_GENERATOR.get(), BloodGeneratorScreen::new);
    }

    private BMAddonClientEvents() {
    }
}
