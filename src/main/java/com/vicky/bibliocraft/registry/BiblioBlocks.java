package com.vicky.bibliocraft.registry;

import com.vicky.bibliocraft.BiblioCraftLegacy;
import com.vicky.bibliocraft.block.BiblioContainerBlock;
import com.vicky.bibliocraft.block.BiblioLightBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BiblioBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BiblioCraftLegacy.MODID);

    public static final Map<String, RegistryObject<Block>> ALL = new LinkedHashMap<>();

    private static final String[] CONTAINER_IDS = {
            "bookcase", "case", "desk", "tool_rack", "sword_pedestal",
            "potion_shelf", "disc_rack", "framed_chest",
            "printing_press", "painting_press", "typesetting_table", "shelf",
            "table", "seat", "clock", "armor_stand", "fancy_workbench", "cookie_jar", "lamp", "lantern",
            "map_frame", "painting_frame", "typewriter", "label", "furniture_paneler", "clipboard"
    };

    static {
        for (String id : CONTAINER_IDS) {
            ALL.put(id, BLOCKS.register(id, () -> {
                BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .strength(1.5F);
                if (id.equals("lamp") || id.equals("lantern")) {
                    properties = properties.lightLevel(state ->
                            state.hasProperty(BiblioLightBlock.LIT) && state.getValue(BiblioLightBlock.LIT) ? 15 : 0);
                    return new BiblioLightBlock(properties);
                }
                return new BiblioContainerBlock(properties);
            }));
        }
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    private BiblioBlocks() {}
}
