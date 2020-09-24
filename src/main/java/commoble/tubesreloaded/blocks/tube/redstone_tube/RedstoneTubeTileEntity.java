package commoble.tubesreloaded.blocks.tube.redstone_tube;

import commoble.tubesreloaded.blocks.tube.TubeTileEntity;
import commoble.tubesreloaded.registry.BlockRegistrar;
import commoble.tubesreloaded.registry.TileEntityRegistrar;
import net.minecraft.block.BlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

public class RedstoneTubeTileEntity extends TubeTileEntity
{
	public RedstoneTubeTileEntity()
	{
		super(TileEntityRegistrar.REDSTONE_TUBE);
	}

	@Override
	public void tick()
	{ // block is powered while there are items moving through it
		// change state when contents of inventory change from nothing to something or
		// from something to nothing
		super.tick();
		if (!this.world.isRemote)
		{
			boolean hasItems = this.inventory.size() > 0;
			boolean isPowered = this.getBlockState().get(RedstoneTubeBlock.POWERED);
			if (hasItems != isPowered)
			{
				BlockState newState = this.getBlockState().with(RedstoneTubeBlock.POWERED, Boolean.valueOf(hasItems));
				this.world.setBlockState(this.pos, newState);
				this.world.playSound(null, this.pos,
						hasItems ? SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF,
						SoundCategory.BLOCKS, 0.3F, hasItems ? 0.6F : 0.5F);
				this.world.notifyNeighborsOfStateChange(this.pos, BlockRegistrar.REDSTONE_TUBE);
			}
		}
	}
}
