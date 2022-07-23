package commoble.tubesreloaded.blocks.shunt;

import commoble.tubesreloaded.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class ShuntItemHandler implements IItemHandler
{
	public final ShuntBlockEntity shunt;
	public final boolean canTakeItems;
	private boolean shunting = false; // prevent infinite loops

	public ShuntItemHandler(ShuntBlockEntity shunt, boolean canTakeItems)
	{
		this.shunt = shunt;
		this.canTakeItems = canTakeItems;
	}

	@Override
	public int getSlots()
	{
		// TODO Auto-generated method stub
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
		if (!this.canTakeItems)
		{
			return stack.copy();
		}
		
		if (!simulate) // actually inserting an item
		{
			BlockPos shuntPos = this.shunt.getBlockPos();
			Direction outputDir = this.shunt.getBlockState().getValue(ShuntBlock.FACING);
			BlockPos outputPos = shuntPos.relative(outputDir);
			
			if (!this.shunting)
			{
				// attempt to insert item
				this.shunting = true;
				ItemStack remaining = this.getOutputOptional(outputPos, outputDir)
						.map(handler -> WorldHelper.disperseItemToHandler(stack, handler))
						.orElseGet(() -> stack.copy());
				this.shunting = false;
				
				if (remaining.getCount() > 0) // we have remaining items
				{
					WorldHelper.ejectItemstack(this.shunt.getLevel(), shuntPos, outputDir, remaining);
				}
			}
			else
			{
				WorldHelper.ejectItemstack(this.shunt.getLevel(), shuntPos, outputDir, stack.copy());
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	private LazyOptional<IItemHandler> getOutputOptional(BlockPos outputPos, Direction outputDir)
	{
		return WorldHelper.getItemHandlerAt(this.shunt.getLevel(), outputPos, outputDir.getOpposite());
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		// TODO Auto-generated method stub
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64; // same as generic handler
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		Level world = this.shunt.getLevel();
		BlockPos pos = this.shunt.getBlockPos();
		return this.canTakeItems && !world.hasNeighborSignal(pos);
	}

}
