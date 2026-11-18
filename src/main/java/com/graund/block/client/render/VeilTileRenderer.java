package com.graund.block.client.render;

import com.graund.block.tile.FaceMode;
import com.graund.block.tile.TileEntityVeil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/** Draws opaque treatments and real atlas-textured glass faces. */
public class VeilTileRenderer extends TileEntitySpecialRenderer<TileEntityVeil> {
    private static final float[] OPAQUE = {0.13F, 0.16F, 0.20F, 1.0F};
    private static final float[] WINDOW = {0.70F, 0.90F, 1.0F, 0.30F};

    @Override
    public void render(TileEntityVeil tile, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (tile == null || tile.getWorld() == null || tile.isInvalid()) return;
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) return;

        Vec3d eye = camera.getPositionEyes(partialTicks);
        double dx = eye.x - (tile.getPos().getX() + 0.5D);
        double dy = eye.y - (tile.getPos().getY() + 0.5D);
        double dz = eye.z - (tile.getPos().getZ() + 0.5D);

        List<FaceDraw> opaque = new ArrayList<>();
        List<FaceDraw> glass = new ArrayList<>();
        List<FaceDraw> windows = new ArrayList<>();
        for (EnumFacing face : EnumFacing.values()) {
            FaceMode mode = tile.getMode(face);
            if (mode == FaceMode.MASK) continue;
            if (mode == FaceMode.OPAQUE) {
                opaque.add(new FaceDraw(face, OPAQUE));
            } else if (mode == FaceMode.GLASS) {
                glass.add(new FaceDraw(face, null));
            } else if (mode == FaceMode.ONE_WAY) {
                double side = dx * face.getDirectionVec().getX()
                        + dy * face.getDirectionVec().getY()
                        + dz * face.getDirectionVec().getZ();
                if (side > 0.0001D) windows.add(new FaceDraw(face, WINDOW));
                else opaque.add(new FaceDraw(face, OPAQUE));
            }
        }
        if (opaque.isEmpty() && glass.isEmpty() && windows.isEmpty()) return;

        boolean oldBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean oldTexture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean oldCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean oldLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );

            if (!opaque.isEmpty()) {
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(true);
                drawColored(opaque);
            }
            if (!windows.isEmpty()) {
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                drawColored(windows);
            }
            if (!glass.isEmpty()) {
                GlStateManager.enableTexture2D();
                GlStateManager.depthMask(false);
                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                        .getAtlasSprite("minecraft:blocks/glass");
                drawTextured(glass, sprite);
            }
        } finally {
            GlStateManager.depthMask(true);
            if (oldBlend) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
            if (oldTexture) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
            if (oldCull) GlStateManager.enableCull(); else GlStateManager.disableCull();
            if (oldLighting) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
            GlStateManager.popMatrix();
        }
    }

    private static void drawColored(List<FaceDraw> faces) {
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (FaceDraw f : faces) addColoredFace(b, f.face, f.color);
        Tessellator.getInstance().draw();
    }

    private static void drawTextured(List<FaceDraw> faces, TextureAtlasSprite sprite) {
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        float minU = sprite.getMinU(), maxU = sprite.getMaxU();
        float minV = sprite.getMinV(), maxV = sprite.getMaxV();
        for (FaceDraw f : faces) addTexturedFace(b, f.face, minU, minV, maxU, maxV);
        Tessellator.getInstance().draw();
    }

    private static void addColoredFace(BufferBuilder b, EnumFacing face, float[] c) {
        double e = 0.0015D;
        switch (face) {
            case DOWN:
                cv(b,0,-e,1,c); cv(b,0,-e,0,c); cv(b,1,-e,0,c); cv(b,1,-e,1,c); break;
            case UP:
                cv(b,0,1+e,0,c); cv(b,0,1+e,1,c); cv(b,1,1+e,1,c); cv(b,1,1+e,0,c); break;
            case NORTH:
                cv(b,1,0,-e,c); cv(b,0,0,-e,c); cv(b,0,1,-e,c); cv(b,1,1,-e,c); break;
            case SOUTH:
                cv(b,0,0,1+e,c); cv(b,1,0,1+e,c); cv(b,1,1,1+e,c); cv(b,0,1,1+e,c); break;
            case WEST:
                cv(b,-e,0,0,c); cv(b,-e,0,1,c); cv(b,-e,1,1,c); cv(b,-e,1,0,c); break;
            case EAST:
                cv(b,1+e,0,1,c); cv(b,1+e,0,0,c); cv(b,1+e,1,0,c); cv(b,1+e,1,1,c); break;
            default: break;
        }
    }

    private static void addTexturedFace(BufferBuilder b, EnumFacing face,
                                        float minU, float minV, float maxU, float maxV) {
        double e = 0.0015D;
        switch (face) {
            case DOWN:
                tv(b,0,-e,1,minU,maxV); tv(b,0,-e,0,minU,minV);
                tv(b,1,-e,0,maxU,minV); tv(b,1,-e,1,maxU,maxV); break;
            case UP:
                tv(b,0,1+e,0,minU,minV); tv(b,0,1+e,1,minU,maxV);
                tv(b,1,1+e,1,maxU,maxV); tv(b,1,1+e,0,maxU,minV); break;
            case NORTH:
                tv(b,1,0,-e,maxU,maxV); tv(b,0,0,-e,minU,maxV);
                tv(b,0,1,-e,minU,minV); tv(b,1,1,-e,maxU,minV); break;
            case SOUTH:
                tv(b,0,0,1+e,minU,maxV); tv(b,1,0,1+e,maxU,maxV);
                tv(b,1,1,1+e,maxU,minV); tv(b,0,1,1+e,minU,minV); break;
            case WEST:
                tv(b,-e,0,0,minU,maxV); tv(b,-e,0,1,maxU,maxV);
                tv(b,-e,1,1,maxU,minV); tv(b,-e,1,0,minU,minV); break;
            case EAST:
                tv(b,1+e,0,1,maxU,maxV); tv(b,1+e,0,0,minU,maxV);
                tv(b,1+e,1,0,minU,minV); tv(b,1+e,1,1,maxU,minV); break;
            default: break;
        }
    }

    private static void cv(BufferBuilder b, double x, double y, double z, float[] c) {
        b.pos(x,y,z).color(c[0],c[1],c[2],c[3]).endVertex();
    }

    private static void tv(BufferBuilder b, double x, double y, double z, float u, float v) {
        b.pos(x,y,z).tex(u,v).color(1.0F,1.0F,1.0F,0.78F).endVertex();
    }

    private static final class FaceDraw {
        private final EnumFacing face;
        private final float[] color;
        private FaceDraw(EnumFacing face, float[] color) { this.face=face; this.color=color; }
    }
}
