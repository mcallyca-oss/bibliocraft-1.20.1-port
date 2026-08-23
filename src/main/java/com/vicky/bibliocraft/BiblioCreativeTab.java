package com.vicky.bibliocraft;

import com.vicky.bibliocraft.registry.BiblioItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BiblioCraftLegacy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BiblioCreativeTab {
    @SubscribeEvent
    public static void fill(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.FUNCTIONAL_BLOCKS
                && event.getTabKey() != CreativeModeTabs.TOOLS_AND_UTILITIES) {
            return;
        }

        for (var blockItem : BiblioItems.BLOCK_ITEMS.values()) {
            event.accept(blockItem);
        }

        event.accept(BiblioItems.PRINT_PLATE);
        event.accept(BiblioItems.CHASE);
        event.accept(BiblioItems.ENCHANTED_PLATE);
        event.accept(BiblioItems.PAINTING_CANVAS);
        event.accept(BiblioItems.ATLAS_PLATE);
        event.accept(BiblioItems.TYPESET_KEY);
        event.accept(BiblioItems.SLOTTED_BOOK);
    }

    private BiblioCreativeTab() {}
}
