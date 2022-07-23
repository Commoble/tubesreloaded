package commoble.tubesreloaded.client;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

/**
 * Delegates to another blockreader for most things, but treats one position as having a different state
 */
public record FakeWorldForTubeRaytrace(BlockGetter delegate, BlockPos pos, BlockState state) implements BlockGetter
{
	@Override
	public BlockEntity getBlockEntity(BlockPos pos)
	{
		return this.delegate.getBlockEntity(pos);
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

	@Override
	public int getHeight()
	{
		return delegate.getHeight();
	}

	@Override
	public int getMinBuildHeight()
	{
		return delegate.getMinBuildHeight();
	}
}
