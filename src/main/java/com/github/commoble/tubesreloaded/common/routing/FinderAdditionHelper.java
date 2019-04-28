package com.github.commoble.tubesreloaded.common.routing;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.github.commoble.tubesreloaded.common.brasstube.TileEntityBrassTube;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FinderAdditionHelper
{
	public static void onTubePlaced(World world, BlockPos pos)
	{
		ItemFinderMap accumulatedFinderMap = new ItemFinderMap();
		for (EnumFacing face : EnumFacing.values())
		{
			accumulatedFinderMap.mergeFromTE(world, pos.offset(face));
		}
		
		FinderAdditionHelper.propagateAdditiveMapUpdate(world, pos, accumulatedFinderMap, new LinkedList<Pair<BlockPos,ItemFinderMap>>(), new HashSet<BlockPos>());
	}
	
	// check "each" tube in the network, starting from the source tube, using breadth-first search (BFS)
	// each tube is visited only once
	// each tube passes its map to the next tube(s) in the search as a reference
	// compare incoming map to current tube's map
	// if there are any finders in the current tube that can be simplified to newmMapFinder+1, do that
	// if no changes are made, do not continue searching from this tube
	public static void propagateAdditiveMapUpdate(World world, BlockPos pos, ItemFinderMap incomingMap, Queue<Pair<BlockPos, ItemFinderMap>> toSearch, Set<BlockPos> visited)
	{
		visited.add(pos);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBrassTube)
		{
			TileEntityBrassTube tube = (TileEntityBrassTube)te;
			
			if (tube.finderMap.mergeFromFinderMap(incomingMap))
			{	// if change occurred in tube, continue searching
				for (EnumFacing face : EnumFacing.values())
				{
					BlockPos nextPos = pos.offset(face);
					if (!visited.contains(nextPos));
					{
						toSearch.add(new ImmutablePair<BlockPos,ItemFinderMap>(pos.offset(face), tube.finderMap));
					}
				}
			}
			
		}
		if (!toSearch.isEmpty())
		{
			Pair<BlockPos,ItemFinderMap> pair = toSearch.remove();
			propagateAdditiveMapUpdate(world, pair.getLeft(), pair.getRight(), toSearch, visited);
		}
	}
}
