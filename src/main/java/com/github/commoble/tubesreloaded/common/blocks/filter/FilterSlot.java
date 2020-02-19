package com.github.commoble.tubesreloaded.common.blocks.filter;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class FilterSlot extends Slot
{
	private final FilterContainer container;
	
	public FilterSlot(FilterContainer container, int index, int xPosition, int yPosition)
	{
		super(new Inventory(1), index, xPosition, yPosition);
		this.container = container;
	}

	@Override
	public void onSlotChange(ItemStack from, ItemStack to)
	{
		this.container.filterProxy.ifPresent(filter -> filter.setFilterStackAndSaveAndSync(to));
	}
}
