package com.github.commoble.tubesreloaded.common.blocks.loader;

import net.minecraft.block.Block;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;

public class LoaderBlock extends Block
{
	public static final DirectionProperty FACING = DirectionalBlock.FACING;

	public LoaderBlock(Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
	}
}
