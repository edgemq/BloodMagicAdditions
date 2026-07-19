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
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(
        modid = BMAddon.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class BMAddonClientEvents {
    public static final ResourceLocation BLOOD_ALTAR_ASSEMBLER_LIGHTS_MODEL = ResourceLocation.fromNamespaceAndPath(
            BMAddon.MODID,
            "block/blood_altar_assembler_lights"
    );
    public static final ModelResourceLocation BLOOD_ALTAR_ASSEMBLER_LIGHTS_MODEL_LOCATION =
            ModelResourceLocation.standalone(BLOOD_ALTAR_ASSEMBLER_LIGHTS_MODEL);

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(BLOOD_ALTAR_ASSEMBLER_LIGHTS_MODEL_LOCATION);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(
                BMAddonMenus.BLOOD_GENERATOR.get(),
                BloodGeneratorScreen::new
        );

        InitScreens.register(
                event,
                BMAddonMenus.BLOOD_ALTAR_ASSEMBLER.get(),
                BloodAltarAssemblerScreen::new,
                "/screens/blood_altar_assembler.json"
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(
                    BMAddonBlocks.BLOOD_ALTAR_ASSEMBLER.get(),
                    RenderType.cutout()
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
                    ResourceLocation.fromNamespaceAndPath(BMAddon.MODID, "encoded"),
                    (stack, level, entity, seed) -> BloodAltarPatternItem.isEncoded(stack) ? 1.0F : 0.0F
            );
        });
    }

    private BMAddonClientEvents() {
    }
}
