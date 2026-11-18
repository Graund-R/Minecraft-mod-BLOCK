package com.graund.block.client.render;

import com.graund.block.tile.FaceMode;
import com.graund.block.tile.TileEntityVeil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/** Draws the visible face treatment; occlusion is handled by MaskDepthPass. */
public class VeilTileRenderer extends TileEntitySpecialRenderer<TileEntityVeil> {
    private static final float[] OPAQUE = {0.13F, 0.16F, 0.20F, 1.0F};
    private static final float[] GLASS = {0.62F, 0.84F, 0.96F, 0.24F};
    private static final float[] WINDOW = {0.38F, 0.76F, 0.94F, 0.22F};

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
        List<FaceDraw> transparent = new ArrayList<>();
        for (EnumFacing face : EnumFacing.values()) {
            FaceMode mode = tile.getMode(face);
            if (mode == FaceMode.MASK) continue;
            if (mode == FaceMode.OPAQUE) {
                opaque.add(new FaceDraw(face, OPAQUE));
            } else if (mode == FaceMode.GLASS) {
                transparent.add(new FaceDraw(face, GLASS));
            } else if (mode == FaceMode.ONE_WAY) {
                double side = dx * face.getDirectionVec().getX()
                        + dy * face.getDirectionVec().getY()
                        + dz * face.getDirectionVec().getZ();
                if (side > 0.0001D) transparent.add(new FaceDraw(face, WINDOW));
                else opaque.add(new FaceDraw(face, OPAQUE));
            }
        }
        if (opaque.isEmpty() && transparent.isEmpty()) return;

        boolean oldBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean oldTexture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean oldCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean oldLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            GlStateManager.disableTexture2D();
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
                GlStateManager.depthMask(true);
                draw(opaque);
            }
            if (!transparent.isEmpty()) {
                GlStateManager.depthMask(false);
                draw(transparent);
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

    private static void draw(List<FaceDraw> faces) {
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (FaceDraw f : faces) addFace(b, f.face, f.color);
        Tessellator.getInstance().draw();
    }

    private static void addFace(BufferBuilder b, EnumFacing face, float[] c) {
        double e = 0.0015D;
        switch (face) {
            case DOWN:
                v(b,0,-e,1,c); v(b,0,-e,0,c); v(b,1,-e,0,c); v(b,1,-e,1,c); break;
            case UP:
                v(b,0,1+e,0,c); v(b,0,1+e,1,c); v(b,1,1+e,1,c); v(b,1,1+e,0,c); break;
            case NORTH:
                v(b,1,0,-e,c); v(b,0,0,-e,c); v(b,0,1,-e,c); v(b,1,1,-e,c); break;
            case SOUTH:
                v(b,0,0,1+e,c); v(b,1,0,1+e,c); v(b,1,1,1+e,c); v(b,0,1,1+e,c); break;
            case WEST:
                v(b,-e,0,0,c); v(b,-e,0,1,c); v(b,-e,1,1,c); v(b,-e,1,0,c); break;
            case EAST:
                v(b,1+e,0,1,c); v(b,1+e,0,0,c); v(b,1+e,1,0,c); v(b,1+e,1,1,c); break;
            default: break;
        }
    }

    private static void v(BufferBuilder b, double x, double y, double z, float[] c) {
        b.pos(x,y,z).color(c[0],c[1],c[2],c[3]).endVertex();
    }

    private static final class FaceDraw {
        private final EnumFacing face;
        private final float[] color;
        private FaceDraw(EnumFacing face, float[] color) { this.face=face; this.color=color; }
    }
}
