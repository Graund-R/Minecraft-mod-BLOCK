package com.graund.block.block;

import com.graund.block.VeilBlockMod;
import com.graund.block.tile.TileEntityVeil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockVeil extends BlockContainer {
    public BlockVeil() {
        super(Material.GLASS);
        setRegistryName(VeilBlockMod.MODID, "veil");
        setUnlocalizedName(VeilBlockMod.MODID + ".veil");
        setHardness(1.0F);
        setResistance(3.0F);
        setLightOpacity(0);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.REDSTONE);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityVeil();
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}