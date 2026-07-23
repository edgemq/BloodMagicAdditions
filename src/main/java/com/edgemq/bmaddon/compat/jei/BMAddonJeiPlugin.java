package com.edgemq.bmaddon.compat.jei;

import com.breakinblocks.neovitae.compat.jei.altar.AraVitaeRecipeCategory;
import com.breakinblocks.neovitae.compat.jei.tabulavitae.TabulaVitaeRecipeCategory;
import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.registry.BMAddonItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public final class BMAddonJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_UID = Identifier.fromNamespaceAndPath(
            BMAddon.MODID,
            "jei_plugin"
    );

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack assembler = new ItemStack(BMAddonItems.BLOOD_ALTAR_ASSEMBLER.get());
        registration.addRecipeCatalyst(assembler, AraVitaeRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(assembler, TabulaVitaeRecipeCategory.RECIPE_TYPE);
    }
}
