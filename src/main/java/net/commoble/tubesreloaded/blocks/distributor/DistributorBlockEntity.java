package net.commoble.tubesreloaded.blocks.distributor;

import java.util.stream.IntStream;

import javax.annotation.Nullable;

import net.commoble.tubesreloaded.TubesReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class DistributorBlockEntity extends BlockEntity
{
	public static final String NEXT_DIRECTIONS = "next_directions";
	
	// handler instances for internal use
	// index of array represents the face of this tile that items are inserted into
	protected final DistributorItemHandler[] handlers = IntStream.range(0, 6)
		.mapToObj(i -> new DistributorItemHandler(this, Direction.from3DDataValue(i)))
		.toArray(DistributorItemHandler[]::new);
	
	public Direction nextSide = Direction.DOWN;
	
	public DistributorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}
	
	public DistributorBlockEntity(BlockPos pos, BlockState state)
	{
		this(TubesReloaded.get().distributorEntity.get(), pos, state);
	}
	
	public IItemHandler getItemHandler(@Nullable Direction side)
	{
		return side == null ? null : this.handlers[side.ordinal()];
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
