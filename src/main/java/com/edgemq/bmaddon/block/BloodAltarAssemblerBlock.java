package com.edgemq.bmaddon.block;

import appeng.block.AEBaseEntityBlock;
import appeng.util.InteractionUtil;
import com.edgemq.bmaddon.blockentity.BloodAltarAssemblerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BloodAltarAssemblerBlock extends AEBaseEntityBlock<BloodAltarAssemblerBlockEntity> {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public BloodAltarAssemblerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(
            BlockState currentState,
            BloodAltarAssemblerBlockEntity blockEntity
    ) {
        return currentState.setValue(POWERED, blockEntity.isPowered());
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        BloodAltarAssemblerBlockEntity blockEntity = getBlockEntity(level, pos);

        if (blockEntity == null) {
            return InteractionResult.PASS;
        }

        if (InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(blockEntity::createMenu, blockEntity.getDisplayName()),
                    pos
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
