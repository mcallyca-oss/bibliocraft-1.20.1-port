package com.vicky.bibliocraft.item;

import net.minecraft.world.item.Item;

public class BiblioLegacyItem extends Item {
    private final String legacyId;

    public BiblioLegacyItem(String legacyId, Properties properties) {
        super(properties);
        this.legacyId = legacyId;
    }

    public String legacyId() {
        return legacyId;
    }
}
