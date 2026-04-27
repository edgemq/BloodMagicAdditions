package com.edgemq.bmaddon.registry;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.block.BloodAltarAssemblerBlock;
import com.edgemq.bmaddon.block.BloodGeneratorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BMAddonBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BMAddon.MODID);

    public static final RegistryObject<Block> BLOOD_GENERATOR = BLOCKS.register(
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

    public static final RegistryObject<Block> BLOOD_ALTAR_ASSEMBLER = BLOCKS.register(
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