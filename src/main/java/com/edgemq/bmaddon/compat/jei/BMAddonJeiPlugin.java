package com.edgemq.bmaddon.compat.jei;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.registry.BMAddonItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class BMAddonJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = ResourceLocation.fromNamespaceAndPath(
            BMAddon.MODID,
            "jei_plugin"
    );

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    }
}
