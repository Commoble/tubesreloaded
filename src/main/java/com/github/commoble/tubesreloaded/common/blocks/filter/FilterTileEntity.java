package com.github.commoble.tubesreloaded.common.blocks.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.tubesreloaded.common.blocks.shunt.ShuntBlock;
import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;
import com.github.commoble.tubesreloaded.common.util.WorldHelper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class FilterTileEntity extends TileEntity
{
	public static final String INV_KEY = "inventory";
	
	public ItemStack filterStack = ItemStack.EMPTY;
	public FilterShuntingItemHandler shuntingHandler = new FilterShuntingItemHandler(this);
	public FilterStorageItemHandler storageHandler = new FilterStorageItemHandler(this);
	private LazyOptional<IItemHandler> shuntingOptional = LazyOptional.of(() -> this.shuntingHandler);
	private LazyOptional<IItemHandler> storageOptional = LazyOptional.of(() -> this.storageHandler);
	
	public FilterTileEntity(TileEntityType<?> teType)
	{
		super(teType);
	}
	
	public FilterTileEntity()
	{
		this(TileEntityRegistrar.FILTER);
	}
	
	@Override
	public void remove()
	{
		this.shuntingOptional.invalidate();
		this.storageOptional.invalidate();
		super.remove();
	}
	
	public boolean canItemPassThroughFilter(ItemStack stack)
	{
		if (stack.getCount() <= 0)
		{
			return false;
		}
		if (this.filterStack.getCount() <= 0)
		{
			return true;
		}
		
		return this.filterStack.getItem().equals(stack.getItem());
	}
	
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			Direction output_dir = this.getBlockState().get(ShuntBlock.FACING);
			if (side == output_dir.getOpposite())
			{
				return this.shuntingOptional.cast();
			}
			else if (side != output_dir)
			{
				return this.storageOptional.cast();
			}
		}
		return super.getCapability(cap, side);
	}
	
	// returns true if the filter's storage was manipulated, false otherwise (allowing other things to happen like block placement)
	public boolean onActivated(PlayerEntity player, Direction sideOfBlock, ItemStack stackInHand)
	{
		if (stackInHand.getCount() <= 0)
		// empty hand
		{
			if (!this.world.isRemote)
			{
				ItemStack filtered = this.filterStack.copy();
				this.setFilterStackAndSaveAndSync(stackInHand.split(1));
				player.addItemStackToInventory(filtered);
			}
			return true; // because hand is empty so nothing else would happen anyway
			
		}
		else if (stackInHand.getItem().equals(this.filterStack.getItem()))
		{	// item in hand matches filter item, retrieve item from filter
			if (!this.world.isRemote)
			{
				ItemStack filtered = this.filterStack.copy();
				this.setFilterStackAndSaveAndSync(ItemStack.EMPTY);
				if (!player.addItemStackToInventory(filtered))	// attempt to put item in inventory
				{	// if we failed to do that
					WorldHelper.ejectItemstack(this.world, this.pos, sideOfBlock, filtered);
				}
			}
			return true;
		}
		else if (this.filterStack.getCount() <= 0)
		{
			if (!this.world.isRemote)
			{
				this.setFilterStackAndSaveAndSync(stackInHand.split(1));
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void setFilterStackAndSaveAndSync(ItemStack filterStack)
	{
		this.filterStack = filterStack;
		this.markDirty();
		this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
		
	}
	
	public void dropItems()
	{
			InventoryHelper.spawnItemStack(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
					this.filterStack);
	}
	
	////// NBT and syncing
	
	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		CompoundNBT inventory = compound.getCompound(INV_KEY);
		this.filterStack = ItemStack.read(inventory);
	}

	@Override	// write entire inventory by default (for server -> hard disk purposes this is what is called)
	public CompoundNBT write(CompoundNBT compound)
	{
		CompoundNBT inventory = new CompoundNBT();
		this.filterStack.write(inventory);
		compound.put(INV_KEY, inventory);
		return super.write(compound);
	}
	
	@Override
	public CompoundNBT getUpdateTag()
	{
		return this.write(new CompoundNBT());	// okay to send entire inventory on chunk load
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		CompoundNBT nbt = new CompoundNBT();
		this.write(nbt);
		
		return new SUpdateTileEntityPacket(this.getPos(), 1, nbt);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
	{
		this.read(packet.getNbtCompound());
	}
}
