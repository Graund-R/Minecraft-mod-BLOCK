package com.graund.block.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.EnumMap;
import java.util.Map;

public class TileEntityVeil extends TileEntity {
    private final Map<EnumFacing, FaceMode> faceModes = new EnumMap<>(EnumFacing.class);
    private int maskDepth = 16;

    public TileEntityVeil() {
        for (EnumFacing face : EnumFacing.values()) {
            faceModes.put(face, FaceMode.GLASS);
        }
    }

    public FaceMode getMode(EnumFacing face) {
        return faceModes.get(face);
    }

    public FaceMode cycleMode(EnumFacing face) {
        FaceMode next = getMode(face).next();
        faceModes.put(face, next);
        markDirty();
        if (world != null && !world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
        return next;
    }

    public int getMaskDepth() {
        return maskDepth;
    }

    public void setMaskDepth(int depth) {
        maskDepth = Math.max(1, Math.min(128, depth));
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagCompound faces = new NBTTagCompound();
        for (EnumFacing face : EnumFacing.values()) {
            faces.setInteger(face.getName(), getMode(face).ordinal());
        }
        compound.setTag("FaceModes", faces);
        compound.setInteger("MaskDepth", maskDepth);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        NBTTagCompound faces = compound.getCompoundTag("FaceModes");
        for (EnumFacing face : EnumFacing.values()) {
            int ordinal = faces.hasKey(face.getName()) ? faces.getInteger(face.getName()) : 0;
            FaceMode[] modes = FaceMode.values();
            faceModes.put(face, modes[Math.max(0, Math.min(modes.length - 1, ordinal))]);
        }
        maskDepth = Math.max(1, Math.min(128,
                compound.hasKey("MaskDepth") ? compound.getInteger("MaskDepth") : 16));
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        handleUpdateTag(packet.getNbtCompound());
    }
}