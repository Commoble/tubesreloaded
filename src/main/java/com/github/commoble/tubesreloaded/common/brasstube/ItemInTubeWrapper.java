package com.github.commoble.tubesreloaded.common.brasstube;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;

/** Wrapper for the itemstacks being routed for the tubes
 * Tracks an itemstack as well as which tubes the stack has been through
 * @author Joseph
 *
 */
public class ItemInTubeWrapper
{
	public ItemStack stack;
	public Queue<Direction> remainingMoves;
	public int ticksRemaining;
	
	public static final String MOVES_REMAINING_TAG = "visited";
	public static final String TICKS_REMAINING_TAG = "ticksRemaining";
	
	/** It would be a good idea to supply this constructor with a copy of a list when using an existing list **/
	public ItemInTubeWrapper(ItemStack stack, Queue<Direction> moves, int ticksToTravel)
	{
		this.stack = stack;
		this.remainingMoves = moves;
		this.ticksRemaining = ticksToTravel;
	}
	
	public static ItemInTubeWrapper readFromNBT(CompoundNBT compound)
	{
		ItemStack stack = ItemStack.read(compound);
		ListNBT remainingMovesNBT = compound.getList(MOVES_REMAINING_TAG, 10);
		Queue<Direction> moves = new LinkedList<Direction>();
		int size = remainingMovesNBT.size();
		for (int i=0; i<size; i++)
		{
			moves.add(Direction.byIndex(remainingMovesNBT.getInt(i)));
		}
		int ticksRemaining = compound.getInt(TICKS_REMAINING_TAG);

		return new ItemInTubeWrapper(stack, moves, ticksRemaining);
		
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		ListNBT movesListNBT = new ListNBT();
		int posCount = 0;
		for (Direction dir : this.remainingMoves)
		{
			IntNBT posNBT = new IntNBT(dir.getIndex());
			movesListNBT.add(posCount, posNBT);
			posCount++;
		}
		
		compound.put(MOVES_REMAINING_TAG, movesListNBT);
		compound.putInt(TICKS_REMAINING_TAG, this.ticksRemaining);
		this.stack.write(compound);
		
		return compound;
	}
	
	// compress the move list into an intarray NBT
	// where the intarray is of the form (dir0, count0, dir1, count1, . . . dirN, countN)
	// i.e. consisting of pairs of Direction indexes and how many times to move in that direction
	public static IntArrayNBT compressMoveList(Queue<Direction> moves)
	{
		if (moves == null || moves.isEmpty())
			return new IntArrayNBT(new int[0]);
		
		int moveIndex = 0;
		ArrayList<Integer> buffer = new ArrayList<Integer>();
		Direction currentMove = moves.peek();
		buffer.add(currentMove.getIndex());
		buffer.add(0);
		
		for (Direction dir : moves)
		{
			if (!dir.equals(currentMove))
			{
				buffer.add(dir.getIndex());
				buffer.add(1);
				currentMove = dir;
				moveIndex += 2;
			}
			else
			{
				buffer.set(moveIndex+1, buffer.get(moveIndex+1)+1);
			}
		}
		
		IntArrayNBT nbt = new IntArrayNBT(buffer);

		return nbt;
	}
	
	public static Queue<Direction> decompressMoveList(IntArrayNBT nbt)
	{
		Queue<Direction> moves = new LinkedList<Direction>();
		int[] buffer = nbt.getIntArray();
		int size = buffer.length;
		if (size % 2 != 0)
		{
			return moves;	// array should have an even size
		}
		// below this line, size of array is guaranteed to be even
		int pairCount = size / 2;
		
		for (int i=0; i<pairCount; i++)
		{
			Direction dir = Direction.byIndex(buffer[i*2]);
			int moveCount = buffer[i*2+1];
			for (int count=0; count<moveCount; count++)
			{
				moves.add(dir);	// add this direction that many times
			}
		}
		
		return moves;
	}
}
