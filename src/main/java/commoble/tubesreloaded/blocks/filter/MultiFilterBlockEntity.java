package commoble.tubesreloaded.blocks.filter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import commoble.tubesreloaded.TubesReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class MultiFilterBlockEntity extends AbstractFilterBlockEntity
{
	public final SetItemHandler inventory = new SetItemHandler(27) {
		@Override
		protected void onContentsChanged(int slot)
		{
			super.onContentsChanged(slot);
			MultiFilterBlockEntity.this.setChanged();
		}
	};
	public final IItemHandler shuntingHandler = new FilterShuntingItemHandler(this); 
	public final LazyOptional<IItemHandler> shuntingOptional = LazyOptional.of(() -> this.shuntingHandler);
	
	public MultiFilterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public MultiFilterBlockEntity(BlockPos pos, BlockState state)
	{
		this(TubesReloaded.get().multiFilterEntity.get(), pos, state);
	}

	@Override
	public void invalidateCaps()
	{
		this.shuntingOptional.invalidate();
		super.invalidateCaps();
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == ForgeCapabilities.ITEM_HANDLER && this.getBlockState().hasProperty(DirectionalBlock.FACING))
		{
			Direction outputDir = this.getBlockState().getValue(DirectionalBlock.FACING);
			if (side == outputDir.getOpposite())
			{
				return this.shuntingOptional.cast();
			}
		}
		return super.getCapability(cap, side);
	}

	@Override
	public boolean canItemPassThroughFilter(ItemStack stack)
	{
		return this.inventory.getSet().contains(stack.getItem());
	}

	@Override
	public void dropItems()
	{
		int slots = this.inventory.getSlots();
		BlockPos pos = this.getBlockPos();
		for (int i=0; i<slots; i++)
		{
			Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.inventory.getStackInSlot(i));
		}
	}

	@Override
	public void load(CompoundTag tag)
	{
		super.load(tag);
		this.inventory.deserializeNBT(tag.getCompound(INV_KEY));
	}

	@Override
	protected void saveAdditional(CompoundTag tag)
	{
		super.saveAdditional(tag);
		tag.put(INV_KEY, this.inventory.serializeNBT());
	}
}
