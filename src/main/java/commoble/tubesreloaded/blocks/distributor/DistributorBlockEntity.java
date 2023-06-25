package commoble.tubesreloaded.blocks.distributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import commoble.tubesreloaded.TubesReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

public class DistributorBlockEntity extends BlockEntity
{
	public static final String NEXT_DIRECTIONS = "next_directions";
	
	// handler instances for internal use
	// index of array represents the face of this tile that items are inserted into
	protected final DistributorItemHandler[] handlers = IntStream.range(0, 6)
		.mapToObj(i -> new DistributorItemHandler(this, Direction.from3DDataValue(i)))
		.toArray(DistributorItemHandler[]::new);
	
	// optional instances for public use
	// these will last for the lifetime of the TileEntity and be invalidated when the TE is removed
	public final List<LazyOptional<DistributorItemHandler>> handlerOptionals = Arrays.stream(this.handlers)
		.map(handler -> LazyOptional.of(() -> handler))
		.collect(Collectors.toCollection(ArrayList::new));
	
	public Direction nextSide = Direction.DOWN;
	
	public DistributorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}
	
	public DistributorBlockEntity(BlockPos pos, BlockState state)
	{
		this(TubesReloaded.get().distributorEntity.get(), pos, state);
	}
	
	public List<LazyOptional<DistributorItemHandler>> initItemHandlers()
	{
		return IntStream.range(0, 6)
			.mapToObj(i -> LazyOptional.of(() -> new DistributorItemHandler(this, Direction.from3DDataValue(i))))
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	@Override
	public void invalidateCaps()
	{
		this.handlerOptionals.forEach(LazyOptional::invalidate);
		super.invalidateCaps();
	}
	
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == ForgeCapabilities.ITEM_HANDLER)
		{
			return this.handlerOptionals.get(side.ordinal()).cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putIntArray(NEXT_DIRECTIONS, IntStream.range(0, 6)
			.map(i-> this.handlers[i].getNextDirectionIndex())
			.toArray());
	}
	
	@Override
	public void load(CompoundTag nbt)
	{
		int[] directionIndices = nbt.getIntArray(NEXT_DIRECTIONS);
		int maxSize = Math.min(this.handlers.length, directionIndices.length);
		for (int i=0; i<maxSize; i++)
		{
			this.handlers[i].setNextDirectionIndex(directionIndices[i]);
		}
		super.load(nbt);
	}
}
