package com.github.commoble.tubesreloaded.common.brasstube;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.tubesreloaded.common.registry.BlockRegistrar;
import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityBrassTube extends TileEntity
{
	public static final String DIST_NBT_KEY = "distance";
	public static final String INV_NBT_KEY = "inventory";

	protected List<ItemInTubeWrapper> inventory = new LinkedList<ItemInTubeWrapper>();
	protected TubeInventoryHandler inventoryHandler = new TubeInventoryHandler(this); // extends ItemStackHandler
	protected LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> inventoryHandler);

	public int distanceToNearestInventory = Integer.MAX_VALUE;

	public TileEntityBrassTube(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
		// TODO Auto-generated constructor stub
	}

	public TileEntityBrassTube()
	{
		super(TileEntityRegistrar.TE_TYPE_BRASS_TUBE);
	}

	/**** State behavior ****/

	public void checkStateAndMaybeUpdate()
	{
		int newDist = Integer.MAX_VALUE;
		for (EnumFacing face : EnumFacing.values())
		{
			TileEntity neighborTE = this.world.getTileEntity(this.pos.offset(face));
			if (neighborTE instanceof TileEntityBrassTube)
			{
				TileEntityBrassTube tube = (TileEntityBrassTube) neighborTE;
				if (tube.distanceToNearestInventory < Integer.MAX_VALUE
						&& tube.distanceToNearestInventory + 1 < newDist)
				{
					newDist = tube.distanceToNearestInventory + 1;
				}
			}

			else if (neighborTE != null)
			{
				LazyOptional<IItemHandler> cap = neighborTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
						face);
				IItemHandler handler = cap.orElse(null);
				if (handler != null)
				{
					if (TileEntityBrassTube.isSpaceForAnythingInItemHandler(handler))
					{
						newDist = 0;
						break;
					}
				}
			}
		}
		if (newDist != this.distanceToNearestInventory)
		{
			this.distanceToNearestInventory = newDist;
			this.markStateUpdated();
		}
	}

	public void markStateUpdated()
	{
		this.markDirty();
		this.world.notifyNeighborsOfStateChange(pos, BlockRegistrar.BRASS_TUBE);
	}

	/**** Inventory handling ****/

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return inventoryHolder.cast();
		}
		return super.getCapability(cap, side);
	}

	public ItemStack enqueueItemStack(ItemStack stack)
	{
		this.inventory.add(new ItemInTubeWrapper(stack, this.pos));
		return stack;
	}

	public void dropItems()
	{
		for (ItemInTubeWrapper wrapper : this.inventory)
		{
			InventoryHelper.spawnItemStack(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
					wrapper.stack);
		}
	}

	public static boolean isSpaceForAnythingInItemHandler(IItemHandler handler)
	{
		return true;
	}

	/**** NBT and synchronization ****/

	@Override
	public void read(NBTTagCompound compound)
	{
		super.read(compound);
		this.distanceToNearestInventory = compound.getInt(DIST_NBT_KEY);
		NBTTagList invList = compound.getList(INV_NBT_KEY, 10);
		List<ItemInTubeWrapper> inventory = new LinkedList<ItemInTubeWrapper>();
		for (int i = 0; i < invList.size(); i++)
		{
			NBTTagCompound itemTag = invList.getCompound(i);
			inventory.add(ItemInTubeWrapper.readFromNBT(itemTag));
		}
		this.inventory = inventory;
	}

	@Override
	public NBTTagCompound write(NBTTagCompound compound)
	{
		compound.setInt(DIST_NBT_KEY, this.distanceToNearestInventory);

		NBTTagList invList = new NBTTagList();

		for (ItemInTubeWrapper wrapper : this.inventory)
		{
			// empty itemstacks are not added to the tube
			NBTTagCompound invTag = new NBTTagCompound();
			wrapper.writeToNBT(invTag);
			invList.add((INBTBase) invTag);
		}
		if (!invList.isEmpty())
		{
			compound.setTag(INV_NBT_KEY, invList);
		}

		return super.write(compound);
	}

	/**
	 * Get an NBT compound to sync to the client with SPacketChunkData, used for
	 * initial loading of the chunk or when many blocks change at once. This
	 * compound comes back to you clientside in {@link handleUpdateTag}
	 */
	@Override
	public NBTTagCompound getUpdateTag()
	{
		return write(new NBTTagCompound());
	}

	/**
	 * Prepare a packet to sync TE to client This method as-is sends the entire NBT
	 * data in the packet Consider overriding to whittle the packet down if TE data
	 * is large
	 */
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.write(nbt);
		return new SPacketUpdateTileEntity(getPos(), 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
	{
		this.read(packet.getNbtCompound());
	}
}
