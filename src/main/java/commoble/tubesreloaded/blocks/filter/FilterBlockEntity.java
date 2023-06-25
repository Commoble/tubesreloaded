package commoble.tubesreloaded.blocks.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import commoble.tubesreloaded.TubesReloaded;
import commoble.tubesreloaded.blocks.shunt.ShuntBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class FilterBlockEntity extends BlockEntity
{
	public static final String INV_KEY = "inventory";
	
	public ItemStack filterStack = ItemStack.EMPTY;
	public FilterShuntingItemHandler shuntingHandler = new FilterShuntingItemHandler(this);
	public FilterStorageItemHandler storageHandler = new FilterStorageItemHandler(this);
	private LazyOptional<IItemHandler> shuntingOptional = LazyOptional.of(() -> this.shuntingHandler);
	private LazyOptional<IItemHandler> storageOptional = LazyOptional.of(() -> this.storageHandler);
	
	public FilterBlockEntity(BlockEntityType<?> teType, BlockPos pos, BlockState state)
	{
		super(teType, pos, state);
	}
	
	public FilterBlockEntity(BlockPos pos, BlockState state)
	{
		this(TubesReloaded.get().filterEntity.get(), pos, state);
	}
	
	@Override
	public void invalidateCaps()
	{
		this.shuntingOptional.invalidate();
		this.storageOptional.invalidate();
		super.invalidateCaps();
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
		if (cap == ForgeCapabilities.ITEM_HANDLER)
		{
			Direction output_dir = this.getBlockState().getValue(ShuntBlock.FACING);
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
	
	public void setFilterStackAndSaveAndSync(ItemStack filterStack)
	{
		this.filterStack = filterStack;
		this.setChanged();
		BlockState state = this.getBlockState();
		this.getLevel().sendBlockUpdated(this.getBlockPos(), state, state, 2);
		
	}
	
	public void dropItems()
	{
		BlockPos thisPos = this.getBlockPos();
		Containers.dropItemStack(this.getLevel(), thisPos.getX(), thisPos.getY(), thisPos.getZ(), this.filterStack);
	}
	
	////// NBT and syncing
	
	protected void writeData(CompoundTag compound)
	{
		CompoundTag inventory = new CompoundTag();
		this.filterStack.save(inventory);
		compound.put(INV_KEY, inventory);
	}
	
	protected void readData(CompoundTag compound)
	{
		CompoundTag inventory = compound.getCompound(INV_KEY);
		this.filterStack = ItemStack.of(inventory);
	}

	@Override	// write entire inventory by default (for server -> hard disk purposes this is what is called)
	public void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		this.writeData(compound);
	}
	
	@Override
	/** read **/
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.readData(compound);
	}
	
	@Override
	public CompoundTag getUpdateTag()
	{
		CompoundTag tag = super.getUpdateTag();
		this.writeData(tag);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}	
	
	// super.handleUpdateTag() just calls load()
	
	// super.onDataPacket() just calls load()
}
