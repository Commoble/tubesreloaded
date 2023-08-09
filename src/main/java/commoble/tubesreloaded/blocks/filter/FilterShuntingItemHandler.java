package commoble.tubesreloaded.blocks.filter;

import commoble.tubesreloaded.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class FilterShuntingItemHandler implements IItemHandler
{
	private final AbstractFilterBlockEntity filter;
	private boolean shunting = false; // true while retreiving insertion result from neighbor, averts infinite loops
	
	// inventory of the block we may be sending items to
	private LazyOptional<IItemHandler> targetInventory = LazyOptional.empty();
	
	public FilterShuntingItemHandler (AbstractFilterBlockEntity filter)
	{
		this.filter = filter;
	}

	@Override
	public int getSlots()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		// TODO Auto-generated method stub
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (!this.filter.getBlockState().hasProperty(DirectionalBlock.FACING))
			return stack;
		
		if (this.shunting || !this.isItemValid(slot, stack))
		{
			return stack.copy();
		}
		
		if (!simulate) // actually inserting an item
		{
			// attempt to insert item
			BlockPos pos = this.filter.getBlockPos();
			Direction outputDir = this.filter.getBlockState().getValue(DirectionalBlock.FACING);
			BlockPos outputPos = pos.relative(outputDir);
			
			this.shunting = true;
			ItemStack remaining = this.getOutputOptional(outputPos,outputDir)
				.map(handler -> WorldHelper.disperseItemToHandler(stack, handler))
				.orElse(stack.copy());
			this.shunting = false;
			
			if (remaining.getCount() > 0) // we have remaining items
			{
				WorldHelper.ejectItemstack(this.filter.getLevel(), pos, outputDir, remaining);
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	private LazyOptional<IItemHandler> getOutputOptional(BlockPos outputPos, Direction outputDir)
	{
		if (!this.targetInventory.isPresent())
		{
			this.targetInventory = WorldHelper.getItemHandlerAt(this.filter.getLevel(), outputPos, outputDir.getOpposite());
		}
		return this.targetInventory;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return stack.getCount() > 0 && this.filter.canItemPassThroughFilter(stack);
	}

}
