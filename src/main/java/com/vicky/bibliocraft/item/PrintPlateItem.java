package com.vicky.bibliocraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;

public final class PrintPlateItem extends BiblioLegacyItem {
    public static final String BOOK_TAG = "BiblioBook";

    public PrintPlateItem(Properties properties) {
        super("print_plate", properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        InteractionHand other = ctx.getHand() == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack source = player.getItemInHand(other);

        if (source.is(Items.WRITTEN_BOOK) && source.hasTag()) {
            ctx.getItemInHand().getOrCreateTag().put(BOOK_TAG, source.getTag().copy());
            player.displayClientMessage(
                    Component.literal("Print plate prepared").withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public static boolean hasBook(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(BOOK_TAG);
    }

    public static CompoundTag getBookTag(ItemStack stack) {
        return hasBook(stack) ? stack.getTag().getCompound(BOOK_TAG) : null;
    }
}
