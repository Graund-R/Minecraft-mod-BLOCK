package com.graund.block.init;

import com.graund.block.VeilBlockMod;
import com.graund.block.block.BlockVeil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = VeilBlockMod.MODID)
public final class ModBlocks {
    public static final BlockVeil VEIL = new BlockVeil();

    private ModBlocks() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(VEIL);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(VEIL)
                .setRegistryName(VEIL.getRegistryName()));
    }
}