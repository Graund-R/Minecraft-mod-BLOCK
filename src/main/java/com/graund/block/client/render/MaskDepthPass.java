package com.graund.block.client.render;

import com.graund.block.tile.FaceMode;
import com.graund.block.tile.TileEntityVeil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes per-viewer Veil Block surfaces into the depth buffer before terrain
 * is drawn. Color writes are disabled, so the pass does not draw a visible
 * surface. Normal terrain, entities and particles then respect the depth
 * buffer in the usual way.
 */
public final class MaskDepthPass {
    private MaskDepthPass() {
    }

    public static void render(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        WorldClient world = minecraft.world;
        Entity camera = minecraft.getRenderViewEntity();

        if (world == null || camera == null) {
            return;
        }

        double cameraX = camera.lastTickPosX
                + (camera.posX - camera.lastTickPosX) * (double) partialTicks;
        double cameraY = camera.lastTickPosY
                + (camera.posY - camera.lastTickPosY) * (double) partialTicks
                + (double) camera.getEyeHeight();
        double cameraZ = camera.lastTickPosZ
                + (camera.posZ - camera.lastTickPosZ) * (double) partialTicks;

        double maxDistance = minecraft.gameSettings.renderDistanceChunks * 16.0D + 16.0D;
        double maxDistanceSq = maxDistance * maxDistance;
        List<FaceDraw> faces = new ArrayList<>();

        for (TileEntity tile : world.loadedTileEntityList) {
            if (!(tile instanceof TileEntityVeil) || tile.isInvalid()) {
                continue;
            }

            TileEntityVeil veil = (TileEntityVeil) tile;
            BlockPos pos = veil.getPos();
            double centerX = pos.getX() + 0.5D;
            double centerY = pos.getY() + 0.5D;
            double centerZ = pos.getZ() + 0.5D;
            double dx = cameraX - centerX;
            double dy = cameraY - centerY;
            double dz = cameraZ - centerZ;

            if (dx * dx + dy * dy + dz * dz > maxDistanceSq) {
                continue;
            }

            for (EnumFacing face : EnumFacing.values()) {
                FaceMode mode = veil.getMode(face);

                if (mode == FaceMode.MASK || mode == FaceMode.OPAQUE) {
                    faces.add(new FaceDraw(pos, face));
                } else if (mode == FaceMode.ONE_WAY) {
                    double side = dx * face.getXOffset()
                            + dy * face.getYOffset()
                            + dz * face.getZOffset();

                    // ONE_WAY lets the observer see through from the face's
                    // outward side, but blocks the reverse direction.
                    if (side < -0.0001D) {
                        faces.add(new FaceDraw(pos, face));
                    }
                }
            }
        }

        if (faces.isEmpty()) {
            return;
        }

        ByteBuffer oldColorMask = BufferUtils.createByteBuffer(4);
        GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK, oldColorMask);
        boolean oldDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        boolean oldCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean oldTexture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean oldDepth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        int oldDepthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);

        GlStateManager.pushMatrix();
        try {
            // orientCamera() has already applied the camera rotation and
            // eye-height transform. Translate world-space vertices by the
            // interpolated camera position, as the chunk renderer does.
            GlStateManager.translate(-cameraX, -cameraY, -cameraZ);
            GlStateManager.enableDepth();
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
            GlStateManager.depthMask(true);
            GlStateManager.colorMask(false, false, false, false);
            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();

            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            for (FaceDraw face : faces) {
                addFace(buffer, face.pos, face.face);
            }

            Tessellator.getInstance().draw();
        } finally {
            GlStateManager.colorMask(
                    oldColorMask.get(0) != 0,
                    oldColorMask.get(1) != 0,
                    oldColorMask.get(2) != 0,
                    oldColorMask.get(3) != 0
            );
            GlStateManager.depthMask(oldDepthMask);
            GlStateManager.depthFunc(oldDepthFunction);

            if (oldCull) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }

            if (oldTexture) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }

            if (oldDepth) {
                GlStateManager.enableDepth();
            } else {
                GlStateManager.disableDepth();
            }

            GlStateManager.popMatrix();
        }
    }

    private static void addFace(BufferBuilder buffer, BlockPos pos, EnumFacing face) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        switch (face) {
            case DOWN:
                vertex(buffer, x, y, z + 1);
                vertex(buffer, x, y, z);
                vertex(buffer, x + 1, y, z);
                vertex(buffer, x + 1, y, z + 1);
                break;
            case UP:
                vertex(buffer, x, y + 1, z);
                vertex(buffer, x, y + 1, z + 1);
                vertex(buffer, x + 1, y + 1, z + 1);
                vertex(buffer, x + 1, y + 1, z);
                break;
            case NORTH:
                vertex(buffer, x + 1, y, z);
                vertex(buffer, x, y, z);
                vertex(buffer, x, y + 1, z);
                vertex(buffer, x + 1, y + 1, z);
                break;
            case SOUTH:
                vertex(buffer, x, y, z + 1);
                vertex(buffer, x + 1, y, z + 1);
                vertex(buffer, x + 1, y + 1, z + 1);
                vertex(buffer, x, y + 1, z + 1);
                break;
            case WEST:
                vertex(buffer, x, y, z);
                vertex(buffer, x, y, z + 1);
                vertex(buffer, x, y + 1, z + 1);
                vertex(buffer, x, y + 1, z);
                break;
            case EAST:
                vertex(buffer, x + 1, y, z + 1);
                vertex(buffer, x + 1, y, z);
                vertex(buffer, x + 1, y + 1, z);
                vertex(buffer, x + 1, y + 1, z + 1);
                break;
            default:
                break;
        }
    }

    private static void vertex(BufferBuilder buffer, double x, double y, double z) {
        buffer.pos(x, y, z).endVertex();
    }

    private static final class FaceDraw {
        private final BlockPos pos;
        private final EnumFacing face;

        private FaceDraw(BlockPos pos, EnumFacing face) {
            this.pos = pos;
            this.face = face;
        }
    }
}
