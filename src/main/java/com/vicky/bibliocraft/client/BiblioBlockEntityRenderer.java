package com.vicky.bibliocraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vicky.bibliocraft.block.BiblioContainerBlock;
import com.vicky.bibliocraft.blockentity.BiblioContainerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class BiblioBlockEntityRenderer implements BlockEntityRenderer<BiblioContainerBlockEntity> {
    private final ItemRenderer itemRenderer;

    public BiblioBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BiblioContainerBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        if (be.getBlockState().hasProperty(BiblioContainerBlock.FACING)) {
            pose.mulPose(Axis.YP.rotationDegrees(-be.getBlockState().getValue(BiblioContainerBlock.FACING).toYRot()));
        }
        pose.translate(-0.5D, 0.0D, -0.5D);

        switch (be.legacyId()) {
            case "table" -> renderTable(be, pose, buffer, packedLight, packedOverlay);
            case "shelf" -> renderShelf(be, pose, buffer, packedLight, packedOverlay);
            case "potion_shelf" -> renderPotionShelf(be, pose, buffer, packedLight, packedOverlay);
            case "armor_stand" -> renderArmorStand(be, pose, buffer, packedLight, packedOverlay);
            case "cookie_jar" -> renderCookieJar(be, pose, buffer, packedLight, packedOverlay);
            case "fancy_workbench" -> renderWorkbench(be, pose, buffer, packedLight, packedOverlay);
            case "map_frame" -> renderMapFrame(be, pose, buffer, packedLight, packedOverlay);
            case "painting_frame" -> renderPaintingFrame(be, pose, buffer, packedLight, packedOverlay);
            case "typewriter" -> renderTypewriter(be, pose, buffer, packedLight, packedOverlay);
            case "label" -> renderLabel(be, pose, buffer, packedLight, packedOverlay);
            case "clipboard" -> renderClipboard(be, pose, buffer, packedLight, packedOverlay);
        }
        pose.popPose();
    }

    private void renderTable(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        ItemStack display = be.getItem(0);
        if (display.isEmpty()) return;
        // Coordinates/scale ported from the original BiblioCraft 2.4.6 table renderer.
        renderItem(display, pose, buffer, light, overlay, .5, 1.14, .5, 0, 0, .75f);
    }

    private void renderShelf(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        // Exact four visible positions from TileEntityShelfRenderer (1.12.2).
        renderItem(be.getItem(0), pose, buffer, light, overlay, .25, .66, .25, 0, 0, .60f);
        renderItem(be.getItem(1), pose, buffer, light, overlay, .75, .66, .25, 0, 0, .60f);
        renderItem(be.getItem(2), pose, buffer, light, overlay, .25, .17, .25, 0, 0, .60f);
        renderItem(be.getItem(3), pose, buffer, light, overlay, .75, .17, .25, 0, 0, .60f);
    }

    private void renderPotionShelf(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        // Exact 4 x 3 slot coordinates from TileEntityPotionShelfRenderer (1.12.2).
        double[] xs = {.15, .38, .61, .84};
        double[] ys = {.81, .48, .13};
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int slot = row * 4 + col;
                double z = (slot == 1 || slot == 3 || slot == 4 || slot == 6 || slot == 9 || slot == 11) ? .26 : .25;
                renderItem(be.getItem(slot), pose, buffer, light, overlay, xs[col], ys[row], z, 0, 0, .42f);
            }
        }
    }

    private void renderArmorStand(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        renderItem(be.getItem(0), pose, buffer, light, overlay, .5, 1.58, .5, 0, 180, .52f);
        renderItem(be.getItem(1), pose, buffer, light, overlay, .5, 1.17, .5, 0, 180, .48f);
        renderItem(be.getItem(2), pose, buffer, light, overlay, .5, .82, .5, 0, 180, .44f);
        renderItem(be.getItem(3), pose, buffer, light, overlay, .5, .34, .5, 0, 180, .40f);
        renderItem(be.getItem(4), pose, buffer, light, overlay, .16, 1.02, .5, 0, 90, .48f);
        renderItem(be.getItem(5), pose, buffer, light, overlay, .84, 1.02, .5, 0, -90, .48f);
    }

    private void renderCookieJar(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        int visible = 0;
        for (int i=0;i<8;i++) if (!be.getItem(i).isEmpty()) visible++;
        for (int i=0;i<visible;i++) {
            double x=.38 + (i%2)*.24;
            double z=.38 + ((i/2)%2)*.24;
            double y=.18 + (i/4)*.13;
            ItemStack cookie = be.getItem(i).copy();
            cookie.setCount(1);
            renderItem(cookie, pose, buffer, light, overlay, x, y, z, 90, i*37f, .28f);
        }
    }

    private void renderWorkbench(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        ItemStack result = be.getItem(9);
        if (!result.isEmpty()) renderItem(result, pose, buffer, light, overlay, .72, .89, .52, 90, 0, .34f);
    }

    private void renderMapFrame(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        ItemStack map = be.getItem(0);
        if (!map.isEmpty()) renderItem(map, pose, buffer, light, overlay, .505, .55, .08, 0, 180, .78f);
    }

    private void renderPaintingFrame(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        ItemStack canvas = be.getItem(0);
        if (!canvas.isEmpty()) renderItem(canvas, pose, buffer, light, overlay, .505, .55, .075, 0, 180, .72f);
    }

    private void renderTypewriter(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        ItemStack paper = be.getItem(0);
        if (!paper.isEmpty()) renderItem(paper, pose, buffer, light, overlay, .50, .76, .48, 78, 0, .34f);
    }

    private void renderLabel(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        double[] xs = {.29, .50, .71};
        for (int i = 0; i < 3; i++) {
            ItemStack stack = be.getItem(i);
            if (!stack.isEmpty()) renderItem(stack, pose, buffer, light, overlay, xs[i], .43, .08, 0, 180, .28f);
        }
    }

    private void renderClipboard(BiblioContainerBlockEntity be, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        ItemStack page = be.getItem(0);
        if (!page.isEmpty()) renderItem(page, pose, buffer, light, overlay, .505, .50, .075, 0, 180, .48f);
    }

    private void renderItem(ItemStack stack, PoseStack pose, MultiBufferSource buffer, int light, int overlay,
                            double x, double y, double z, float rotX, float rotY, float scale) {
        if (stack.isEmpty()) return;
        pose.pushPose();
        pose.translate(x,y,z);
        pose.mulPose(Axis.YP.rotationDegrees(rotY));
        pose.mulPose(Axis.XP.rotationDegrees(rotX));
        pose.scale(scale,scale,scale);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffer, null, 0);
        pose.popPose();
    }
}
