package commoble.tubesreloaded.blocks.shunt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import commoble.tubesreloaded.TubesReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

public class ShuntBlockEntity extends BlockEntity
{
	public LazyOptional<ShuntItemHandler> outputHandler = this.getItemHandler(false);
	public LazyOptional<ShuntItemHandler> inputHandler = this.getItemHandler(true);

	public ShuntBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state)
	{
		super(tileEntityTypeIn, pos, state);
	}

	public ShuntBlockEntity(BlockPos pos, BlockState state)
	{
		this(TubesReloaded.get().shuntEntity.get(), pos, state);
	}
	
	public LazyOptional<ShuntItemHandler> getItemHandler(boolean canInsertItems)
	{
		return LazyOptional.of(() -> new ShuntItemHandler(this, canInsertItems));
	}
	
	@Override
	public void invalidateCaps()
	{
		this.outputHandler.invalidate();
		this.inputHandler.invalidate();
		super.invalidateCaps();
	}
	
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == ForgeCapabilities.ITEM_HANDLER)
		{
			Direction output_dir = this.getBlockState().getValue(ShuntBlock.FACING);
			return side == output_dir ? this.outputHandler.cast() : this.inputHandler.cast();
		}
		return super.getCapability(cap, side);
	}
}
