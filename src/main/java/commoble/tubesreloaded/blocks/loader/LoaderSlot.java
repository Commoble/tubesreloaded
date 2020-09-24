package commoble.tubesreloaded.blocks.loader;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LoaderSlot extends Slot
{
	public LoaderSlot(LoaderContainer container, int index, int xPosition, int yPosition)
	{
		super(new LoaderInventory(container), index, xPosition, yPosition);
	}

	static class LoaderInventory implements IInventory
	{
		private LoaderContainer container;

		public LoaderInventory(LoaderContainer container)
		{
			this.container = container;
		}

		@Override
		public void clear()
		{
			// NOPE
		}

		@Override
		public int getSizeInventory()
		{
			return 1;
		}

		@Override
		public boolean isEmpty()
		{
			return true;
		}

		@Override
		public ItemStack getStackInSlot(int index)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack decrStackSize(int index, int count)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeStackFromSlot(int index)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack)
		{
			World world = this.container.player.world;
			BlockPos pos = this.container.pos;
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if (block instanceof LoaderBlock)
			{
				((LoaderBlock)block).insertItem(stack, world, pos, state);
			}
		}

		@Override
		public void markDirty()
		{
			// NOPE
		}

		@Override
		public boolean isUsableByPlayer(PlayerEntity player)
		{
			return true;
		}

		@Override
		public int count(Item itemIn)
		{
			return 0;
		}

		@Override
		public boolean hasAny(Set<Item> set)
		{
			return false;
		}
	}
}
