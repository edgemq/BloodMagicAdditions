package com.edgemq.bmaddon.network;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.config.SyncedBMAddonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class BMAddonNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BMAddon.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(SyncCommonConfigS2CPacket.class, nextPacketId())
                .encoder(SyncCommonConfigS2CPacket::encode)
                .decoder(SyncCommonConfigS2CPacket::decode)
                .consumerMainThread(SyncCommonConfigS2CPacket::handle)
                .add();
    }

    public static void sendConfigToPlayer(ServerPlayer player) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
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

    private static int nextPacketId() {
        return packetId++;
    }

    private BMAddonNetwork() {
    }
}