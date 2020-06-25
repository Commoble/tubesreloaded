package com.github.commoble.tubesreloaded.blocks.tube;

import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.EntitySelectionContext;

public class TubeRayTraceSelectionContext extends EntitySelectionContext
{
//	private final Set<BlockPos> ignoreSet;
	
	public TubeRayTraceSelectionContext(BlockPos start, BlockPos end)
	{
		super(false, -Double.MAX_VALUE, Items.AIR); // same as EntitySelectionContext.DUMMY
//		this.ignoreSet = ImmutableSet.of(start.toImmutable(), end.toImmutable());
	}
	
//	public boolean shouldIgnoreBlock(BlockPos pos)
//	{
//		return this.ignoreSet.contains(pos);
//	}

}
