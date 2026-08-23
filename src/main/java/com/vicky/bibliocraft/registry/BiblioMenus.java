package com.vicky.bibliocraft.registry;

import com.vicky.bibliocraft.BiblioCraftLegacy;
import com.vicky.bibliocraft.menu.BiblioContainerMenu;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.inventory.MenuType;

public final class BiblioMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, BiblioCraftLegacy.MODID);

    public static final RegistryObject<MenuType<BiblioContainerMenu>> BIBLIO_CONTAINER =
            MENUS.register("biblio_container",
                    () -> IForgeMenuType.create(BiblioContainerMenu::new));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }

    private BiblioMenus() {}
}
