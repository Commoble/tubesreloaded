package com.github.commoble.tubesreloaded.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.github.commoble.tubesreloaded.common.brasstube.BrassTubeTileEntity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class WorldHelper
{
	public static List<BrassTubeTileEntity> getTubesAdjacentTo(World world, BlockPos pos)
	{
		List<BrassTubeTileEntity> tes = new ArrayList<BrassTubeTileEntity>(6);
		for (Direction face : Direction.values())
		{
			BlockPos checkPos = pos.offset(face);
			TileEntity te = world.getTileEntity(checkPos);
			if (te instanceof BrassTubeTileEntity)
			{
				tes.add((BrassTubeTileEntity)te);
			}
		}
		
		return tes;
	}
	
	public static Stream<BrassTubeTileEntity> getBlockPositionsAsTubeTileEntities(World world, Collection<BlockPos> posCollection)
	{
		Stream<TileEntity> teStream = posCollection.stream().map(tubePos -> world.getTileEntity(tubePos));
		Stream<TileEntity> filteredStream = teStream.filter(te -> te instanceof BrassTubeTileEntity);
		return filteredStream.map(te -> (BrassTubeTileEntity) te);
	}
	
	public static LazyOptional<TileEntity> getTileEntityAt(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		return LazyOptional.of(te != null ? () -> te : null);
	}
	
	public static LazyOptional<IItemHandler> getTEItemHandlerAt(World world, BlockPos pos, Direction faceOfBlockPos)
	{
		TileEntity te = world.getTileEntity(pos);
		
		return te != null ? te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, faceOfBlockPos) : LazyOptional.empty();
	}
}
