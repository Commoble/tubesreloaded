package com.github.commoble.tubesreloaded.common.brasstube;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;
import com.github.commoble.tubesreloaded.common.routing.Route;
import com.github.commoble.tubesreloaded.common.routing.RoutingNetwork;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityBrassTube extends TileEntity
{
	// public static final String DIST_NBT_KEY = "distance";
	public static final String INV_NBT_KEY = "inventory";

	protected List<ItemInTubeWrapper> inventory = new LinkedList<ItemInTubeWrapper>();
	protected TubeInventoryHandler inventoryHandler = new TubeInventoryHandler(this); // extends ItemStackHandler
	protected LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> inventoryHandler);
	
	@Nonnull	// use getNetwork()
	private RoutingNetwork network = RoutingNetwork.INVALID_NETWORK;

	public TileEntityBrassTube(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	public TileEntityBrassTube()
	{
		this(TileEntityRegistrar.TE_TYPE_BRASS_TUBE);
	}
	
	/**** Getters and Setters	****/
	public RoutingNetwork getNetwork()
	{
		if (this.network.invalid)
		{
			this.network = RoutingNetwork.buildNetworkFrom(this.pos, this.world);
		}
		return this.network;
	}
	
	public void setNetwork(RoutingNetwork network)
	{
		this.network = network;
	}
	
	// insertionSide is the side of this block the item was inserted from
	public Route getBestRoute(Direction insertionSide, ItemStack stack)
	{
		return this.getNetwork().getBestRoute(this.world, this.pos, insertionSide, stack);
	}

	/**** Event Handling ****/
	
	public void onPossibleNetworkUpdateRequired()
	{
		RoutingNetwork newNetwork = RoutingNetwork.buildNetworkFrom(this.pos, this.world);
		if (this.network.invalid || !newNetwork.equals(this.network))
		{	// if the existing network has been invalidated or changed,
			// use the new network, and invalidate the old network
			// additionally, make sure all tubes in the new network are using the new network
			// 	(to reduce the amount of network building that must be done)
			// and invalidate the old network (in case it was changed)
			this.network.invalid = true;
			this.network = newNetwork;
			newNetwork.confirmAllTubes(this.world);
		}
	}

	/**** Inventory handling ****/

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
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
		for (Direction face : Direction.values())
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

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		// this.distanceToNearestInventory = compound.getInt(DIST_NBT_KEY);
		ListNBT invList = compound.getList(INV_NBT_KEY, 10);
		List<ItemInTubeWrapper> inventory = new LinkedList<ItemInTubeWrapper>();
		for (int i = 0; i < invList.size(); i++)
		{
			CompoundNBT itemTag = invList.getCompound(i);
			inventory.add(ItemInTubeWrapper.readFromNBT(itemTag));
		}
		this.inventory = inventory;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		// compound.setInt(DIST_NBT_KEY, this.distanceToNearestInventory);

		ListNBT invList = new ListNBT();

		for (ItemInTubeWrapper wrapper : this.inventory)
		{
			// empty itemstacks are not added to the tube
			CompoundNBT invTag = new CompoundNBT();
			wrapper.writeToNBT(invTag);
			invList.add((INBT) invTag);
		}
		if (!invList.isEmpty())
		{
			compound.put(INV_NBT_KEY, invList);
		}

		return super.write(compound);
	}

	/**
	 * Get an NBT compound to sync to the client with SPacketChunkData, used for
	 * initial loading of the chunk or when many blocks change at once. This
	 * compound comes back to you clientside in {@link handleUpdateTag}
	 * //handleUpdateTag just calls read by default
	 */
	@Override
	public CompoundNBT getUpdateTag()
	{
		return write(new CompoundNBT());
	}

	/**
	 * Prepare a packet to sync TE to client This method as-is sends the entire NBT
	 * data in the packet Consider overriding to whittle the packet down if TE data
	 * is large
	 */
	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		CompoundNBT nbt = new CompoundNBT();
		this.write(nbt);
		return new SUpdateTileEntityPacket(getPos(), 1, nbt);
	}

	/**
	 * Receive packet on client and get data out of it
	 */
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
	{
		this.read(packet.getNbtCompound());
	}
}
