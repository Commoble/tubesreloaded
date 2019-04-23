package com.github.commoble.tubesreloaded.common.brasstube;

import java.util.HashSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

/** Wrapper for the itemstacks being routed for the tubes
 * Tracks an itemstack as well as which tubes the stack has been through
 * @author Joseph
 *
 */
public class ItemInTubeWrapper
{
	public ItemStack stack;
	public HashSet<BlockPos> visited = new HashSet<BlockPos>();
	
	public static final String X_TAG = "X";
	public static final String Y_TAG = "Y";
	public static final String Z_TAG = "Z";
	public static final String VISITED_SET_TAG = "visited";
	
	public ItemInTubeWrapper(ItemStack stack, BlockPos origin)
	{
		this.stack = stack;
		visited.add(origin);
	}
	
	public ItemInTubeWrapper(ItemStack stack, HashSet<BlockPos> set)
	{
		this.stack = stack;
		this.visited = set;
	}
	
	public static ItemInTubeWrapper readFromNBT(NBTTagCompound compound)
	{
		ItemStack stack = ItemStack.read(compound);
		NBTTagList visitedSetTagList = compound.getList(VISITED_SET_TAG, 10);
		HashSet<BlockPos> visitedSet = new HashSet<BlockPos>();
		for (int i=0; i<visitedSetTagList.size(); i++)
		{
			NBTTagCompound visitedPos = visitedSetTagList.getCompound(i);
			int x = visitedPos.getInt(X_TAG);
			int y = visitedPos.getInt(Y_TAG);
			int z = visitedPos.getInt(Z_TAG);
			visitedSet.add(new BlockPos(x,y,z));
		}

		return new ItemInTubeWrapper(stack, visitedSet);
		
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		NBTTagList visitedSetTagList = new NBTTagList();
		int posCount = 0;
		for (BlockPos pos : this.visited)
		{
			NBTTagCompound visitedPos = new NBTTagCompound();
			visitedPos.setInt(X_TAG, pos.getX());
			visitedPos.setInt(Y_TAG, pos.getY());
			visitedPos.setInt(Z_TAG, pos.getZ());
			visitedSetTagList.add(posCount, visitedPos);
			posCount++;
		}
		
		compound.setTag(VISITED_SET_TAG, visitedSetTagList);
		this.stack.write(compound);
		
		return compound;
	}
}
