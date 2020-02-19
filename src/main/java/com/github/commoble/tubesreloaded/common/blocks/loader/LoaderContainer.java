package com.github.commoble.tubesreloaded.common.blocks.loader;

import com.github.commoble.tubesreloaded.common.registry.BlockRegistrar;
import com.github.commoble.tubesreloaded.common.registry.ContainerRegistrar;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;

public class LoaderContainer extends Container
{
	public final PlayerEntity player;
	public final BlockPos pos;
	public final Slot loaderSlot;

	public LoaderContainer(int id, PlayerInventory playerInventory, BlockPos pos)
	{
		super(ContainerRegistrar.LOADER, id);
		this.player = playerInventory.player;
		this.pos = pos;

		// add input slot
		this.loaderSlot = this.addSlot(new LoaderSlot(this, 0, 80, 35));

		// add player inventory
		for (int backpackRow = 0; backpackRow < 3; ++backpackRow)
		{
			for (int backpackColumn = 0; backpackColumn < 9; ++backpackColumn)
			{
				this.addSlot(new Slot(playerInventory, backpackColumn + backpackRow * 9 + 1, 8 + backpackColumn * 18, 84 + backpackRow * 18));
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
		return isWithinUsableDistance(IWorldPosCallable.of(playerIn.world, this.pos), playerIn, BlockRegistrar.LOADER);
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this
	 * moves the stack between the player inventory and the other inventory(s).
	 */
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack())
		{
			ItemStack stackInSlot = slot.getStack();

			if (!stackInSlot.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
				slot.onSlotChanged();
				this.loaderSlot.putStack(stackInSlot.copy());
			}
		}

		return ItemStack.EMPTY;
	}
}
