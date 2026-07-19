package com.edgemq.bmaddon.network;

import com.edgemq.bmaddon.config.SyncedBMAddonConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class BMAddonNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION)
                .commonToClient(
                        SyncCommonConfigS2CPacket.TYPE,
                        SyncCommonConfigS2CPacket.STREAM_CODEC,
                        SyncCommonConfigS2CPacket::handle
                );
    }

    public static void sendConfigToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(
                player,
                new SyncCommonConfigS2CPacket(SyncedBMAddonConfig.fromCommonConfig())
        );
    }

    public static void sendConfigToAllPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server == null) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendConfigToPlayer(player);
        }
    }

    private BMAddonNetwork() {
    }
}
