package com.edgemq.bmaddon.registry;

import com.edgemq.bmaddon.BMAddon;
import com.edgemq.bmaddon.menu.BloodAltarAssemblerMenu;
import com.edgemq.bmaddon.menu.BloodGeneratorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BMAddonMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, BMAddon.MODID);

    public static final RegistryObject<MenuType<BloodGeneratorMenu>> BLOOD_GENERATOR =
            MENUS.register(
                    "blood_generator",
                    () -> IForgeMenuType.create(BloodGeneratorMenu::new)
            );

    public static final RegistryObject<MenuType<BloodAltarAssemblerMenu>> BLOOD_ALTAR_ASSEMBLER =
            MENUS.register(
                    "blood_altar_assembler",
                    () -> IForgeMenuType.create(BloodAltarAssemblerMenu::new)
            );

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    private BMAddonMenus() {
    }
}