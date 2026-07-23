package com.edgemq.bmaddon.client.render;

import com.edgemq.bmaddon.blockentity.BloodAltarAssemblerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class BloodAltarAssemblerRenderer implements BlockEntityRenderer<BloodAltarAssemblerBlockEntity, BlockEntityRenderState> {
    public BloodAltarAssemblerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(
            BlockEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
    }
}
