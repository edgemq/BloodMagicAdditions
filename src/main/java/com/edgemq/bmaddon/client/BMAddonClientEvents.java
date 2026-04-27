package com.edgemq.bmaddon.client;

import appeng.init.client.InitScreens;
import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.client.render.BloodAltarAssemblerRenderer;
import com.edgemq.bmaddon.client.screen.BloodAltarAssemblerScreen;
import com.edgemq.bmaddon.client.screen.BloodGeneratorScreen;
import com.edgemq.bmaddon.item.BloodAltarPatternItem;
import com.edgemq.bmaddon.registry.BMAddonBlockEntities;
import com.edgemq.bmaddon.registry.BMAddonBlocks;
import com.edgemq.bmaddon.registry.BMAddonItems;
import com.edgemq.bmaddon.registry.BMAddonMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = BMAddon.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class BMAddonClientEvents {
    public static final ResourceLocation BLOOD_ALTAR_ASSEMBLER_LIGHTS_MODEL = new ResourceLocation(
            BMAddon.MODID,
            "block/blood_altar_assembler_lights"
    );

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(BLOOD_ALTAR_ASSEMBLER_LIGHTS_MODEL);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(
                    BMAddonBlocks.BLOOD_ALTAR_ASSEMBLER.get(),
                    RenderType.cutout()
            );

            MenuScreens.register(
                    BMAddonMenus.BLOOD_GENERATOR.get(),
                    BloodGeneratorScreen::new
            );

            InitScreens.register(
                    BMAddonMenus.BLOOD_ALTAR_ASSEMBLER.get(),
                    BloodAltarAssemblerScreen::new,
                    "/screens/blood_altar_assembler.json"
            );

            BlockEntityRenderers.register(
                    BMAddonBlockEntities.BLOOD_ALTAR_ASSEMBLER.get(),
                    BloodAltarAssemblerRenderer::new
            );
            ItemBlockRenderTypes.setRenderLayer(
                    BMAddonBlocks.BLOOD_GENERATOR.get(),
                    RenderType.cutout()
            );
            ItemProperties.register(
                    BMAddonItems.BLOOD_ALTAR_PATTERN.get(),
                    new ResourceLocation(BMAddon.MODID, "encoded"),
                    (stack, level, entity, seed) -> BloodAltarPatternItem.isEncoded(stack) ? 1.0F : 0.0F
            );
        });
    }

    private BMAddonClientEvents() {
    }
}