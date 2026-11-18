package com.graund.block.proxy;

import com.graund.block.client.ClientInteractionHandler;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new ClientInteractionHandler());
    }
}