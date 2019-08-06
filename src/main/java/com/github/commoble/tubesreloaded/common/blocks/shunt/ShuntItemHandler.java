package com.github.commoble.tubesreloaded.common.blocks.shunt;

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

public class ShuntItemHandler implements IItemHandler
{
	public final ShuntTileEntity shunt;
	public final boolean can_take_items;
	
	// item handler of the block this may be sending items to
	private LazyOptional<IItemHandler> targetInventory = LazyOptional.empty();

	public ShuntItemHandler(ShuntTileEntity shunt, boolean can_take_items)
	{
		this.shunt = shunt;
		this.can_take_items = can_take_items;
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
		if (!this.can_take_items)
		{
			return stack.copy();
		}
		
		if (!simulate) // actually inserting an item
		{
			// attempt to insert item
			BlockPos shunt_pos = this.shunt.getPos();
			Direction output_dir = this.shunt.getBlockState().get(ShuntBlock.FACING);
			BlockPos output_pos = shunt_pos.offset(output_dir);
			ItemStack remaining = this.getOutputOptional(output_pos, output_dir)
					.map(handler -> WorldHelper.disperseItemToHandler(stack, handler))
					.orElse(stack.copy());
			
			if (remaining.getCount() > 0) // we have remaining items
			{
				WorldHelper.ejectItemstack(this.shunt.getWorld(), shunt_pos, output_dir, remaining);
			}
		}
		
		return ItemStack.EMPTY;
	}
	
	private LazyOptional<IItemHandler> getOutputOptional(BlockPos output_pos, Direction output_dir)
	{
		if (!this.targetInventory.isPresent())
		{
			// if the block we are attempting to insert the item into is a shuntlike block, do not insert
			Tag<Block> shuntTag = BlockTags.getCollection().get(new ResourceLocation("tubesreloaded", "shunts"));
			this.targetInventory = WorldHelper.getTEItemHandlerAtIf(this.shunt.getWorld(), output_pos, output_dir.getOpposite(), te -> !shuntTag.contains(te.getBlockState().getBlock()));
		}
		return this.targetInventory;
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
		// TODO Auto-generated method stub
		return this.can_take_items;
	}

}
