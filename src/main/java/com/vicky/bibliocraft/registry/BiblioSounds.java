package com.vicky.bibliocraft.registry;

import com.vicky.bibliocraft.BiblioCraftLegacy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BiblioSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BiblioCraftLegacy.MODID);

    public static final RegistryObject<SoundEvent> TICK = register("tick");
    public static final RegistryObject<SoundEvent> TOCK = register("tock");
    public static final RegistryObject<SoundEvent> CHIME = register("chime");

    private static RegistryObject<SoundEvent> register(String id) {
        ResourceLocation location = new ResourceLocation(BiblioCraftLegacy.MODID, id);
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(location));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private BiblioSounds() {}
}
