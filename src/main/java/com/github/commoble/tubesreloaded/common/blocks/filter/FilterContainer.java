package com.github.commoble.tubesreloaded.common.blocks.filter;

import java.util.Optional;

import com.github.commoble.tubesreloaded.common.registry.ContainerRegistrar;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;

public class FilterContainer extends Container
{
	public final Optional<FilterTileEntity> filterProxy;
	public final Slot filterSlot;

	public FilterContainer(int id, PlayerInventory playerInventory)
	{
		this(id, playerInventory, Optional.empty());
	}

	public FilterContainer(int id, PlayerInventory playerInventory, Optional<FilterTileEntity> filterProxy)
	{
		super(ContainerRegistrar.FILTER, id);
		this.filterProxy = filterProxy;

		// add filter slot
		this.filterSlot = this.addSlot(new FilterSlot(this, 0, 80, 35));

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
		return this.filterProxy
			.map(filter -> Container.isWithinUsableDistance(IWorldPosCallable.of(filter.getWorld(), filter.getPos()), playerIn, filter.getBlockState().getBlock()))
			.orElse(false);

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
				if (!this.mergeItemStack(stackFromSlot, 9, 45, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(stackFromSlot, 0, 9, false))
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
