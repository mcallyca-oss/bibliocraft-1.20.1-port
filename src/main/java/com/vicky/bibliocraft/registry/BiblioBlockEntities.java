package com.vicky.bibliocraft.registry;

import com.vicky.bibliocraft.BiblioCraftLegacy;
import com.vicky.bibliocraft.block.BiblioContainerBlock;
import com.vicky.bibliocraft.blockentity.BiblioContainerBlockEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;

public final class BiblioBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BiblioCraftLegacy.MODID);

    public static final RegistryObject<BlockEntityType<BiblioContainerBlockEntity>> BIBLIO_CONTAINER =
            BLOCK_ENTITIES.register("biblio_container", () -> {
                List<BiblioContainerBlock> blocks = new ArrayList<>();
                for (String id : BiblioBlocks.ALL.keySet()) {
                    blocks.add((BiblioContainerBlock) BiblioBlocks.ALL.get(id).get());
                }
                return BlockEntityType.Builder.of(
                        BiblioContainerBlockEntity::new,
                        blocks.toArray(new BiblioContainerBlock[0])
                ).build(null);
            });

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }

    private BiblioBlockEntities() {}
}
