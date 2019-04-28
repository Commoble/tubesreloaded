package com.github.commoble.tubesreloaded.common.brasstube;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;
import com.github.commoble.tubesreloaded.common.routing.ItemFinderMap;

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
	//public static final String DIST_NBT_KEY = "distance";
	public static final String INV_NBT_KEY = "inventory";

	public ItemFinderMap finderMap = new ItemFinderMap();
	
	protected List<ItemInTubeWrapper> inventory = new LinkedList<ItemInTubeWrapper>();
	protected TubeInventoryHandler inventoryHandler = new TubeInventoryHandler(this); // extends ItemStackHandler
	protected LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> inventoryHandler);

	public TileEntityBrassTube(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	public TileEntityBrassTube()
	{
		this(TileEntityRegistrar.TE_TYPE_BRASS_TUBE);
	}

	/**** Event Handling ****/

	

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

	public boolean isAnyInventoryAdjacent()
	{
		for (EnumFacing face : EnumFacing.values())
		{
			TileEntity te = this.world.getTileEntity(pos.offset(face));
			if (te != null && !(te instanceof TileEntityBrassTube))
			{
				// if a nearby inventory that is not a tube exists
				LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
						face.getOpposite());
				IItemHandler handler = cap.orElse(null);
				if (handler != null)
				{
					if (TileEntityBrassTube.isSpaceForAnythingInItemHandler(handler))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	/**** NBT and synchronization ****/

	@Override
	public void read(NBTTagCompound compound)
	{
		super.read(compound);
		//this.distanceToNearestInventory = compound.getInt(DIST_NBT_KEY);
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
		//compound.setInt(DIST_NBT_KEY, this.distanceToNearestInventory);

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
	 *  // handleUpdateTag just calls read by default
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
