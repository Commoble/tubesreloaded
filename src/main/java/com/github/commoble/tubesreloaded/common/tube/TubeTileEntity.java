package com.github.commoble.tubesreloaded.common.tube;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.tubesreloaded.common.ConfigValues;
import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;
import com.github.commoble.tubesreloaded.common.routing.Endpoint;
import com.github.commoble.tubesreloaded.common.routing.Route;
import com.github.commoble.tubesreloaded.common.routing.RoutingNetwork;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TubeTileEntity extends TileEntity implements ITickableTileEntity
{
	// public static final String DIST_NBT_KEY = "distance";
	public static final String INV_NBT_KEY_ADD = "inventory_new_items";
	public static final String INV_NBT_KEY_RESET = "inventory_reset";

	@Nonnull
	public Queue<ItemInTubeWrapper> inventory = new LinkedList<ItemInTubeWrapper>();
	
	protected final TubeInventoryHandler[] inventoryHandlers = Arrays.stream(Direction.values())
			.map(dir -> new TubeInventoryHandler(this, dir))
			.toArray(TubeInventoryHandler[]::new);	// one handler for each direction
	
	private Queue<ItemInTubeWrapper> wrappers_to_send_to_client = new LinkedList<ItemInTubeWrapper>();
	public Queue<ItemInTubeWrapper> incoming_wrapper_buffer = new LinkedList<ItemInTubeWrapper>();
	
	@Nonnull	// use getNetwork()
	private RoutingNetwork network = RoutingNetwork.INVALID_NETWORK;

	public TubeTileEntity(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	public TubeTileEntity()
	{
		this(TileEntityRegistrar.TE_TYPE_TUBE);
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
	
	public static LazyOptional<TubeTileEntity> getTubeTEAt(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		return LazyOptional.of(te instanceof TubeTileEntity ? () -> (TubeTileEntity)te : null);
	}
	
	// insertionSide is the side of this block the item was inserted from
	public Route getBestRoute(Direction insertionSide, ItemStack stack)
	{
		return this.getNetwork().getBestRoute(this.world, this.pos, insertionSide, stack);
	}

	/**** Event Handling ****/
	
	public void onPossibleNetworkUpdateRequired()
	{
		//RoutingNetwork newNetwork = RoutingNetwork.buildNetworkFrom(this.pos, this.world);
//		if (this.network.invalid || this.didNetworkChange())
//		{	// if the existing network has been invalidated or changed,
//			// use the new network, and invalidate the old network
//			// additionally, make sure all tubes in the new network are using the new network
//			// 	(to reduce the amount of network building that must be done)
//			// and invalidate the old network (in case it was changed)
//			this.network.invalid = true;
//			this.network = RoutingNetwork.buildNetworkFrom(this.pos, this.world);
//			this.network.confirmAllTubes(this.world);
//		}
		if (!this.network.invalid && this.didNetworkChange())
		{
			this.network.invalid = true;
		}
	}
	
	private boolean didNetworkChange()
	{
		for (Direction face : Direction.values())
		{
			BlockPos checkPos = this.pos.offset(face);
			// if the adjacent block is a tube or endpoint but isn't in the network
			// OR if the adjacent block is in the network but isn't a tube or endpoint
			// then the network changed
			if (this.getNetwork().contains(pos, face.getOpposite()) != this.getNetwork().isValidToBeInNetwork(checkPos, world, face.getOpposite()))
				return true;
		}
		return false;
	}
	
	public void tick()
	{
		this.merge_buffer();
		if (!this.inventory.isEmpty())	// if inventory is empty, skip the tick
		{
			if (!this.world.isRemote)	// block has changes that need to be saved (serverside)
			{
				this.markDirty();
			}
			//world.createExplosion(null, pos.getX() + 0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, 1F, Explosion.Mode.NONE);
			Queue<ItemInTubeWrapper> remainingWrappers = new LinkedList<ItemInTubeWrapper>();
			for (ItemInTubeWrapper wrapper : this.inventory)
			{
				wrapper.ticksElapsed++;
				if (wrapper.ticksElapsed >= wrapper.maximumDurationInTube)
				{
					if (wrapper.freshlyInserted)
					{
						wrapper.freshlyInserted = false;
						wrapper.remainingMoves.removeFirst();
						wrapper.ticksElapsed = 0;
						remainingWrappers.add(wrapper);
					}
					else
					{
						this.sendWrapperOnward(wrapper);
					}
				}
				else
				{
					remainingWrappers.add(wrapper);
				}
			}
			this.inventory = remainingWrappers;
		}
	}
	
	public void sendWrapperOnward(ItemInTubeWrapper wrapper)
	{
		if (!wrapper.remainingMoves.isEmpty())	// wrapper has remaining moves
		{
			Direction dir = wrapper.remainingMoves.poll();
			TileEntity te = this.world.getTileEntity(this.pos.offset(dir));
			if (te instanceof TubeTileEntity) // te exists and is a tube
			{
				((TubeTileEntity) te).enqueueItemStack(wrapper.stack, wrapper.remainingMoves, wrapper.maximumDurationInTube);
			}
			else if (te != null && !world.isRemote)	// te exists but is not a tube
			{
				ItemStack remaining = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()).map(handler -> Endpoint.disperseItemToHandler(wrapper.stack, handler)).orElse(ItemStack.EMPTY);

				if (!remaining.isEmpty())	// target inventory filled up unexpectedly
				{
					this.enqueueItemStack(remaining, dir.getOpposite(), false);
				}
			}
		}
		else if (!world.isRemote)	// wrapper has no remaining moves -- this isn't expected, eject the item
		{
			this.ejectItem(wrapper.stack);
		}
	}
	
	private void ejectItem(ItemStack stack)
	{
        ItemEntity itementity = new ItemEntity(this.world, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, stack);
        itementity.setDefaultPickupDelay();
        this.world.addEntity(itementity);
	}

	/**** Inventory handling ****/

	@SuppressWarnings("unchecked")
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return LazyOptional.of(() -> (T)inventoryHandlers[side.getIndex()]);	// T is <IItemHandler> here, which our handler implements
		}
		return super.getCapability(cap, side);
	}

	// insert a new itemstack into the tube network from a direction
	// and determine a route for it
	public ItemStack enqueueItemStack(ItemStack stack, Direction face, boolean simulate)
	{
		Route route = this.getNetwork().getBestRoute(this.world, this.pos, face, stack);
		if (route == null || route.sequenceOfMoves.isEmpty())
			return stack.copy();
			
		if (simulate)
			return ItemStack.EMPTY;
		
		int ticks_per_tube = this.getNetwork().getTicksPerTube();
		this.wrappers_to_send_to_client.add(new ItemInTubeWrapper(stack, route.sequenceOfMoves, ticks_per_tube, face.getOpposite()));

		this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
		
		return this.enqueueItemStack(new ItemInTubeWrapper(stack, route.sequenceOfMoves, 10, face.getOpposite()));
	}
	
	public ItemStack enqueueItemStack(ItemInTubeWrapper wrapper)
	{
		this.incoming_wrapper_buffer.add(wrapper);
		return ItemStack.EMPTY;
	}

	public ItemStack enqueueItemStack(ItemStack stack, Queue<Direction> remainingMoves, int ticksPerTube)
	{
		return enqueueItemStack(new ItemInTubeWrapper(stack, remainingMoves, ticksPerTube));
	}
	
	public void merge_buffer()
	{
		if (!this.incoming_wrapper_buffer.isEmpty())
		{
			for (ItemInTubeWrapper wrapper : this.incoming_wrapper_buffer)
			{
				this.inventory.add(wrapper);
			}
			this.incoming_wrapper_buffer = new LinkedList<ItemInTubeWrapper>();
		}
	}

	public void dropItems()
	{
		this.merge_buffer();
		for (ItemInTubeWrapper wrapper : this.inventory)
		{
			InventoryHelper.spawnItemStack(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
					wrapper.stack);
		}

		this.inventory = new LinkedList<ItemInTubeWrapper>();	// clear it in case this is being called without destroying the TE
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
			if (te != null && !(te instanceof TubeTileEntity))
			{
				// if a nearby inventory that is not a tube exists
				LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
						face.getOpposite());
				IItemHandler handler = cap.orElse(null);
				if (handler != null)
				{
					if (TubeTileEntity.isSpaceForAnythingInItemHandler(handler))
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
		if (compound.contains(INV_NBT_KEY_RESET))	// only update inventory if the compound has an inv. key
		{									// this lets the client receive packets without the inventory being cleared
			ListNBT invList = compound.getList(INV_NBT_KEY_RESET, 10);
			Queue<ItemInTubeWrapper> inventory = new LinkedList<ItemInTubeWrapper>();
			for (int i = 0; i < invList.size(); i++)
			{
				CompoundNBT itemTag = invList.getCompound(i);
				inventory.add(ItemInTubeWrapper.readFromNBT(itemTag));
			}
			this.inventory = inventory;
		}
		else if (compound.contains(INV_NBT_KEY_ADD))	// add newly inserted items to this tube
		{
			ListNBT invList = compound.getList(INV_NBT_KEY_ADD, 10);
			for (int i=0; i<invList.size(); i++)
			{
				CompoundNBT itemTag = invList.getCompound(i);
				this.inventory.add(ItemInTubeWrapper.readFromNBT(itemTag));
			}
		}
	}

	@Override	// write entire inventory by default (for server -> hard disk purposes this is what is called)
	public CompoundNBT write(CompoundNBT compound)
	{
		// compound.setInt(DIST_NBT_KEY, this.distanceToNearestInventory);
		ListNBT invList = new ListNBT();
		this.merge_buffer();

		for (ItemInTubeWrapper wrapper : this.inventory)
		{
			// empty itemstacks are not added to the tube
			CompoundNBT invTag = new CompoundNBT();
			wrapper.writeToNBT(invTag);
			invList.add((INBT) invTag);
		}
		if (!invList.isEmpty())
		{
			compound.put(INV_NBT_KEY_RESET, invList);
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
		return write(new CompoundNBT());	// okay to send entire inventory on chunk load
	}

	/**
	 * Prepare a packet to sync TE to client
	 * We don't need to send the inventory in every packet
	 * but we should notify the client of new items entering the network
	 */
	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		CompoundNBT nbt = new CompoundNBT();
		super.write(nbt); // write the basic TE stuff

		ListNBT invList = new ListNBT();

		while (!this.wrappers_to_send_to_client.isEmpty())
		{
			// empty itemstacks are not added to the tube
			ItemInTubeWrapper wrapper = this.wrappers_to_send_to_client.poll();
			CompoundNBT invTag = new CompoundNBT();
			wrapper.writeToNBT(invTag);
			invList.add((INBT) invTag);
		}
		if (!invList.isEmpty())
		{
			nbt.put(INV_NBT_KEY_ADD, invList);
		}
		
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
