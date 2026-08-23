package com.vicky.bibliocraft.client;

import com.vicky.bibliocraft.menu.BiblioContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BiblioContainerScreen extends AbstractContainerScreen<BiblioContainerMenu> {
    private static final int CLOCK_CELL_W = 11;
    private static final int CLOCK_CELL_H = 10;

    public BiblioContainerScreen(BiblioContainerMenu menu, Inventory inv, Component title) {
        super(menu,inv,title);
        imageWidth = menu.isClock() ? 220 : 176;
        imageHeight = menu.isClock() ? 178 : 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics g,float partialTick,int mx,int my){
        int x=(width-imageWidth)/2;
        int y=(height-imageHeight)/2;
        g.fill(x,y,x+imageWidth,y+imageHeight,0xFFC6C6C6);
        g.fill(x+4,y+14,x+imageWidth-4,y+imageHeight-5,0xFF8B8B8B);

        String id=menu.getBlockEntity().legacyId();
        Component label=Component.literal(switch(id){
            case "printing_press" -> "Printing Press";
            case "painting_press" -> "Painting Press";
            case "typesetting_table" -> "Typesetting Table";
            case "bookcase" -> "Bookcase";
            case "desk" -> "Writing Desk";
            case "clock" -> "BiblioCraft Clock";
            case "table" -> "Table";
            case "armor_stand" -> "Armor Stand";
            case "fancy_workbench" -> "Fancy Workbench";
            case "cookie_jar" -> "Cookie Jar";
            case "lamp" -> "Lamp";
            case "lantern" -> "Lantern";
            default -> "BiblioCraft";
        });
        g.drawString(font,label,x+8,y+5,0x404040,false);

        if (menu.isClock()) {
            renderClock(g, x, y, mx, my);
            return;
        }

        if(id.equals("printing_press")){
            int p=menu.getBlockEntity().pressProgress();
            g.drawString(font,Component.literal("Press: "+p+"%"),x+8,y+68,0x404040,false);
        }

        if(id.equals("typesetting_table")){
            var be = menu.getBlockEntity();
            g.drawString(font, Component.literal(
                    "Book: " + be.typesetBookName()), x+8, y+56, 0x404040, false);
            g.drawString(font, Component.literal(
                    "Author: " + (be.typesetAuthor().isEmpty() ? "-" : be.typesetAuthor())),
                    x+8, y+70, 0x404040, false);
            g.drawString(font, Component.literal(
                    "Public: " + (be.typesetPublic() ? "yes" : "no")),
                    x+8, y+84, 0x404040, false);
            g.drawString(font, Component.literal(
                    "Levels: " + be.requiredLevels()),
                    x+8, y+98, 0x404040, false);
        }
    }

    private void renderClock(GuiGraphics g, int x, int y, int mx, int my) {
        drawToggle(g, x+8, y+22, 94, 16, "Tick / Tock", menu.clockTickSound());
        drawToggle(g, x+118, y+22, 94, 16, "Hourly Chime", menu.clockChimes());
        drawToggle(g, x+8, y+42, 94, 16, "Redstone", menu.clockRedstone());
        drawToggle(g, x+118, y+42, 94, 16, "Pulse Mode", menu.clockPulse());

        g.drawString(font, Component.literal("24-hour schedule (30 min cells)"), x+8, y+66, 0xFFFFFF, false);
        g.drawString(font, Component.literal("Current: " + formatHalfHour(menu.clockActivity())), x+8, y+79, 0xFFFFFF, false);

        int gx=x+8, gy=y+96;
        for(int i=0;i<48;i++){
            int col=i%16;
            int row=i/16;
            int cx=gx+col*(CLOCK_CELL_W+1);
            int cy=gy+row*(CLOCK_CELL_H+2);
            boolean enabled=menu.clockSetting(i)!=0;
            boolean current=menu.clockActivity()==i;
            int color=enabled ? 0xFF4A7A4A : 0xFF555555;
            if(current) color=enabled ? 0xFF78B878 : 0xFF8A6A3A;
            g.fill(cx,cy,cx+CLOCK_CELL_W,cy+CLOCK_CELL_H,color);
            if(current){
                g.fill(cx,cy,cx+CLOCK_CELL_W,cy+1,0xFFFFFFFF);
                g.fill(cx,cy+CLOCK_CELL_H-1,cx+CLOCK_CELL_W,cy+CLOCK_CELL_H,0xFFFFFFFF);
            }
            if(mx>=cx && mx<cx+CLOCK_CELL_W && my>=cy && my<cy+CLOCK_CELL_H){
                g.renderTooltip(font, Component.literal(formatHalfHour(i) + (enabled ? " - ON" : " - off")), mx, my);
            }
        }
        g.drawString(font, Component.literal("Click cells to schedule a redstone pulse."), x+8, y+137, 0xE0E0E0, false);
    }

    private void drawToggle(GuiGraphics g,int x,int y,int w,int h,String text,boolean on){
        g.fill(x,y,x+w,y+h,on?0xFF4A7A4A:0xFF555555);
        g.drawCenteredString(font, Component.literal(text+": "+(on?"ON":"off")), x+w/2, y+4, 0xFFFFFF);
    }

    private static String formatHalfHour(int cell){
        int normalized=Math.floorMod(cell,48);
        int hour=(normalized/2+12)%24;
        int minute=(normalized&1)*30;
        return String.format("%02d:%02d",hour,minute);
    }

    @Override
    public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(menu.isClock() && button==0){
            int x=(width-imageWidth)/2;
            int y=(height-imageHeight)/2;
            int id=-1;
            if(in(mouseX,mouseY,x+8,y+22,94,16)) id=0;
            else if(in(mouseX,mouseY,x+118,y+22,94,16)) id=1;
            else if(in(mouseX,mouseY,x+8,y+42,94,16)) id=2;
            else if(in(mouseX,mouseY,x+118,y+42,94,16)) id=3;
            else {
                int gx=x+8,gy=y+96;
                for(int i=0;i<48;i++){
                    int cx=gx+(i%16)*(CLOCK_CELL_W+1);
                    int cy=gy+(i/16)*(CLOCK_CELL_H+2);
                    if(in(mouseX,mouseY,cx,cy,CLOCK_CELL_W,CLOCK_CELL_H)){ id=100+i; break; }
                }
            }
            if(id>=0 && minecraft!=null && minecraft.gameMode!=null){
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);
                return true;
            }
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }

    private static boolean in(double mx,double my,int x,int y,int w,int h){
        return mx>=x && mx<x+w && my>=y && my<y+h;
    }
}
