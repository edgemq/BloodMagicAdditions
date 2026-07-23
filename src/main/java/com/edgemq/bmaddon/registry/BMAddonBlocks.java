package com.edgemq.bmaddon.registry;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.block.BloodAltarAssemblerBlock;
import com.edgemq.bmaddon.block.BloodGeneratorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class BMAddonBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, BMAddon.MODID);

    public static final DeferredHolder<Block, Block> BLOOD_GENERATOR = BLOCKS.register(
            "blood_generator",
            () -> new BloodGeneratorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(4.0F, 6.0F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, Block> BLOOD_ALTAR_ASSEMBLER = BLOCKS.register(
            "blood_altar_assembler",
            () -> new BloodAltarAssemblerBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(4.0F, 6.0F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
            )
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private BMAddonBlocks() {
    }
}