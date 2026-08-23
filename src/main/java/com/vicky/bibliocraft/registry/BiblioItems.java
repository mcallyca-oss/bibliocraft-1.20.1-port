package com.vicky.bibliocraft.registry;

import com.vicky.bibliocraft.BiblioCraftLegacy;
import com.vicky.bibliocraft.item.BiblioLegacyItem;
import com.vicky.bibliocraft.item.PrintPlateItem;
import com.vicky.bibliocraft.item.SlottedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraftforge.eventbus.api.IEventBus;

public final class BiblioItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BiblioCraftLegacy.MODID);

    public static final Map<String, RegistryObject<Item>> BLOCK_ITEMS = new LinkedHashMap<>();

    static {
        for (var entry : BiblioBlocks.ALL.entrySet()) {
            String id = entry.getKey();
            BLOCK_ITEMS.put(id, ITEMS.register(id, () -> new BlockItem(
                    entry.getValue().get(),
                    new Item.Properties()
            )));
        }
    }

    public static final RegistryObject<Item> PRINT_PLATE =
            ITEMS.register("print_plate", () -> new PrintPlateItem(new Item.Properties()));

    public static final RegistryObject<Item> CHASE =
            ITEMS.register("chase", () -> new BiblioLegacyItem("chase",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ENCHANTED_PLATE =
            ITEMS.register("enchanted_plate", () -> new BiblioLegacyItem("enchanted_plate",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PAINTING_CANVAS =
            ITEMS.register("painting_canvas", () -> new BiblioLegacyItem("painting_canvas",
                    new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> ATLAS_PLATE =
            ITEMS.register("atlas_plate", () -> new BiblioLegacyItem("atlas_plate",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TYPESET_KEY =
            ITEMS.register("typeset_key", () -> new BiblioLegacyItem("typeset_key",
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SLOTTED_BOOK =
            ITEMS.register("slotted_book", () -> new SlottedBookItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private BiblioItems() {}
}
