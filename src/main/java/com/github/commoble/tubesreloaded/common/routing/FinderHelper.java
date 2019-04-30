package com.github.commoble.tubesreloaded.common.routing;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.github.commoble.tubesreloaded.common.brasstube.TileEntityBrassTube;
import com.github.commoble.tubesreloaded.common.util.WorldHelper;

import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class FinderHelper
{
	private static void initiatePositivePulse(World world, BlockPos pos)
	{
		//temFinderMap accumulatedFinderMap = new ItemFinderMap();
		
		//FinderHelper.propagatePositivePulse(world, pos, accumulatedFinderMap, new LinkedList<Pair<BlockPos,ItemFinderMap>>(), new HashSet<BlockPos>());
		Queue<Pair<BlockPos, ItemFinderMap>> toSearch = new LinkedList<Pair<BlockPos,ItemFinderMap>>();
		Set<BlockPos> visited = new HashSet<BlockPos>();
		
		System.out.println("initiating positive pulse at " + pos.toString());
		visited.add(pos);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBrassTube)
		{
			TileEntityBrassTube tube = (TileEntityBrassTube)te;
			//boolean changeOccurred = false;

			for (EnumFacing face : EnumFacing.values())
			{
				tube.finderMap.mergeFromTE(world, pos.offset(face));
			}
			
			// initiator always attempts to pulse neighbors in any case
			for (EnumFacing face : EnumFacing.values())
			{
				BlockPos nextPos = pos.offset(face);
				if (!visited.contains(nextPos));
				{
					toSearch.add(new ImmutablePair<BlockPos,ItemFinderMap>(pos.offset(face), tube.finderMap));
				}
			}
			
		}
		if (!toSearch.isEmpty())
		{
			Pair<BlockPos,ItemFinderMap> pair = toSearch.remove();
			propagatePositivePulse(world, pair.getLeft(), pair.getRight(), toSearch, visited);
		}
	}
	
	// check "each" tube in the network, starting from the source tube, using breadth-first search (BFS)
	// each tube is visited only once
	// each tube passes its map to the next tube(s) in the search as a reference
	// compare incoming map to current tube's map
	// if there are any finders in the current tube that can be simplified to newmMapFinder+1, do that
	// if no changes are made, do not continue searching from this tube
	private static void propagatePositivePulse(World world, BlockPos pos, ItemFinderMap incomingMap, Queue<Pair<BlockPos, ItemFinderMap>> toSearch, Set<BlockPos> visited)
	{
		System.out.println("positively pulsing " + pos.toString());
		visited.add(pos);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBrassTube)
		{
			TileEntityBrassTube tube = (TileEntityBrassTube)te;
			
			if (tube.finderMap.mergeFromFinderMap(incomingMap))
			{	// if change occurred in tube, continue searching
				System.out.println("Items added to " + pos.toString());
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
			propagatePositivePulse(world, pair.getLeft(), pair.getRight(), toSearch, visited);
		}
	}
	
	public static void onTubeNeighborChange(World world, BlockPos pos)
	{
		// generate negative pulse from origin (that received the neighborchange event)
		// negative pulse propagates until it reaches tubes that do not change from the pulse
		// tubes on the edge of the negative pulse generate positive pulses

		Set<BlockPos> visited = new HashSet<BlockPos>();
		Set<BlockPos> positivePulseGenerators = new HashSet<BlockPos>();
		Queue<BlockPos> BFSqueue = new LinkedList<BlockPos>();
		propagateNegativePulse(world, pos, visited, positivePulseGenerators, BFSqueue);

		for (BlockPos positivePos : positivePulseGenerators)
		{
			initiatePositivePulse(world, positivePos);
		}
	}
	
	// recursively iterate through the network via BFS
	// return a blockpos set representing tubes that positive pulses will be generated from
	private static void initiateNegativePulse(World world, BlockPos pos)
	{
		
		{
			
//			-neighbor updates happen BEFORE post-place event
//			-order of notable events when placing a tube:
//				oldblock.onreplaced	(only relevant for destroyed TEs/tubes)
//				old TE removed		(only relevant for destroyed TEs/tubes)
//				newblock.onblockadded
//				new TE instantiated
//				new TE setPos, setWorld
//				world.markAndNotifyBlock
//				neighbors.neighborChanged
//				post place / onBlockPlacedBy
//
//			Things that can happen when a neighbor updates:
//
//			POSITIVE	-new inventory added, new pipe added, slot becomes empty
//				-can be done entirely from neighborchanged
//				-if new tube is placed, the neighbors will see it and eventually propagate correctly
//			-existing item becomes closer	-handled
//			-empty becomes closer than item, removing item	-handled
//			-item becomes closer than empty	-handled
//
//
//			NEGATIVE	-slot fills, inventory or pipe removed
//				-again, can be done entirely from neighborchanged
//			-new best reference to existing item becomes further away
//			-or reference to item no longer exists
//				HOW TO HANDLE
//				1-on neighborchanged,
//				2-if any of this block's finders no longer exist as one-block closer
//					in any neighbors, then
//					3-remove that finder from this block
//				4-propagate 2 and 3 through network until 2 is false
//				5-after 2,3,4 have all completely resolved,
//					in any tube where "4" occurred, generate a positive pulse in each neighbor
//					alternatively, "peek ahead" to determine 2, then only generate positive
//					pulses from the tube that peeked
//
//			COMBINING THE TWO
//			-on someblock.neighborchanged
//			-generate negative pulse from someblock
//			-negative pulse generates positive pulses
//			-if someblock was not eligible to be affected by negative pulse, it will only generate a positive pulse
//
//
//			[({empty in here} && {empty < dist}) || ({item != null} && {item in here and closer})]
		}
	}
	
	/// returns true if negative pulse continued to propagate from the given position, false if it stopped here
	private static void propagateNegativePulse(World world, BlockPos pos,
			Set<BlockPos> visited,
			Set<BlockPos> positivePulseGenerators,
			Queue<BlockPos> BFSqueue)
	{
		System.out.println("Negatively pulsing " + pos);
		visited.add(pos);

		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBrassTube)
		{
			TileEntityBrassTube tube = (TileEntityBrassTube) te;
			// if any of this tube's finders no longer exist as one-block-closer in any neigbors
			// then remove that finder from this tube
			Map<Item, ItemFinder> finders= tube.finderMap.finders;
			List<ItemFinderMap> adjacentFinderMaps = FinderHelper.getFinderMapsAdjacentTo(world, pos);
			Set<Item> itemsToRemove = new HashSet<Item>();
			// for each item in this tube's finders
			for (Item item : finders.keySet())
			{
				boolean foundProperFinders = false;
				// compare that item with each adjacent finder-map
				for (ItemFinderMap finderMap : adjacentFinderMaps)
				{
					// try to find at least one adjacent finder that is closer to a slot for the item
					if (finderMap.isItemOrEmptyCloserHereThanIn(item, finders.get(item).distance))
					{	// if we do we can ignore this finder
						foundProperFinders = true;
						break;
					}
				}
				// if we can't, then mark the finder for removal from this tube
				if (!foundProperFinders)
				{
					itemsToRemove.add(item);
				}
			}
			boolean itemsWereRemoved = itemsToRemove.size() > 0;
			if (itemsWereRemoved)
			{
				// can't remove items from a set while iterating over it, delay the removal until here
				for (Item item : itemsToRemove)
				{
					System.out.println("Removing" + (item == null ? "empty" : item.getTranslationKey()) + " from " + pos.toString());
					finders.remove(item);
				}
				// items were removed from this, so continue to propagate (and return true)
				for (EnumFacing face : EnumFacing.values())
				{
					BlockPos checkPos = pos.offset(face);
					if (!visited.contains(checkPos))
					{
						BFSqueue.add(checkPos);
					}
				}
			}
			else
			{
				// if doesn't continue propagating, mark this pos for generating a positive pulse later
				positivePulseGenerators.add(pos);
			}
			
			// 
		}
		
		BlockPos nextPos = BFSqueue.poll();
		if (nextPos != null)
		{
			propagateNegativePulse(world, nextPos, visited, positivePulseGenerators, BFSqueue);
		}
	}
	
	public static List<ItemFinderMap> getFinderMapsAdjacentTo(World world, BlockPos pos)
	{
		LinkedList<ItemFinderMap> list = new LinkedList<ItemFinderMap>();
		for (EnumFacing face : EnumFacing.values())
		{
			BlockPos checkPos = pos.offset(face);
			TileEntity te = world.getTileEntity(checkPos);
			
			if (te instanceof TileEntityBrassTube)
			{
				list.add(((TileEntityBrassTube)te).finderMap);
			}
			else if (te != null)
			{
				LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
				if (cap.isPresent())
				{
					cap.ifPresent(handler ->
					{
						list.add(ItemFinderMap.getFinderMapFromItemHandler(handler, checkPos));
					});

				}
			}
		}
		return list;
	}
}
