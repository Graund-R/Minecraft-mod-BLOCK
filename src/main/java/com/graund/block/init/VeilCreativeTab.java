package com.graund.block.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class VeilCreativeTab {
    public static final CreativeTabs TAB = new CreativeTabs("veilblock") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ModBlocks.VEIL);
        }
    };

    private VeilCreativeTab() {
    }
}
