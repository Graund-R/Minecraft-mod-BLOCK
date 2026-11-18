package com.graund.block.client;

import com.graund.block.init.ModBlocks;
import com.graund.block.network.CycleFaceMessage;
import com.graund.block.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientInteractionHandler {
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getWorld().isRemote || !event.getEntityPlayer().isSneaking()) {
            return;
        }
        if (event.getWorld().getBlockState(event.getPos()).getBlock() != ModBlocks.VEIL) {
            return;
        }

        // Consume the normal mining action and send a face-configuration request instead.
        event.setCanceled(true);
        NetworkHandler.CHANNEL.sendToServer(new CycleFaceMessage(event.getPos(), event.getFace()));
    }
}