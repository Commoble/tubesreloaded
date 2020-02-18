package com.github.commoble.tubesreloaded.common.blocks.distributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class DistributorTileEntity extends TileEntity
{
	public static final String NEXT_DIRECTIONS = "next_directions";
	
	// handler instances for internal use
	// index of array represents the face of this tile that items are inserted into
	protected final DistributorItemHandler[] handlers = IntStream.range(0, 6)
		.mapToObj(i -> new DistributorItemHandler(this, Direction.byIndex(i)))
		.toArray(DistributorItemHandler[]::new);
	
	// optional instances for public use
	// these will last for the lifetime of the TileEntity and be invalidated when the TE is removed
	public final List<LazyOptional<DistributorItemHandler>> handlerOptionals = Arrays.stream(this.handlers)
		.map(handler -> LazyOptional.of(() -> handler))
		.collect(Collectors.toCollection(ArrayList::new));
	
	public Direction nextSide = Direction.DOWN;
	
	public DistributorTileEntity()
	{
		super(TileEntityRegistrar.DISTRIBUTOR);
	}
	
	public List<LazyOptional<DistributorItemHandler>> initItemHandlers()
	{
		return IntStream.range(0, 6)
			.mapToObj(i -> LazyOptional.of(() -> new DistributorItemHandler(this, Direction.byIndex(i))))
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	@Override
	public void remove()
	{
		this.handlerOptionals.forEach(LazyOptional::invalidate);
		super.remove();
	}
	
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return this.handlerOptionals.get(side.getIndex()).cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt)
	{
		CompoundNBT data = super.write(nbt);
		data.putIntArray(NEXT_DIRECTIONS, IntStream.range(0, 6)
			.map(i-> this.handlers[i].getNextDirectionIndex())
			.toArray());
		return data;
	}
	
	@Override
	public void read(CompoundNBT nbt)
	{
		int[] directionIndices = nbt.getIntArray(NEXT_DIRECTIONS);
		int maxSize = Math.max(this.handlers.length, directionIndices.length);
		for (int i=0; i<maxSize; i++)
		{
			this.handlers[i].setNextDirectionIndex(directionIndices[i]);
		}
		super.read(nbt);
	}
}
