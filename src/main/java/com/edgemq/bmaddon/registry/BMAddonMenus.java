package com.edgemq.bmaddon.registry;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.menu.BloodAltarAssemblerMenu;
import com.edgemq.bmaddon.menu.BloodGeneratorMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class BMAddonMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, BMAddon.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BloodGeneratorMenu>> BLOOD_GENERATOR =
            MENUS.register(
                    "blood_generator",
                    () -> IMenuTypeExtension.create(BloodGeneratorMenu::new)
            );

    public static final DeferredHolder<MenuType<?>, MenuType<BloodAltarAssemblerMenu>> BLOOD_ALTAR_ASSEMBLER =
            MENUS.register(
                    "blood_altar_assembler",
                    () -> IMenuTypeExtension.create(BloodAltarAssemblerMenu::new)
            );

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    private BMAddonMenus() {
    }
}
