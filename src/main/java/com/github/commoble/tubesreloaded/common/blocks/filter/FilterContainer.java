package com.github.commoble.tubesreloaded.common.blocks.filter;

import com.github.commoble.tubesreloaded.common.registry.ContainerRegistrar;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FilterContainer extends Container
{
	public final IInventory inventory;

	// called on Client-side when packet received	
	public static FilterContainer getClientContainer(int id, PlayerInventory playerInventory)
	{
		return new FilterContainer(id, playerInventory, new Inventory(1));
	}
	
	public static IContainerProvider getServerContainerProvider(FilterTileEntity filter)
	{
		return (id, playerInventory, theServerPlayer) -> new FilterContainer(id, playerInventory, new FilterInventory(filter));
	}

	private FilterContainer(int id, PlayerInventory playerInventory, IInventory filterInventory)
	{
		super(ContainerRegistrar.FILTER, id);
		this.inventory = filterInventory;

		// add filter slot
		this.addSlot(new FilterSlot(filterInventory, 0, 80, 35));

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
				if (!this.mergeItemStack(stackFromSlot, 1, 37, true))
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
	
	static class FilterSlot extends Slot
	{
		public FilterSlot(IInventory inventoryIn, int index, int xPosition, int yPosition)
		{
			super(inventoryIn, index, xPosition, yPosition);
		}

		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
	}
	
	static class FilterInventory implements IInventory
	{
		private final FilterTileEntity filter;
		
		public FilterInventory(FilterTileEntity filter)
		{
			this.filter = filter;
		}
		
		@Override
		public void clear()
		{
			this.filter.setFilterStackAndSaveAndSync(ItemStack.EMPTY);
		}

		@Override
		public int getSizeInventory()
		{
			return 1;
		}

		@Override
		public boolean isEmpty()
		{
			return this.filter.filterStack.isEmpty();
		}

		@Override
		public ItemStack getStackInSlot(int index)
		{
			return this.filter.filterStack;
		}

		@Override
		public ItemStack decrStackSize(int index, int count)
		{
			ItemStack newStack = this.filter.filterStack.split(count);
			this.filter.setFilterStackAndSaveAndSync(this.filter.filterStack);
			return newStack;
		}

		@Override
		public ItemStack removeStackFromSlot(int index)
		{
			ItemStack stack = this.filter.filterStack.copy();
			this.filter.setFilterStackAndSaveAndSync(ItemStack.EMPTY);
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack)
		{
			this.filter.setFilterStackAndSaveAndSync(stack);
		}

		@Override
		public void markDirty()
		{
			this.filter.markDirty();
		}

		@Override
		public boolean isUsableByPlayer(PlayerEntity player)
		{
			World world = this.filter.getWorld();
			BlockPos pos = this.filter.getPos();
			Block block = this.filter.getBlockState().getBlock();
			return world.getBlockState(pos).getBlock() != block ? false : player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
		}
		
	}

}
