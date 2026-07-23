package com.edgemq.bmaddon.block;

import com.edgemq.bmaddon.blockentity.BloodGeneratorBlockEntity;
import com.edgemq.bmaddon.network.BMAddonNetwork;
import com.edgemq.bmaddon.registry.BMAddonBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BloodGeneratorBlock extends BaseEntityBlock {
    public static final MapCodec<BloodGeneratorBlock> CODEC = simpleCodec(BloodGeneratorBlock::new);
    private static final VoxelShape SHAPE = box(
            0.0D,
            0.0D,
            0.0D,
            16.0D,
            15.99D,
            16.0D
    );

    public BloodGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloodGeneratorBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(
                blockEntityType,
                BMAddonBlockEntities.BLOOD_GENERATOR.get(),
                BloodGeneratorBlockEntity::serverTick
        );
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof BloodGeneratorBlockEntity bloodGenerator && player instanceof ServerPlayer serverPlayer) {
            BMAddonNetwork.sendConfigToPlayer(serverPlayer);
            serverPlayer.openMenu(
                    new SimpleMenuProvider(bloodGenerator::createMenu, bloodGenerator.getDisplayName()),
                    pos
            );
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
