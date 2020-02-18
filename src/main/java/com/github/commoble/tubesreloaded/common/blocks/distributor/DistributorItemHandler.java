package com.github.commoble.tubesreloaded.common.blocks.distributor;

import com.github.commoble.tubesreloaded.common.util.WorldHelper;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class DistributorItemHandler implements IItemHandler
{
	protected final DistributorTileEntity distributor;
	public final Direction inputFace; // face of the distributor block that items are inserted into
	
	private Direction nextDirection = Direction.DOWN; 
	
	public DistributorItemHandler(DistributorTileEntity distributor, Direction inputFace)
	{
		this.distributor = distributor;
		this.inputFace = inputFace;
	}
	
	public int getNextDirectionIndex()
	{
		return this.nextDirection.getIndex();
	}
	
	public void setNextDirectionIndex(int index)
	{
		this.nextDirection = Direction.byIndex(index);
		this.distributor.markDirty();
	}
	
	@Override
	public int getSlots()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		// starting with the index of the next direction to check,
		// check all directions EXCEPT the input direction.
		// when a valid item handler to send the item to is found,
		// send as much of the remaining stack as possible in that direction.
		// Stop looking when remaining stack is empty, or when all directions have been checked.
		// Afterward, set the next direction to check to the next direction after the last checked direction.
		// Return empty stack if successfully sent item onward, return the entire stack if failure to do so.
		int startCheckIndex = this.nextDirection.getIndex();
		int checkIndex = 0;
		Direction checkDirection;
		ItemStack remainingStack = stack;
		
		if (!simulate && !stack.isEmpty())
		{
			// avoid checking neighboring TEs during simulation to avoid infinite loops in tubes
			// this unfortunately means that we have to wholly accept any item sent into it
			// so we'll have to eject the item if there's nowhere to send it onward to
			
			for (int i=0; i<6; i++)
			{
				checkIndex = (i + startCheckIndex) % 6;
				checkDirection = Direction.byIndex(checkIndex);
				if (checkDirection == this.inputFace)
					continue;
				
				BlockPos outputPos = this.distributor.getPos().offset(checkDirection);
				final ItemStack stackForNextInsertion = remainingStack.copy();
				remainingStack = this.getOutputOptional(outputPos, checkDirection)
					.map(handler -> WorldHelper.disperseItemToHandler(stackForNextInsertion, handler, simulate))
					.orElse(remainingStack);
				
				if (remainingStack.isEmpty())
				{
					break;
				}
				
			}
			
			if (!remainingStack.isEmpty())
			{
				WorldHelper.ejectItemstack(this.distributor.getWorld(), this.distributor.getPos(), this.inputFace.getOpposite(), remainingStack);
			}
			
			this.setNextDirectionIndex((checkIndex + 1) % 6);
		}
		
		return ItemStack.EMPTY;
	}
	
	private LazyOptional<IItemHandler> getOutputOptional(BlockPos output_pos, Direction output_dir)
	{
		// if the block we are attempting to insert the item into is a shuntlike block, do not insert
		Tag<Block> shuntTag = BlockTags.getCollection().get(new ResourceLocation("tubesreloaded", "shunts"));
		return WorldHelper.getTEItemHandlerAtIf(this.distributor.getWorld(), output_pos, output_dir.getOpposite(), te -> !shuntTag.contains(te.getBlockState().getBlock()));
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
		return true;
	}

}
