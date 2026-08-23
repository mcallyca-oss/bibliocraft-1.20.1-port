package com.vicky.bibliocraft.blockentity;

import com.vicky.bibliocraft.item.PrintPlateItem;
import com.vicky.bibliocraft.item.SlottedBookItem;
import com.vicky.bibliocraft.registry.BiblioBlockEntities;
import com.vicky.bibliocraft.registry.BiblioItems;
import com.vicky.bibliocraft.registry.BiblioSounds;
import com.vicky.bibliocraft.menu.BiblioContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BiblioContainerBlockEntity extends BlockEntity implements MenuProvider, Container {
    private final ItemStack[] items = new ItemStack[27];
    private int pressProgress;

    private boolean clockTickSound = true;
    private boolean clockChimes;
    private boolean clockRedstone;
    private boolean clockPulse = true;
    private int clockActivity;
    private int clockPulseTicks;
    private final int[] clockRedstoneSettings = new int[48];

    private String typesetBookName = "Select a book";
    private String typesetAuthor = "";
    private boolean bookIsSaved;
    private boolean typesetPublic;
    private String chaseText = "";
    private boolean showBookName;
    private boolean showChaseText;
    private int requiredLevels;
    private boolean showLevels;
    private int typeCounter;

    public BiblioContainerBlockEntity(BlockPos pos, BlockState state) {
        super(BiblioBlockEntities.BIBLIO_CONTAINER.get(), pos, state);
        for (int i = 0; i < items.length; i++) items[i] = ItemStack.EMPTY;
    }

    public String legacyId() {
        return getBlockState().getBlock().builtInRegistryHolder().key().location().getPath();
    }

    public int logicalSize() {
        return switch (legacyId()) {
            case "bookcase" -> 16;
            case "case" -> 2;
            case "desk" -> 10;
            case "tool_rack" -> 4;
            case "sword_pedestal" -> 1;
            case "potion_shelf" -> 12;
            case "disc_rack" -> 9;
            case "framed_chest" -> 27;
            case "printing_press" -> 4;
            case "painting_press" -> 1;
            case "typesetting_table" -> 3;
            case "shelf" -> 16;
            case "table" -> 3;
            case "armor_stand" -> 6;
            case "fancy_workbench" -> 10;
            case "cookie_jar" -> 8;
            case "map_frame", "painting_frame", "clipboard" -> 1;
            case "typewriter" -> 2;
            case "label", "furniture_paneler" -> 3;
            case "seat", "clock", "lamp", "lantern" -> 0;
            default -> 9;
        };
    }

    public int pressProgress() { return pressProgress; }
    public void resetPress() { pressProgress = 0; }

    public String typesetBookName() { return typesetBookName; }
    public String typesetAuthor() { return typesetAuthor; }
    public boolean bookIsSaved() { return bookIsSaved; }
    public boolean typesetPublic() { return typesetPublic; }
    public String chaseText() { return chaseText; }
    public boolean showBookName() { return showBookName; }
    public boolean showChaseText() { return showChaseText; }
    public int requiredLevels() { return requiredLevels; }
    public boolean showLevels() { return showLevels; }
    public int typeCounter() { return typeCounter; }

    public boolean accepts(int slot, ItemStack stack) {
        if (stack.isEmpty() || slot < 0 || slot >= logicalSize()) return false;
        return switch (legacyId()) {
            case "bookcase" -> isBookLike(stack);
            case "desk" -> isBookLike(stack) || stack.is(Items.FILLED_MAP) || stack.is(Items.COMPASS);
            case "tool_rack" -> isToolLike(stack);
            case "sword_pedestal" -> stack.getItem() instanceof SwordItem;
            case "potion_shelf" -> stack.getItem() instanceof PotionItem;
            case "disc_rack" -> stack.getItem() instanceof RecordItem;
            case "printing_press" -> acceptsPrintPress(slot, stack);
            case "painting_press" -> stack.is(BiblioItems.PAINTING_CANVAS.get()) || stack.is(Items.PAPER) || stack.is(Items.FILLED_MAP);
            case "typesetting_table" -> slot == 0 ? isBookLike(stack) : (slot == 1 ? stack.is(Items.PAPER) : stack.is(Items.INK_SAC) || stack.is(Items.BLACK_DYE) || stack.is(BiblioItems.CHASE.get()));
            case "table" -> slot == 0 || isTableCover(stack);
            case "armor_stand" -> acceptsArmorStand(slot, stack);
            case "cookie_jar" -> stack.is(Items.COOKIE);
            case "fancy_workbench" -> slot < 9;
            case "map_frame" -> stack.is(Items.FILLED_MAP);
            case "painting_frame" -> stack.is(BiblioItems.PAINTING_CANVAS.get());
            case "typewriter" -> stack.is(Items.PAPER);
            case "label", "furniture_paneler", "clipboard" -> true;
            default -> true;
        };
    }

    private static boolean acceptsArmorStand(int slot, ItemStack stack) {
        EquipmentSlot equipmentSlot = LivingEntity.getEquipmentSlotForItem(stack);
        return switch (slot) {
            case 0 -> equipmentSlot == EquipmentSlot.HEAD;
            case 1 -> equipmentSlot == EquipmentSlot.CHEST;
            case 2 -> equipmentSlot == EquipmentSlot.LEGS;
            case 3 -> equipmentSlot == EquipmentSlot.FEET;
            case 4, 5 -> equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND;
            default -> false;
        };
    }

    private static boolean acceptsPrintPress(int slot, ItemStack stack) {
        return switch (slot) {
            case 0 -> stack.is(Items.INK_SAC) || stack.is(Items.BLACK_DYE);
            case 1 -> stack.is(BiblioItems.PRINT_PLATE.get()) || stack.is(BiblioItems.ENCHANTED_PLATE.get()) || stack.is(BiblioItems.ATLAS_PLATE.get()) || stack.is(BiblioItems.CHASE.get());
            case 2 -> stack.is(Items.WRITABLE_BOOK) || stack.is(Items.BOOK);
            default -> false;
        };
    }

    private static boolean isBookLike(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BookItem || item instanceof WritableBookItem || item instanceof WrittenBookItem || item instanceof EnchantedBookItem;
    }

    private static boolean isToolLike(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DiggerItem || item instanceof SwordItem || item instanceof ShearsItem || item instanceof FishingRodItem || item instanceof FlintAndSteelItem || item instanceof BrushItem;
    }

    private static boolean isTableCover(ItemStack stack) {
        if (stack.is(net.minecraft.tags.ItemTags.WOOL)) return true;
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock;
    }

    private static int slotLimitFor(String id) {
        return switch (id) {
            case "cookie_jar", "framed_chest" -> 64;
            default -> 1;
        };
    }

    public void selectTypesetBook(ItemStack source) {
        if (!"typesetting_table".equals(legacyId()) || source.isEmpty()) return;
        CompoundTag nbt = source.getTag();
        if (nbt != null) {
            if (nbt.contains("title")) typesetBookName = nbt.getString("title");
            if (nbt.contains("author")) typesetAuthor = nbt.getString("author");
        }
        bookIsSaved = source.is(Items.WRITTEN_BOOK) || source.is(Items.WRITABLE_BOOK);
        requiredLevels = Math.max(0, typesetBookName.length() / 20);
        showBookName = true;
        showLevels = requiredLevels > 0;
        setChanged();
    }

    public void togglePublic() {
        if (!"typesetting_table".equals(legacyId())) return;
        typesetPublic = !typesetPublic;
        setChanged();
    }

    public void setChaseText(String text) {
        if (!"typesetting_table".equals(legacyId())) return;
        String value = text == null ? "" : text;
        chaseText = value.substring(0, Math.min(64, value.length()));
        showChaseText = !chaseText.isEmpty();
        setChanged();
    }

    private void typesettingTick() {
        ItemStack book = getItem(0);
        ItemStack paper = getItem(1);
        ItemStack ink = getItem(2);
        if (book.isEmpty() || !bookIsSaved || !paper.is(Items.PAPER) || !(ink.is(Items.INK_SAC) || ink.is(Items.BLACK_DYE))) {
            typeCounter = 0;
            return;
        }
        if (typeCounter++ < 80) { setChanged(); return; }

        ItemStack result = new ItemStack(BiblioItems.SLOTTED_BOOK.get());
        CompoundTag tag = result.getOrCreateTag();
        tag.putString(SlottedBookItem.TITLE, typesetBookName);
        tag.putString(SlottedBookItem.AUTHOR, typesetAuthor);
        tag.putBoolean(SlottedBookItem.PUBLIC, typesetPublic);
        tag.putString(SlottedBookItem.CHASE_TEXT, chaseText);
        if (book.hasTag()) tag.put(SlottedBookItem.SOURCE_BOOK, book.getTag().copy());
        items[0] = result;
        paper.shrink(1);
        ink.shrink(1);
        items[1] = paper.isEmpty() ? ItemStack.EMPTY : paper;
        items[2] = ink.isEmpty() ? ItemStack.EMPTY : ink;
        typeCounter = 0;
        setChanged();
    }

    public boolean clockRedstoneActive() { return "clock".equals(legacyId()) && clockRedstone && clockPulseTicks > 0; }
    public boolean clockTickSound() { return clockTickSound; }
    public boolean clockChimes() { return clockChimes; }
    public boolean clockRedstoneEnabled() { return clockRedstone; }
    public boolean clockPulse() { return clockPulse; }
    public int clockPulseTicks() { return clockPulseTicks; }
    public int clockActivity() { return clockActivity; }
    public int clockSetting(int index) { return index >= 0 && index < 48 ? clockRedstoneSettings[index] : 0; }

    public boolean toggleClockTickSound() { if (!"clock".equals(legacyId())) return false; clockTickSound = !clockTickSound; setChanged(); return clockTickSound; }
    public boolean toggleClockChimes() { if (!"clock".equals(legacyId())) return false; clockChimes = !clockChimes; setChanged(); return clockChimes; }
    public boolean toggleClockRedstone() { if (!"clock".equals(legacyId())) return false; clockRedstone = !clockRedstone; setChanged(); return clockRedstone; }
    public boolean toggleClockPulse() { if (!"clock".equals(legacyId())) return false; clockPulse = !clockPulse; setChanged(); return clockPulse; }
    public boolean toggleClockSetting(int index) {
        if (!"clock".equals(legacyId()) || index < 0 || index >= 48) return false;
        clockRedstoneSettings[index] = clockRedstoneSettings[index] == 0 ? 1 : 0;
        setChanged();
        return clockRedstoneSettings[index] != 0;
    }

    private void clockTick() {
        if (level == null) return;
        long dayTime = Math.floorMod(level.getDayTime(), 24000L);
        int activity = (int) ((dayTime / 500L + 24L) % 48L);
        if (clockTickSound && level.getGameTime() % 20L == 0L) {
            boolean tick = ((level.getGameTime() / 20L) & 1L) == 0L;
            level.playSound(null, worldPosition, tick ? BiblioSounds.TICK.get() : BiblioSounds.TOCK.get(), SoundSource.BLOCKS, 0.28F, 1.0F);
        }
        if (activity != clockActivity) {
            clockActivity = activity;
            if (clockChimes && clockActivity % 2 == 0) level.playSound(null, worldPosition, BiblioSounds.CHIME.get(), SoundSource.BLOCKS, 0.65F, 1.0F);
            if (clockRedstone && clockRedstoneSettings[clockActivity] != 0) {
                clockPulseTicks = clockPulse ? 10 : 500;
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
            setChanged();
        }
        if (clockPulseTicks > 0 && --clockPulseTicks == 0) level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    private void seatTick() {
        if (level == null) return;
        AABB box = new AABB(worldPosition).inflate(0.75D);
        for (ArmorStand stand : level.getEntitiesOfClass(ArmorStand.class, box, a -> a.getPersistentData().getLong("BiblioSeatPos") == worldPosition.asLong())) {
            if (stand.getPassengers().isEmpty()) stand.discard();
        }
    }

    private TransientCraftingContainer fancyCraftingGrid() {
        AbstractContainerMenu dummy = new AbstractContainerMenu(null, -1) {
            @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
            @Override public boolean stillValid(Player player) { return false; }
        };
        TransientCraftingContainer grid = new TransientCraftingContainer(dummy, 3, 3);
        for (int i = 0; i < 9; i++) grid.setItem(i, items[i].copy());
        return grid;
    }

    private CraftingRecipe findFancyRecipe(TransientCraftingContainer grid) {
        if (level == null) return null;
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).orElse(null);
    }

    public void refreshFancyWorkbench() {
        if (level == null || level.isClientSide || !"fancy_workbench".equals(legacyId())) return;
        TransientCraftingContainer grid = fancyCraftingGrid();
        CraftingRecipe recipe = findFancyRecipe(grid);
        ItemStack result = recipe == null ? ItemStack.EMPTY : recipe.assemble(grid, level.registryAccess());
        if (!ItemStack.matches(items[9], result)) {
            items[9] = result.copy();
            setChanged();
        }
    }

    public void takeFancyWorkbenchResult(Player player, ItemStack crafted) {
        if (level == null || level.isClientSide || crafted.isEmpty() || !"fancy_workbench".equals(legacyId())) return;
        TransientCraftingContainer grid = fancyCraftingGrid();
        CraftingRecipe recipe = findFancyRecipe(grid);
        if (recipe == null) { refreshFancyWorkbench(); return; }
        ItemStack expected = recipe.assemble(grid, level.registryAccess());
        if (expected.isEmpty() || !ItemStack.isSameItemSameTags(expected, crafted)) { refreshFancyWorkbench(); return; }
        var remaining = recipe.getRemainingItems(grid);
        for (int i = 0; i < 9; i++) {
            if (!items[i].isEmpty()) items[i].shrink(1);
            ItemStack remainder = remaining.get(i);
            if (!remainder.isEmpty()) {
                if (items[i].isEmpty()) items[i] = remainder.copy();
                else if (!player.getInventory().add(remainder.copy())) player.drop(remainder.copy(), false);
            }
        }
        items[9] = ItemStack.EMPTY;
        setChanged();
        refreshFancyWorkbench();
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;
        switch (legacyId()) {
            case "typesetting_table" -> typesettingTick();
            case "clock" -> clockTick();
            case "seat" -> seatTick();
            case "fancy_workbench" -> { if (level.getGameTime() % 5L == 0L) refreshFancyWorkbench(); }
            case "printing_press" -> printingPressTick();
        }
    }

    private void printingPressTick() {
        ItemStack ink = getItem(0), plate = getItem(1), book = getItem(2), output = getItem(3);
        if (!output.isEmpty() || ink.isEmpty() || plate.isEmpty() || book.isEmpty() || !PrintPlateItem.hasBook(plate)
                || !(ink.is(Items.INK_SAC) || ink.is(Items.BLACK_DYE)) || !(book.is(Items.WRITABLE_BOOK) || book.is(Items.BOOK))) {
            pressProgress = 0;
            return;
        }
        if (pressProgress < 100) { pressProgress++; setChanged(); return; }
        CompoundTag plateBook = PrintPlateItem.getBookTag(plate);
        if (plateBook == null) { pressProgress = 0; return; }
        ItemStack result = new ItemStack(Items.WRITTEN_BOOK);
        result.setTag(plateBook.copy());
        items[3] = result;
        items[2].shrink(1);
        items[0].shrink(1);
        pressProgress = 0;
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public Component getDisplayName() { return Component.translatable(getBlockState().getBlock().getDescriptionId()); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new BiblioContainerMenu(id, inv, this); }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (int i = 0; i < logicalSize(); i++) {
            if (!items[i].isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) i);
                items[i].save(entry);
                list.add(entry);
            }
        }
        tag.put("Items", list);
        tag.putInt("PressProgress", pressProgress);
        tag.putString("TypesetBookName", typesetBookName);
        tag.putString("TypesetAuthor", typesetAuthor);
        tag.putBoolean("BookIsSaved", bookIsSaved);
        tag.putBoolean("TypesetPublic", typesetPublic);
        tag.putString("ChaseText", chaseText);
        tag.putBoolean("ShowBookName", showBookName);
        tag.putBoolean("ShowChaseText", showChaseText);
        tag.putInt("RequiredLevels", requiredLevels);
        tag.putBoolean("ShowLevels", showLevels);
        tag.putInt("TypeCounter", typeCounter);
        tag.putBoolean("ClockTickSound", clockTickSound);
        tag.putBoolean("ClockChimes", clockChimes);
        tag.putBoolean("ClockRedstone", clockRedstone);
        tag.putBoolean("ClockPulse", clockPulse);
        tag.putInt("ClockActivity", clockActivity);
        tag.putInt("ClockPulseTicks", clockPulseTicks);
        tag.putIntArray("ClockRedstoneSettings", clockRedstoneSettings);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < items.length; i++) items[i] = ItemStack.EMPTY;
        ListTag list = tag.getList("Items", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot >= 0 && slot < logicalSize()) items[slot] = ItemStack.of(entry);
        }
        pressProgress = tag.getInt("PressProgress");
        typesetBookName = tag.getString("TypesetBookName");
        if (typesetBookName.isEmpty()) typesetBookName = "Select a book";
        typesetAuthor = tag.getString("TypesetAuthor");
        bookIsSaved = tag.getBoolean("BookIsSaved");
        typesetPublic = tag.getBoolean("TypesetPublic");
        chaseText = tag.getString("ChaseText");
        showBookName = tag.getBoolean("ShowBookName");
        showChaseText = tag.getBoolean("ShowChaseText");
        requiredLevels = tag.getInt("RequiredLevels");
        showLevels = tag.getBoolean("ShowLevels");
        typeCounter = tag.getInt("TypeCounter");
        if (tag.contains("ClockTickSound")) clockTickSound = tag.getBoolean("ClockTickSound");
        clockChimes = tag.getBoolean("ClockChimes");
        clockRedstone = tag.getBoolean("ClockRedstone");
        if (tag.contains("ClockPulse")) clockPulse = tag.getBoolean("ClockPulse");
        clockActivity = tag.getInt("ClockActivity");
        clockPulseTicks = tag.getInt("ClockPulseTicks");
        int[] saved = tag.getIntArray("ClockRedstoneSettings");
        System.arraycopy(saved, 0, clockRedstoneSettings, 0, Math.min(saved.length, clockRedstoneSettings.length));
    }

    @Override public int getContainerSize() { return logicalSize(); }
    @Override public boolean isEmpty() { for (int i = 0; i < logicalSize(); i++) if (!items[i].isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < logicalSize() ? items[slot] : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= logicalSize()) return ItemStack.EMPTY;
        ItemStack result = items[slot].split(amount);
        setChanged();
        if ("fancy_workbench".equals(legacyId()) && slot < 9) refreshFancyWorkbench();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= logicalSize()) return ItemStack.EMPTY;
        ItemStack result = items[slot];
        items[slot] = ItemStack.EMPTY;
        setChanged();
        return result;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= logicalSize()) return;
        if (stack.isEmpty()) items[slot] = ItemStack.EMPTY;
        else if (accepts(slot, stack)) {
            items[slot] = stack.copy();
            items[slot].setCount(Math.min(items[slot].getCount(), slotLimitFor(legacyId())));
        }
        setChanged();
        if ("fancy_workbench".equals(legacyId()) && slot < 9) refreshFancyWorkbench();
    }
    @Override public int getMaxStackSize() { return 64; }
    @Override public void clearContent() { for (int i = 0; i < logicalSize(); i++) items[i] = ItemStack.EMPTY; setChanged(); }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64;
    }
}
