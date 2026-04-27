package com.edgemq.bmaddon.config;

public final class SyncedBMAddonConfig {
    private static volatile Snapshot clientSnapshot = Snapshot.defaults();

    public static Snapshot fromCommonConfig() {
        return new Snapshot(
                BMAddonCommonConfig.BLOOD_GENERATOR_ENERGY_CAPACITY.get(),
                BMAddonCommonConfig.BLOOD_GENERATOR_MAX_ENERGY_INPUT.get(),
                BMAddonCommonConfig.BLOOD_GENERATOR_LIFE_TANK_CAPACITY.get(),
                BMAddonCommonConfig.BLOOD_GENERATOR_ENERGY_PER_OPERATION.get(),
                BMAddonCommonConfig.BLOOD_GENERATOR_LIFE_ESSENCE_PER_OPERATION.get(),
                BMAddonCommonConfig.BLOOD_GENERATOR_WORK_INTERVAL_TICKS.get(),
                BMAddonCommonConfig.BLOOD_GENERATOR_AUTO_OUTPUT.get(),
                BMAddonCommonConfig.BLOOD_GENERATOR_MAX_FLUID_OUTPUT_PER_TICK.get()
        );
    }

    public static Snapshot getClientSnapshot() {
        return clientSnapshot;
    }

    public static void setClientSnapshot(Snapshot snapshot) {
        clientSnapshot = snapshot;
    }

    private SyncedBMAddonConfig() {
    }

    public record Snapshot(
            int bloodGeneratorEnergyCapacity,
            int bloodGeneratorMaxEnergyInput,
            int bloodGeneratorLifeTankCapacity,
            int bloodGeneratorEnergyPerOperation,
            int bloodGeneratorLifeEssencePerOperation,
            int bloodGeneratorWorkIntervalTicks,
            boolean bloodGeneratorAutoOutput,
            int bloodGeneratorMaxFluidOutputPerTick
    ) {
        public static Snapshot defaults() {
            return new Snapshot(
                    100_000,
                    2_000,
                    16_000,
                    1_000,
                    10,
                    1,
                    true,
                    100
            );
        }
    }
}