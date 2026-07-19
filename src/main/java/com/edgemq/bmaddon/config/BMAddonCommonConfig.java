package com.edgemq.bmaddon.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BMAddonCommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue BLOOD_GENERATOR_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue BLOOD_GENERATOR_MAX_ENERGY_INPUT;
    public static final ModConfigSpec.IntValue BLOOD_GENERATOR_LIFE_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue BLOOD_GENERATOR_ENERGY_PER_OPERATION;
    public static final ModConfigSpec.IntValue BLOOD_GENERATOR_LIFE_ESSENCE_PER_OPERATION;
    public static final ModConfigSpec.IntValue BLOOD_GENERATOR_WORK_INTERVAL_TICKS;
    public static final ModConfigSpec.BooleanValue BLOOD_GENERATOR_AUTO_OUTPUT;
    public static final ModConfigSpec.IntValue BLOOD_GENERATOR_MAX_FLUID_OUTPUT_PER_TICK;

    public static final ModConfigSpec.IntValue BLOOD_ALTAR_ASSEMBLER_BASE_CRAFT_TIME_TICKS;
    public static final ModConfigSpec.IntValue BLOOD_ALTAR_ASSEMBLER_MIN_CRAFT_TIME_TICKS;
    public static final ModConfigSpec.DoubleValue BLOOD_ALTAR_ASSEMBLER_AE_PER_TICK_BASE;
    public static final ModConfigSpec.DoubleValue BLOOD_ALTAR_ASSEMBLER_AE_PER_TICK_PER_ACCELERATION_CARD;
    public static final ModConfigSpec.DoubleValue BLOOD_ALTAR_ASSEMBLER_LIFE_ESSENCE_MULTIPLIER;
    public static final ModConfigSpec.IntValue BLOOD_ALTAR_ASSEMBLER_BASE_PARALLEL_CRAFTS;
    public static final ModConfigSpec.IntValue BLOOD_ALTAR_ASSEMBLER_PARALLEL_CRAFTS_PER_CARD;
    public static final ModConfigSpec.IntValue BLOOD_ALTAR_ASSEMBLER_MAX_PARALLEL_CRAFTS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("blood_generator");

        BLOOD_GENERATOR_ENERGY_CAPACITY = builder
                .comment("Maximum Forge Energy stored by the Blood Generator.")
                .defineInRange("energyCapacity", 100_000, 1, Integer.MAX_VALUE);

        BLOOD_GENERATOR_MAX_ENERGY_INPUT = builder
                .comment("Maximum Forge Energy accepted per insertion.")
                .defineInRange("maxEnergyInput", 2_000, 1, Integer.MAX_VALUE);

        BLOOD_GENERATOR_LIFE_TANK_CAPACITY = builder
                .comment("Maximum Life Essence stored by the Blood Generator, in mB.")
                .defineInRange("lifeTankCapacity", 16_000, 1, Integer.MAX_VALUE);

        BLOOD_GENERATOR_ENERGY_PER_OPERATION = builder
                .comment("Forge Energy consumed per generation operation.")
                .defineInRange("energyPerOperation", 1_000, 1, Integer.MAX_VALUE);

        BLOOD_GENERATOR_LIFE_ESSENCE_PER_OPERATION = builder
                .comment("Life Essence generated per operation, in mB.")
                .defineInRange("lifeEssencePerOperation", 10, 1, Integer.MAX_VALUE);

        BLOOD_GENERATOR_WORK_INTERVAL_TICKS = builder
                .comment("How often the generator runs. 1 = every tick, 20 = once per second.")
                .defineInRange("workIntervalTicks", 1, 1, 20 * 60);

        BLOOD_GENERATOR_AUTO_OUTPUT = builder
                .comment("If true, the Blood Generator will push Life Essence into adjacent fluid handlers.")
                .define("autoOutput", true);

        BLOOD_GENERATOR_MAX_FLUID_OUTPUT_PER_TICK = builder
                .comment("Maximum Life Essence pushed to adjacent fluid handlers per tick, in mB.")
                .defineInRange("maxFluidOutputPerTick", 100, 1, Integer.MAX_VALUE);

        builder.pop();

        builder.push("blood_altar_assembler");

        BLOOD_ALTAR_ASSEMBLER_BASE_CRAFT_TIME_TICKS = builder
                .comment("Base craft time for Blood Altar Assembler, in ticks.")
                .defineInRange("baseCraftTimeTicks", 200, 1, 20 * 60 * 60);

        BLOOD_ALTAR_ASSEMBLER_MIN_CRAFT_TIME_TICKS = builder
                .comment("Minimum craft time after acceleration cards, in ticks.")
                .defineInRange("minCraftTimeTicks", 20, 1, 20 * 60 * 60);

        BLOOD_ALTAR_ASSEMBLER_AE_PER_TICK_BASE = builder
                .comment("Base AE energy consumed per active craft each tick.")
                .defineInRange("aePerTickBase", 8.0D, 0.0D, Double.MAX_VALUE);

        BLOOD_ALTAR_ASSEMBLER_AE_PER_TICK_PER_ACCELERATION_CARD = builder
                .comment("Additional AE energy consumed per active craft each tick per acceleration card.")
                .defineInRange("aePerTickPerAccelerationCard", 12.0D, 0.0D, Double.MAX_VALUE);

        BLOOD_ALTAR_ASSEMBLER_LIFE_ESSENCE_MULTIPLIER = builder
                .comment("Multiplier for Life Essence required by Blood Altar recipes.")
                .defineInRange("lifeEssenceMultiplier", 1.0D, 0.0D, Double.MAX_VALUE);

        BLOOD_ALTAR_ASSEMBLER_BASE_PARALLEL_CRAFTS = builder
                .comment("How many crafts the Blood Altar Assembler can run without parallel cards.")
                .defineInRange("baseParallelCrafts", 1, 1, 64);

        BLOOD_ALTAR_ASSEMBLER_PARALLEL_CRAFTS_PER_CARD = builder
                .comment("How many additional parallel crafts each Blood Altar Parallel Card adds.")
                .defineInRange("parallelCraftsPerCard", 1, 0, 64);

        BLOOD_ALTAR_ASSEMBLER_MAX_PARALLEL_CRAFTS = builder
                .comment("Hard cap for active crafts in one Blood Altar Assembler.")
                .defineInRange("maxParallelCrafts", 4, 1, 64);

        builder.pop();

        SPEC = builder.build();
    }

    private BMAddonCommonConfig() {
    }
}
