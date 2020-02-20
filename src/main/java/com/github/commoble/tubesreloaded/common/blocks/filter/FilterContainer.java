package com.github.commoble.tubesreloaded.common.blocks.filter;

import com.github.commoble.tubesreloaded.common.registry.ContainerRegistrar;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class FilterContainer extends Container
{
	public final IInventory inventory;

	public FilterContainer(int id, PlayerInventory playerInventory)
	{
		this(id, playerInventory, new Inventory(1));
	}

	public FilterContainer(int id, PlayerInventory playerInventory, IInventory filterInventory)
	{
		super(ContainerRegistrar.FILTER, id);
		this.inventory = filterInventory;

		// add filter slot
		this.addSlot(new Slot(filterInventory, 0, 80, 35));

		for (int backpackRow = 0; backpackRow < 3; ++backpackRow)
		{
			for (int backpackColumn = 0; backpackColumn < 9; ++backpackColumn)
			{
				this.addSlot(new Slot(playerInventory, backpackColumn + backpackRow * 9 + 9, 8 + backpackColumn * 18, 84 + backpackRow * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
		{
			this.addSlot(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
		}

	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return this.inventory.isUsableByPlayer(playerIn);

	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this
	 * moves the stack between the player inventory and the other inventory(s).
	 */
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		ItemStack copiedStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack())
		{
			ItemStack stackFromSlot = slot.getStack();
			copiedStack = stackFromSlot.copy();
			if (index == 0)
			{
				if (!this.mergeItemStack(stackFromSlot, 9, 37, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(stackFromSlot, 0, 1, false))
			{
				return ItemStack.EMPTY;
			}

			if (stackFromSlot.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (stackFromSlot.getCount() == copiedStack.getCount())
			{
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, stackFromSlot);
		}

		return copiedStack;
	}

}
