package com.vicky.bibliocraft;

import com.vicky.bibliocraft.registry.BiblioBlockEntities;
import com.vicky.bibliocraft.registry.BiblioBlocks;
import com.vicky.bibliocraft.registry.BiblioItems;
import com.vicky.bibliocraft.registry.BiblioMenus;
import com.vicky.bibliocraft.registry.BiblioSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BiblioCraftLegacy.MODID)
public final class BiblioCraftLegacy {
    public static final String MODID = "bibliocraft";

    public BiblioCraftLegacy() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BiblioBlocks.register(bus);
        BiblioItems.register(bus);
        BiblioMenus.register(bus);
        BiblioBlockEntities.register(bus);
        BiblioSounds.register(bus);
    }
}
