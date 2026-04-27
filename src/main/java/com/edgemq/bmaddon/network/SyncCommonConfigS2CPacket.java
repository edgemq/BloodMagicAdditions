package com.edgemq.bmaddon.network;

import com.edgemq.bmaddon.config.SyncedBMAddonConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SyncCommonConfigS2CPacket {
    private final SyncedBMAddonConfig.Snapshot snapshot;

    public SyncCommonConfigS2CPacket(SyncedBMAddonConfig.Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public static void encode(SyncCommonConfigS2CPacket packet, FriendlyByteBuf buffer) {
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

    public static SyncCommonConfigS2CPacket decode(FriendlyByteBuf buffer) {
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

    public static void handle(SyncCommonConfigS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> SyncedBMAddonConfig.setClientSnapshot(packet.snapshot));
        context.setPacketHandled(true);
    }
}