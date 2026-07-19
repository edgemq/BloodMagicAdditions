package com.edgemq.bmaddon.event;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.network.BMAddonNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(
        modid = BMAddon.MODID,
        bus = EventBusSubscriber.Bus.GAME
)
public final class BMAddonForgeEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BMAddonNetwork.sendConfigToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BMAddonNetwork.sendConfigToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BMAddonNetwork.sendConfigToPlayer(player);
        }
    }

    private BMAddonForgeEvents() {
    }
}
