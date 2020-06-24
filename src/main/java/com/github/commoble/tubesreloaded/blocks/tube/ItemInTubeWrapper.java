package com.github.commoble.tubesreloaded.common.blocks.tube;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.Direction;

/** Wrapper for the itemstacks being routed for the tubes
 * Tracks an itemstack as well as which tubes the stack has been through
 * @author Joseph
 *
 */
public class ItemInTubeWrapper
{
	public ItemStack stack;
	public LinkedList<Direction> remainingMoves;
	public int maximumDurationInTube;	// the amount of ticks that will be spent in the current tube
	public int ticksElapsed;
	public boolean freshlyInserted = false;	// if true, was just inserted into a tube network --
		// freshlyInserted==true implies an "extra move" is necessary -- this will be the first
		// move in the move list, but the renderer will handle it differently
	
	public static final String MOVES_REMAINING_TAG = "moves";
	public static final String TICKS_REMAINING_TAG = "ticksRemaining";
	public static final String TICKS_DURATION_TAG = "maxDurationInTicks";
	public static final String IS_FRESHLY_INSERTED = "isFreshly";
	
	/** It would be a good idea to supply this constructor with a copy of a list when using an existing list **/
	public ItemInTubeWrapper(ItemStack stack, Queue<Direction> moves, int ticksToTravel)
	{
		this.stack = stack.copy();
		this.remainingMoves = new LinkedList<Direction>();
		for (Direction dir : moves)	// copy original list so so changes don't affect the old list
		{
			this.remainingMoves.add(dir);
		}
		this.ticksElapsed = 0;
		this.maximumDurationInTube = ticksToTravel;
	}
	
	/** Constructor to use when freshly inserting a wrapper into the network **/
	public ItemInTubeWrapper(ItemStack stack, Queue<Direction> moves, int ticksToTravel, Direction firstMove)
	{
		this(stack, moves, ticksToTravel);
		if (firstMove != null)
		{
			this.remainingMoves.addFirst(firstMove);
			this.freshlyInserted = true;
		}
	}
	
	public static ItemInTubeWrapper readFromNBT(CompoundNBT compound)
	{
		ItemStack stack = ItemStack.read(compound);
		int[] moveBuffer = compound.getIntArray(MOVES_REMAINING_TAG);
		int ticksElapsed = compound.getInt(TICKS_REMAINING_TAG);
		int maxDuration = compound.getInt(TICKS_DURATION_TAG);
		boolean isFreshlyInserted = compound.getBoolean(IS_FRESHLY_INSERTED);

		ItemInTubeWrapper wrapper = new ItemInTubeWrapper(stack, decompressMoveList(moveBuffer), maxDuration);
		wrapper.ticksElapsed = ticksElapsed;
		wrapper.freshlyInserted = isFreshlyInserted;
		return wrapper;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.put(MOVES_REMAINING_TAG, compressMoveList(this.remainingMoves));
		compound.putInt(TICKS_REMAINING_TAG, this.ticksElapsed);
		compound.putInt(TICKS_DURATION_TAG, this.maximumDurationInTube);
		compound.putBoolean(IS_FRESHLY_INSERTED, this.freshlyInserted);
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
	
	public static Queue<Direction> decompressMoveList(int[] buffer)
	{
		Queue<Direction> moves = new LinkedList<Direction>();
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
