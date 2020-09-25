package commoble.tubesreloaded.client;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * Delegates to another blockreader for most things, but treats one position as having a different state
 */
public class FakeWorldForTubeRaytrace implements IBlockReader
{
	private final IBlockReader delegate;
	private final BlockPos pos;
	private final BlockState state;
	
	public FakeWorldForTubeRaytrace(IBlockReader delegate, BlockPos pos, BlockState state)
	{
		this.delegate = delegate;
		this.pos = pos;
		this.state = state;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos)
	{
		return this.delegate.getTileEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos)
	{
		return pos.equals(this.pos) ? this.state : this.delegate.getBlockState(pos); 
	}

	@Override
	public FluidState getFluidState(BlockPos pos)
	{
		return this.delegate.getFluidState(pos);
	}
	
}
