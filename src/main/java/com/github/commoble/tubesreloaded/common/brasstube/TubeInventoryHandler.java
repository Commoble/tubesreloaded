package com.github.commoble.tubesreloaded.common.brasstube;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class TubeInventoryHandler extends ItemStackHandler
{
	public TileEntityBrassTube tube;

	public TubeInventoryHandler(TileEntityBrassTube tube)
	{
		super(1);
		this.tube = tube;
	}

	// the return value is the portion of the stack that was NOT inserted
	// if "simulate" is true, do not insert the item, but return the same value that
	// would be returned if it was a real insertion
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
		{
			return ItemStack.EMPTY;
		}
		if (!simulate)
		{
			this.tube.enqueueItemStack(stack);
		}
		return ItemStack.EMPTY;
	}
}
