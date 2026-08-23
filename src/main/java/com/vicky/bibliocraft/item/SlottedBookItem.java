package com.vicky.bibliocraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class SlottedBookItem extends BiblioLegacyItem {
    public static final String AUTHOR = "authorName";
    public static final String TITLE = "bookName";
    public static final String PUBLIC = "publicBook";
    public static final String CHASE_TEXT = "chaseText";
    public static final String SOURCE_BOOK = "sourceBook";

    public SlottedBookItem(Properties properties) {
        super("slotted_book", properties.stacksTo(1));
    }

    public static String title(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(TITLE) : "";
    }

    public static String author(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(AUTHOR) : "";
    }

    public static boolean isPublic(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(PUBLIC);
    }

    public static Component displayName(ItemStack stack) {
        String title = title(stack);
        return title.isEmpty()
                ? Component.translatable("item.bibliocraft.slotted_book")
                : Component.literal(title);
    }
}
