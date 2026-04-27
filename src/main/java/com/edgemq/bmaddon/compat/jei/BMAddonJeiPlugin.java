package com.edgemq.bmaddon.compat.jei;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.registry.BMAddonItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.compat.jei.altar.BloodAltarRecipeCategory;
import wayoftime.bloodmagic.compat.jei.alchemytable.AlchemyTableRecipeCategory;

@JeiPlugin
public final class BMAddonJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(
            BMAddon.MODID,
            "jei_plugin"
    );

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack assembler = new ItemStack(BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get());
        registration.addRecipeCatalyst(assembler, BloodAltarRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(assembler, AlchemyTableRecipeCategory.RECIPE_TYPE);
    }
}