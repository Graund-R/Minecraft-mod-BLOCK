package com.graund.block.network;

import com.graund.block.init.ModBlocks;
import com.graund.block.tile.FaceMode;
import com.graund.block.tile.TileEntityVeil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CycleFaceMessage implements IMessage {
    private BlockPos pos;
    private EnumFacing face;

    public CycleFaceMessage() {
    }

    public CycleFaceMessage(BlockPos pos, EnumFacing face) {
        this.pos = pos;
        this.face = face;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        int side = buf.readUnsignedByte();
        face = side < EnumFacing.values().length ? EnumFacing.values()[side] : EnumFacing.UP;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeByte(face.ordinal());
    }

    public static class Handler implements IMessageHandler<CycleFaceMessage, IMessage> {
        @Override
        public IMessage onMessage(final CycleFaceMessage message, final MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (!player.isSneaking() || player.getDistanceSq(
                            message.pos.getX() + 0.5D,
                            message.pos.getY() + 0.5D,
                            message.pos.getZ() + 0.5D) > 64.0D) {
                        return;
                    }
                    if (player.world.getBlockState(message.pos).getBlock() != ModBlocks.VEIL) {
                        return;
                    }

                    TileEntity tile = player.world.getTileEntity(message.pos);
                    if (!(tile instanceof TileEntityVeil)) {
                        return;
                    }

                    FaceMode mode = ((TileEntityVeil) tile).cycleMode(message.face);
                    player.sendStatusMessage(new TextComponentString(
                            message.face.getName().toUpperCase() + ": " + mode.getDisplayName()), true);
                }
            });
            return null;
        }
    }
}