package com.github.commoble.tubesreloaded.common.routing;

import net.minecraft.util.math.BlockPos;

public class ItemFinder
{
	public int distance;
	public BlockPos pos;
	
	public ItemFinder(int distance, BlockPos pos)
	{
		this.distance = distance;
		this.pos = pos;
	}
	
	public ItemFinder plusOneSpace()
	{
		return new ItemFinder(this.distance+1, this.pos);
	}
}
