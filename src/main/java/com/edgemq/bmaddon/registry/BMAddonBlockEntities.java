package com.edgemq.bmaddon.registry;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.block.BloodAltarAssemblerBlock;
import com.edgemq.bmaddon.blockentity.BloodAltarAssemblerBlockEntity;
import com.edgemq.bmaddon.blockentity.BloodGeneratorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class BMAddonBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BMAddon.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloodGeneratorBlockEntity>> BLOOD_GENERATOR =
            BLOCK_ENTITIES.register(
                    "blood_generator",
                    () -> new BlockEntityType<>(
                            BloodGeneratorBlockEntity::new,
                            BMAddonBlocks.BLOOD_GENERATOR.get()
                    )
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloodAltarAssemblerBlockEntity>> BLOOD_ALTAR_ASSEMBLER =
            BLOCK_ENTITIES.register(
                    "blood_altar_assembler",
                    () -> {
                        BlockEntityType<BloodAltarAssemblerBlockEntity> type = new BlockEntityType<>(
                                BloodAltarAssemblerBlockEntity::new,
                                BMAddonBlocks.BLOOD_ALTAR_ASSEMBLER.get()
                        );

                        BloodAltarAssemblerBlock block = (BloodAltarAssemblerBlock) BMAddonBlocks.BLOOD_ALTAR_ASSEMBLER.get();

                        block.setBlockEntity(
                                BloodAltarAssemblerBlockEntity.class,
                                type,
                                null,
                                null
                        );

                        return type;
                    }
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

    private BMAddonBlockEntities() {
    }
}
