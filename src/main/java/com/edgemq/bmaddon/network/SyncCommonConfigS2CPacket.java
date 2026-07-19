package com.edgemq.bmaddon.network;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.config.SyncedBMAddonConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCommonConfigS2CPacket(SyncedBMAddonConfig.Snapshot snapshot) implements CustomPacketPayload {
    public static final Type<SyncCommonConfigS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BMAddon.MODID, "sync_common_config")
    );

    public static final StreamCodec<FriendlyByteBuf, SyncCommonConfigS2CPacket> STREAM_CODEC =
            CustomPacketPayload.codec(SyncCommonConfigS2CPacket::encode, SyncCommonConfigS2CPacket::decode);

    private static void encode(SyncCommonConfigS2CPacket packet, FriendlyByteBuf buffer) {
        SyncedBMAddonConfig.Snapshot snapshot = packet.snapshot;

        buffer.writeVarInt(snapshot.bloodGeneratorEnergyCapacity());
        buffer.writeVarInt(snapshot.bloodGeneratorMaxEnergyInput());
        buffer.writeVarInt(snapshot.bloodGeneratorLifeTankCapacity());
        buffer.writeVarInt(snapshot.bloodGeneratorEnergyPerOperation());
        buffer.writeVarInt(snapshot.bloodGeneratorLifeEssencePerOperation());
        buffer.writeVarInt(snapshot.bloodGeneratorWorkIntervalTicks());
        buffer.writeBoolean(snapshot.bloodGeneratorAutoOutput());
        buffer.writeVarInt(snapshot.bloodGeneratorMaxFluidOutputPerTick());
    }

    private static SyncCommonConfigS2CPacket decode(FriendlyByteBuf buffer) {
        SyncedBMAddonConfig.Snapshot snapshot = new SyncedBMAddonConfig.Snapshot(
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readVarInt()
        );

        return new SyncCommonConfigS2CPacket(snapshot);
    }

    public static void handle(SyncCommonConfigS2CPacket packet, IPayloadContext context) {
        SyncedBMAddonConfig.setClientSnapshot(packet.snapshot);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
