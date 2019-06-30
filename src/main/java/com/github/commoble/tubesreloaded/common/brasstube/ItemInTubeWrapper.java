package com.github.commoble.tubesreloaded.common.brasstube;

import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

/** Wrapper for the itemstacks being routed for the tubes
 * Tracks an itemstack as well as which tubes the stack has been through
 * @author Joseph
 *
 */
public class ItemInTubeWrapper
{
	public ItemStack stack;
	public Queue<BlockPos> remainingPositions;
	public int ticksRemaining;
	
	public static final String X_TAG = "X";
	public static final String Y_TAG = "Y";
	public static final String Z_TAG = "Z";
	public static final String MOVES_REMAINING_TAG = "visited";
	public static final String TICKS_REMAINING_TAG = "ticksRemaining";
	
	/** It would be a good idea to supply this constructor with a copy of a list when using an existing list **/
	public ItemInTubeWrapper(ItemStack stack, Queue<BlockPos> moves, int ticksToTravel)
	{
		this.stack = stack;
		this.remainingPositions = moves;
		this.ticksRemaining = ticksToTravel;
	}
	
	public static ItemInTubeWrapper readFromNBT(CompoundNBT compound)
	{
		ItemStack stack = ItemStack.read(compound);
		ListNBT remainingPositionsNBT = compound.getList(MOVES_REMAINING_TAG, 10);
		Queue<BlockPos> positions = new LinkedList<BlockPos>();
		for (int i=0; i<remainingPositionsNBT.size(); i++)
		{
			CompoundNBT nbtPos = remainingPositionsNBT.getCompound(i);
			int x = nbtPos.getInt(X_TAG);
			int y = nbtPos.getInt(Y_TAG);
			int z = nbtPos.getInt(Z_TAG);
			positions.add(new BlockPos(x,y,z));
		}
		int ticksRemaining = compound.getInt(TICKS_REMAINING_TAG);

		return new ItemInTubeWrapper(stack, positions, ticksRemaining);
		
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		ListNBT movesListNBT = new ListNBT();
		int posCount = 0;
		for (BlockPos pos : this.remainingPositions)
		{
			CompoundNBT posNBT = new CompoundNBT();
			posNBT.putInt(X_TAG, pos.getX());
			posNBT.putInt(Y_TAG, pos.getY());
			posNBT.putInt(Z_TAG, pos.getZ());
			movesListNBT.add(posCount, posNBT);
			posCount++;
		}
		
		compound.put(MOVES_REMAINING_TAG, movesListNBT);
		compound.putInt(TICKS_REMAINING_TAG, this.ticksRemaining);
		this.stack.write(compound);
		
		return compound;
	}
}
