package com.github.commoble.tubesreloaded.util;

import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class PosHelper
{
	/**
	 * Returns the direction one must travel to get from the startPos to the nextPos
	 * returns null if the blocks are not adjacent
	 * Supplying (destination, start) instead of the other way around gives the face of the destination that touches the start
	 * (helpful for item handlers)
	 * @return
	 */
	@Nullable
	public static Direction getTravelDirectionFromTo(BlockPos startPos, BlockPos nextPos)
	{
		for (Direction face : Direction.values())
		{
			if (startPos.offset(face).equals(nextPos))
			{
				return face;
			}
		}
		return null;
	}
}
