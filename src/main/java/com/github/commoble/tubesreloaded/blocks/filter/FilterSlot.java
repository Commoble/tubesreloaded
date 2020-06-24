package com.github.commoble.tubesreloaded.blocks.filter;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;

public class FilterSlot extends Slot
{
	private final IInventory inventory;

	public FilterSlot(IInventory inventory, int index, int xPosition, int yPosition)
	{
		super(new FilterSlotInventory(), index,xPosition,yPosition);
		this.inventory=inventory;
	}

	static class FilterSlotInventory extends Inventory
	{
		public FilterSlotInventory()
		{
			super(1);
		}
		/**
		 * Returns the maximum stack size for a inventory slot. Seems to always be 64,
		 * possibly will be extended.
		 */
		@Override
		public int getInventoryStackLimit()
		{
			return 1;
		}
	}
}
