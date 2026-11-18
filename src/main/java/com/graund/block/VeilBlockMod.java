package com.graund.block;

import com.graund.block.network.NetworkHandler;
import com.graund.block.proxy.CommonProxy;
import com.graund.block.tile.TileEntityVeil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = VeilBlockMod.MODID, name = VeilBlockMod.NAME, version = VeilBlockMod.VERSION,
        acceptedMinecraftVersions = "[1.12.2"])
public class VeilBlockMod {
    public static final String MODID = "veilblock";
    public static final String NAME = "Veil Block";
    public static final String VERSION = "0.1.0";

    @SidedProxy(clientSide = "com.graund.block.proxy.ClientProxy",
                serverSide = "com.graund.block.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerTileEntity(TileEntityVeil.class, MODID + ":veil");
        NetworkHandler.init();
        proxy.preInit();
    }
}