package com.edgemq.bmaddon.event;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.network.BMAddonNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = BMAddon.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
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