package com.graund.block.client;

import com.graund.block.init.ModBlocks;
import com.graund.block.network.CycleFaceMessage;
import com.graund.block.network.NetworkHandler;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientInteractionHandler {
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getEntityPlayer().isSneaking()) {
            return;
        }
        if (event.getWorld().getBlockState(event.getPos()).getBlock() != ModBlocks.VEIL) {
            return;
        }

        // Cancel on both logical sides so the configuration click never starts mining.
        event.setCanceled(true);

        // Only the client sends the request; the server performs the authoritative change.
        if (event.getWorld().isRemote && event.getFace() != null) {
            NetworkHandler.CHANNEL.sendToServer(
                    new CycleFaceMessage(event.getPos(), event.getFace()));
        }
    }
}
