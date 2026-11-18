package com.graund.block.event;

import com.graund.block.init.ModBlocks;
import com.graund.block.tile.FaceMode;
import com.graund.block.tile.TileEntityVeil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockInteractionHandler {
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getEntityPlayer().isSneaking()) {
            return;
        }
        if (event.getWorld().getBlockState(event.getPos()).getBlock() != ModBlocks.VEIL) {
            return;
        }

        // Sneak + left-click configures the face and must not damage/break the block.
        event.setCanceled(true);
        if (event.getWorld().isRemote) {
            return;
        }

        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (!(tile instanceof TileEntityVeil)) {
            return;
        }

        EnumFacing face = event.getFace();
        FaceMode mode = ((TileEntityVeil) tile).cycleMode(face);
        event.getEntityPlayer().sendStatusMessage(
                new TextComponentString(face.getName().toUpperCase() + ": " + mode.getDisplayName()), true);
    }
}