package com.vicky.bibliocraft.client;

import com.vicky.bibliocraft.BiblioCraftLegacy;
import com.vicky.bibliocraft.registry.BiblioBlockEntities;
import com.vicky.bibliocraft.registry.BiblioMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BiblioCraftLegacy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BiblioClient {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(BiblioMenus.BIBLIO_CONTAINER.get(), BiblioContainerScreen::new));
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BiblioBlockEntities.BIBLIO_CONTAINER.get(), BiblioBlockEntityRenderer::new);
    }
}
