package mcmod.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockRadar extends Block {

	public BlockRadar(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return super.hasTileEntity(state);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return super.createTileEntity(state, world);
	}
}
