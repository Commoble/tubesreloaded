package com.github.commoble.tubesreloaded.blocks.tube;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.tubesreloaded.TubesReloadedMod;
import com.github.commoble.tubesreloaded.registry.TileEntityRegistrar;
import com.github.commoble.tubesreloaded.routing.Route;
import com.github.commoble.tubesreloaded.routing.RoutingNetwork;
import com.github.commoble.tubesreloaded.util.WorldHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
	
	@SuppressWarnings("unchecked")
	protected final LazyOptional<IItemHandler>[] handlerOptionals = Arrays.stream(this.inventoryHandlers)
		.map(handler -> LazyOptional.of(() -> handler))
		.toArray(size -> (LazyOptional<IItemHandler>[])new LazyOptional[size]);
	
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
		this(TileEntityRegistrar.TUBE);
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
	
	public boolean isTubeCompatible (TubeTileEntity tube)
	{
		Block thisBlock = this.getBlockState().getBlock();
		Block otherBlock = tube.getBlockState().getBlock();
		if (thisBlock instanceof TubeBlock && otherBlock instanceof TubeBlock)
		{
			return ((TubeBlock)thisBlock).isTubeCompatible((TubeBlock)otherBlock);
		}
		return false;
	}
	
	public List<Direction> getConnectedDirections()
	{
		BlockState state = this.getBlockState();
		return TubeBlock.getConnectedDirections(state);
	}
	
	public static Optional<TubeTileEntity> getTubeTEAt(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		return Optional.ofNullable(te instanceof TubeTileEntity ? (TubeTileEntity)te : null);
	}
	
	// insertionSide is the side of this block the item was inserted from
	public Route getBestRoute(Direction insertionSide, ItemStack stack)
	{
		return this.getNetwork().getBestRoute(this.world, this.pos, insertionSide, stack);
	}

	/**** Event Handling ****/
	
	@Override
	public void remove()
	{
		this.dropItems();
		this.network.invalid = true;
		Arrays.stream(this.handlerOptionals).forEach(optional -> optional.invalidate());
		super.remove();
	}
	
	public void onPossibleNetworkUpdateRequired()
	{
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
			if (this.getNetwork().contains(this.pos, face.getOpposite()) != this.getNetwork().isValidToBeInNetwork(checkPos, this.world, face.getOpposite()))
				return true;
		}
		return false;
	}
	
	@Override
	public void tick()
	{
		this.merge_buffer();
		if (!this.inventory.isEmpty())	// if inventory is empty, skip the tick
		{
			if (!this.world.isRemote)	// block has changes that need to be saved (serverside)
			{
				this.markDirty();
			}
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
		if (!this.world.isRemote && this.inventory.size() > TubesReloadedMod.config.max_items_in_tube.get())
		{
			this.world.removeBlock(this.pos, false);
		}
	}
	
	public void sendWrapperOnward(ItemInTubeWrapper wrapper)
	{
		if (!wrapper.remainingMoves.isEmpty())	// wrapper has remaining moves
		{
			Direction dir = wrapper.remainingMoves.poll();
			TileEntity te = this.world.getTileEntity(this.pos.offset(dir));
			if (te instanceof TubeTileEntity && this.isTubeCompatible((TubeTileEntity)te)) // te exists and is a valid tube
			{
				((TubeTileEntity)te).enqueueItemStack(wrapper.stack, wrapper.remainingMoves, wrapper.maximumDurationInTube);
			}
			else if (!this.world.isRemote)
			{
				if (te != null)	// te exists but is not a tube
				{
					ItemStack remaining = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()).map(handler -> WorldHelper.disperseItemToHandler(wrapper.stack, handler)).orElse(wrapper.stack.copy());
	
					if (!remaining.isEmpty())	// target inventory filled up unexpectedly
					{
						ItemStack unenqueueableItems = this.enqueueItemStack(remaining, dir, false); // attempt to re-enqueue the item on that side
						WorldHelper.ejectItemstack(this.world, this.pos, dir, unenqueueableItems);	// eject items if we can't
					}
				}
				else	// no TE -- eject stack
				{
					WorldHelper.ejectItemstack(this.world, this.pos, dir, wrapper.stack);
				}
			}
		}
		else if (!this.world.isRemote)	// wrapper has no remaining moves -- this isn't expected, eject the item
		{
			WorldHelper.ejectItemstack(this.world, this.pos, null, wrapper.stack);
		}
	}
	/**** Inventory handling ****/

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side != null)
		{
			return this.handlerOptionals[side.getIndex()].cast();	// T is <IItemHandler> here, which our handler implements
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
		return this.enqueueItemStack(new ItemInTubeWrapper(stack, remainingMoves, ticksPerTube));
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
			TileEntity te = this.world.getTileEntity(this.pos.offset(face));
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

		ListNBT invList = new ListNBT();
		this.merge_buffer();

		for (ItemInTubeWrapper wrapper : this.inventory)
		{
			CompoundNBT invTag = new CompoundNBT();
			wrapper.writeToNBT(invTag);
			invList.add(invTag);
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
		return this.write(new CompoundNBT());	// okay to send entire inventory on chunk load
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
			invList.add(invTag);
		}
		if (!invList.isEmpty())
		{
			nbt.put(INV_NBT_KEY_ADD, invList);
		}
		
		return new SUpdateTileEntityPacket(this.getPos(), 1, nbt);
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
