package com.graund.block.network;

import com.graund.block.VeilBlockMod;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class NetworkHandler {
    public static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel(VeilBlockMod.MODID);

    private NetworkHandler() {
    }

    public static void init() {
        CHANNEL.registerMessage(CycleFaceMessage.Handler.class, CycleFaceMessage.class, 0, Side.SERVER);
    }
}