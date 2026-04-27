package com.edgemq.bmaddon.client.render;

import com.edgemq.bmaddon.blockentity.BloodAltarAssemblerBlockEntity;
import com.edgemq.bmaddon.client.BMAddonClientEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class BloodAltarAssemblerRenderer implements BlockEntityRenderer<BloodAltarAssemblerBlockEntity> {
    public BloodAltarAssemblerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            BloodAltarAssemblerBlockEntity assembler,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (assembler.isPowered()) {
            renderPowerLight(poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private void renderPowerLight(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        BakedModel lightsModel = minecraft.getModelManager().getModel(
                BMAddonClientEvents.BLOOD_ALTAR_ASSEMBLER_LIGHTS_MODEL
        );

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.tripwire());

        minecraft.getBlockRenderer()
                .getModelRenderer()
                .renderModel(
                        poseStack.last(),
                        buffer,
                        null,
                        lightsModel,
                        1.0F,
                        1.0F,
                        1.0F,
                        packedLight,
                        packedOverlay,
                        ModelData.EMPTY,
                        null
                );
    }
}