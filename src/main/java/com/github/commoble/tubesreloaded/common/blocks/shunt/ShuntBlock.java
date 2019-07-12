package com.github.commoble.tubesreloaded.common.blocks.shunt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockReader;

public class ShuntBlock extends Block
{
	public static final DirectionProperty FACING = DirectionalBlock.FACING;

	public ShuntBlock(Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	public TileEntity createNewTileEntity(IBlockReader worldIn)
	{
		return new ShuntTileEntity();
	}

	//// facing and blockstate boilerplate

	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		Direction lookDir = context.getNearestLookingDirection();
		Direction placeDir = context.isPlacerSneaking() ? lookDir : lookDir.getOpposite();
		return this.getDefaultState().with(FACING, placeDir);
	}

	public BlockState rotate(BlockState state, Rotation rot)
	{
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

}
