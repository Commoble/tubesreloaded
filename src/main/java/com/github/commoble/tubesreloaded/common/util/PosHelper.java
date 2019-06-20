package com.github.commoble.tubesreloaded.common.util;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class PosHelper
{
	/**
	 * Returns the direction one must travel to get from the startPos to the nextPos
	 * returns null if the blocks are not adjacent
	 * @return
	 */
	@Nullable
	public static EnumFacing getFacingFrom(BlockPos startPos, BlockPos nextPos)
	{
		for (EnumFacing face : EnumFacing.values())
		{
			if (startPos.offset(face).equals(nextPos))
			{
				return face;
			}
		}
		return null;
	}
}
