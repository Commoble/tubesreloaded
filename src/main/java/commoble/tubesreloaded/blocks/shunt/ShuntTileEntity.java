package commoble.tubesreloaded.blocks.shunt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import commoble.tubesreloaded.registry.TileEntityRegistrar;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class ShuntTileEntity extends TileEntity
{
	public LazyOptional<ShuntItemHandler> output_handler = this.getItemHandler(false);
	public LazyOptional<ShuntItemHandler> input_handler = this.getItemHandler(true);

	public ShuntTileEntity(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	public ShuntTileEntity()
	{
		this(TileEntityRegistrar.SHUNT);
	}
	
	public LazyOptional<ShuntItemHandler> getItemHandler(boolean canInsertItems)
	{
		return LazyOptional.of(() -> new ShuntItemHandler(this, canInsertItems));
	}
	
	@Override
	public void invalidateCaps()
	{
		this.output_handler.invalidate();
		this.input_handler.invalidate();
		super.invalidateCaps();
	}
	
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			Direction output_dir = this.getBlockState().get(ShuntBlock.FACING);
			return side == output_dir ? this.output_handler.cast() : this.input_handler.cast();
		}
		return super.getCapability(cap, side);
	}
}
