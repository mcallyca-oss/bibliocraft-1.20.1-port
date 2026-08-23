package com.vicky.bibliocraft.menu;

import com.vicky.bibliocraft.blockentity.BiblioContainerBlockEntity;
import com.vicky.bibliocraft.registry.BiblioMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BiblioContainerMenu extends AbstractContainerMenu {
    private final BiblioContainerBlockEntity blockEntity;
    private final Container container;
    private final ContainerData clockData;

    public BiblioContainerMenu(int id, Inventory inv, BiblioContainerBlockEntity be) {
        super(BiblioMenus.BIBLIO_CONTAINER.get(), id);
        this.blockEntity = be;
        this.container = be;

        final int[] syncedClock = new int[54];
        this.clockData = new ContainerData() {
            @Override public int get(int index) {
                if (be.getLevel() != null && be.getLevel().isClientSide) {
                    return index >= 0 && index < syncedClock.length ? syncedClock[index] : 0;
                }
                return switch (index) {
                    case 0 -> be.clockTickSound() ? 1 : 0;
                    case 1 -> be.clockChimes() ? 1 : 0;
                    case 2 -> be.clockRedstoneEnabled() ? 1 : 0;
                    case 3 -> be.clockPulse() ? 1 : 0;
                    case 4 -> be.clockActivity();
                    case 5 -> be.clockPulseTicks();
                    default -> index >= 6 && index < 54 ? be.clockSetting(index - 6) : 0;
                };
            }
            @Override public void set(int index, int value) {
                if (index >= 0 && index < syncedClock.length) syncedClock[index] = value;
            }
            @Override public int getCount() { return 54; }
        };
        addDataSlots(clockData);

        int n = be.getContainerSize();
        if ("fancy_workbench".equals(be.legacyId())) {
            for (int i=0;i<9;i++) {
                final int slot=i;
                addSlot(new Slot(container, slot, 30+(i%3)*18, 22+(i/3)*18) {
                    @Override public boolean mayPlace(ItemStack stack) { return blockEntity.accepts(slot, stack); }
                });
            }
            addSlot(new Slot(container, 9, 124, 40) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public void onTake(Player player, ItemStack stack) {
                    super.onTake(player, stack);
                    blockEntity.takeFancyWorkbenchResult(player, stack);
                }
            });
        } else {
            for (int i=0;i<n;i++) {
                final int slot=i;
                addSlot(new Slot(container, slot, 8+(i%9)*18, 18+(i/9)*18) {
                    @Override public boolean mayPlace(ItemStack stack) {
                        return blockEntity.accepts(slot, stack);
                    }
                });
            }
        }

        if (!isClock()) {
            int startY = "fancy_workbench".equals(be.legacyId()) ? 86 : 20 + ((n+8)/9)*18 + 10;
            for(int row=0;row<3;row++)
                for(int col=0;col<9;col++)
                    addSlot(new Slot(inv,col+row*9+9,8+col*18,startY+row*18));

            int hotY=startY+58;
            for(int col=0;col<9;col++)
                addSlot(new Slot(inv,col,8+col*18,hotY));
        }
    }

    public BiblioContainerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (BiblioContainerBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public BiblioContainerBlockEntity getBlockEntity(){ return blockEntity; }
    public boolean isClock(){ return "clock".equals(blockEntity.legacyId()); }

    public boolean clockTickSound(){ return clockData.get(0) != 0; }
    public boolean clockChimes(){ return clockData.get(1) != 0; }
    public boolean clockRedstone(){ return clockData.get(2) != 0; }
    public boolean clockPulse(){ return clockData.get(3) != 0; }
    public int clockActivity(){ return clockData.get(4); }
    public int clockSetting(int slot){ return slot >= 0 && slot < 48 ? clockData.get(6 + slot) : 0; }

    public void selectTypesetBook() {
        if ("typesetting_table".equals(blockEntity.legacyId())) {
            blockEntity.selectTypesetBook(blockEntity.getItem(0));
        }
    }

    public void toggleTypesetPublic() {
        blockEntity.togglePublic();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!isClock()) return false;
        return switch (buttonId) {
            case 0 -> { blockEntity.toggleClockTickSound(); yield true; }
            case 1 -> { blockEntity.toggleClockChimes(); yield true; }
            case 2 -> { blockEntity.toggleClockRedstone(); yield true; }
            case 3 -> { blockEntity.toggleClockPulse(); yield true; }
            default -> {
                if (buttonId >= 100 && buttonId < 148) {
                    blockEntity.toggleClockSetting(buttonId - 100);
                    yield true;
                }
                yield false;
            }
        };
    }

    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0 || index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if(!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack src=slot.getItem();
        ItemStack copy=src.copy();

        int machine=blockEntity.getContainerSize();
        if(index<machine){
            if ("fancy_workbench".equals(blockEntity.legacyId()) && index == 9) {
                if(!moveItemStackTo(src,machine,slots.size(),true)) return ItemStack.EMPTY;
                blockEntity.takeFancyWorkbenchResult(player, copy);
            } else if(!moveItemStackTo(src,machine,slots.size(),true)) return ItemStack.EMPTY;
        }else{
            boolean moved=false;
            for(int i=0;i<machine && !moved;i++){
                if(blockEntity.accepts(i,src) && moveItemStackTo(src,i,i+1,false)) moved=true;
            }
            if(!moved) return ItemStack.EMPTY;
        }
        if(src.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public boolean stillValid(Player player){ return container.stillValid(player); }
}
