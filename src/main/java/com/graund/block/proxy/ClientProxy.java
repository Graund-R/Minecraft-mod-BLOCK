package com.graund.block.proxy;

import com.graund.block.client.ClientInteractionHandler;
import com.graund.block.client.render.VeilTileRenderer;
import com.graund.block.tile.TileEntityVeil;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new ClientInteractionHandler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVeil.class, new VeilTileRenderer());
    }
}