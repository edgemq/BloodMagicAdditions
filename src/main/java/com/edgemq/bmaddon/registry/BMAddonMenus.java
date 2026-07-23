package com.edgemq.bmaddon.registry;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.menu.BloodAltarAssemblerMenu;
import com.edgemq.bmaddon.menu.BloodGeneratorMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class BMAddonMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, BMAddon.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BloodGeneratorMenu>> BLOOD_GENERATOR =
            MENUS.register(
                    "blood_generator",
                    () -> new MenuType<>((IContainerFactory<BloodGeneratorMenu>) BloodGeneratorMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static final DeferredHolder<MenuType<?>, MenuType<BloodAltarAssemblerMenu>> BLOOD_ALTAR_ASSEMBLER =
            MENUS.register(
                    "blood_altar_assembler",
                    () -> new MenuType<>((IContainerFactory<BloodAltarAssemblerMenu>) BloodAltarAssemblerMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    private BMAddonMenus() {
    }
}
