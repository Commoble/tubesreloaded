package com.github.commoble.tubesreloaded.common.routing;

import java.util.HashMap;
import java.util.Map;

import com.github.commoble.tubesreloaded.common.brasstube.TileEntityBrassTube;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemFinderMap
{
	public Map<Item, ItemFinder> finders;

	public ItemFinderMap()
	{
		this.finders = new HashMap<Item, ItemFinder>();
	}
	
	public ItemFinderMap(Map<Item, ItemFinder> finders)
	{
		this.finders = finders;
	}
	
	public void mergeFromTE(World world, BlockPos pos)
	{
		this.mergeFromTE(world.getTileEntity(pos));
	}

	public void mergeFromTE(TileEntity te)
	{
		if (te == null)
			return;

		// if the TE is a tube, take the tube's finders and compare them to this one's,
		// with the following rules:
		// -- any items further away than Items.AIR are ignored
		// -- for any items present in both maps, the finder closest to an inventory is
		// used
		// -- any finders are treated as being one unit further away in this tube as
		// they are in the source tube

		// if the TE is not a tube but is still treatable as an inventory of some kind,
		// as above, except
		// a finder map will be generated with all distances 0

		if (te instanceof TileEntityBrassTube)
		{
			TileEntityBrassTube tube = (TileEntityBrassTube) te;
			this.mergeFromFinderMap(tube.finderMap);
		}
		else
		{
			LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
			if (cap.isPresent())
			{
				BlockPos pos = te.getPos();
				cap.ifPresent(handler -> mergeFromFinderMap(getFinderMapFromItemHandler(handler, pos)));

			}

			// ignore TEs that use IInventory but not item handler
		}
	}

	// returns a finder map that represents the items in a TE's item handler
	// items here are 0 units away from the TE since they are in it
	// 
	public ItemFinderMap getFinderMapFromItemHandler(IItemHandler handler, BlockPos pos)
	{
		HashMap<Item, ItemFinder> map = new HashMap<Item, ItemFinder>();
		int slots = handler.getSlots();
		for (int i = 0; i < slots; i++)
		{
			ItemStack stack = handler.getStackInSlot(i);
			
			// if there is an empty slot here, we can put any item in it, the other ones don't matter
			if (stack.isEmpty())
			{
				HashMap<Item, ItemFinder> emptyMap = new HashMap<Item, ItemFinder>();
				emptyMap.put(null, new ItemFinder(0, pos));	// null is the empty item
				return new ItemFinderMap(map);
			}
			
			if (stack.getCount() < stack.getMaxStackSize() && !map.containsKey(stack.getItem()))
			{
				map.put(stack.getItem(), new ItemFinder(0, pos));
			}
		}
		
		return new ItemFinderMap(map);
	}

	// for each itemfinder in the adjacent map (the argument)
	// use finder+1
	// if this is closer than this map's finder for that item, use that instead
	// also, empty item takes precendence over other items -- anything further away than
	// the nearest empty item slot can be ignored
	// returns true if this map was changed, false if not
	public boolean mergeFromFinderMap(ItemFinderMap adjacentFinderMap)
	{
		// check empty slots first
		boolean changed = this.mergeEmptySlotFrom(adjacentFinderMap);
		
		for (Item item : adjacentFinderMap.finders.keySet())
		{
			changed = this.mergeFromAdjacentFinder(item, adjacentFinderMap.finders.get(item)) ? true : changed;
		}
		
		return changed;
	}
	
	// returns true if this map was changed, false if not
	public boolean mergeEmptySlotFrom(ItemFinderMap adjacentFinderMap)
	{
		if (adjacentFinderMap.finders.containsKey(null))
		{
			ItemFinder emptySlot = adjacentFinderMap.finders.get(null);
			// if this map doesn't contain key OR it does and the one-space-away map's key would be closer if it was here
			if (!this.finders.containsKey(null) || emptySlot.distance+1 < this.finders.get(null).distance)
			{
					// if this new empty slot is closer than the existing empty slot,
					// put it into this map and remove all itemfinders further away than the new slot
					ItemFinder newEmptySlot = emptySlot.plusOneSpace();
					HashMap<Item, ItemFinder> filteredMap = new HashMap<Item, ItemFinder>();
					filteredMap.put(null, newEmptySlot);
					for (Item item : this.finders.keySet())
					{
						ItemFinder finder = this.finders.get(item);
						if (finder.distance < newEmptySlot.distance)
						{
							filteredMap.put(item, finder);
						}
					}
					
					this.finders = filteredMap;
					return true;
			}
		}
		// else do nothing
		return false;
	}
	
	// returns true if this map changed, false if not
	public boolean mergeFromAdjacentFinder(Item item, ItemFinder adjacentFinder)
	{
		if (
				(!this.finders.containsKey(item) || adjacentFinder.distance+1 < this.finders.get(item).distance)
				&&
				(!this.finders.containsKey(null) || adjacentFinder.distance+1 < this.finders.get(null).distance)
			)
		{
			this.finders.put(item, adjacentFinder.plusOneSpace());
			return true;
		}
		else
		{
			return false;
		}
	}
}
