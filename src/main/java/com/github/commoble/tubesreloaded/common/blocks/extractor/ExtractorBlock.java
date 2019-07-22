package com.github.commoble.tubesreloaded.common.blocks.extractor;

import com.github.commoble.tubesreloaded.common.util.DirectionHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class ExtractorBlock extends Block
{
	// output is current facing, input is face.getOpposite()
	public static final DirectionProperty FACING = DirectionalBlock.FACING;
	
	protected final VoxelShape[] shapes;

	public ExtractorBlock(Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.DOWN));
		this.shapes=this.makeShapes();
	}

	//// facing and blockstate boilerplate

	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.getDefaultState().with(FACING, DirectionHelper.getBlockFacingForPlacement(context));
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
	
	// model shapes

	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return state.getShape(worldIn, pos);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return this.getShape(state, worldIn, pos, context);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return this.shapes[this.getShapeIndex(state)];
	}

	public int getShapeIndex(BlockState state)
	{
		return state.get(FACING).getIndex();
	}
	
	protected VoxelShape[] makeShapes()
	{		
		VoxelShape[] shapes = new VoxelShape[6];
		
		for (int face=0; face<6; face++)	// dunswe
		{
			boolean DOWN = face==0;
			boolean UP = face==1;
			boolean NORTH = face==2;
			boolean SOUTH = face==3;
			boolean WEST = face==4;
			boolean EAST = face==5;
			
			double input_x_min = WEST ? 10D : 0D;
			double input_x_max = EAST ? 6D : 16D;
			double input_y_min = DOWN ? 10D : 0D;
			double input_y_max = UP ? 6D : 16D;
			double input_z_min = SOUTH ? 10D : 0D;
			double input_z_max = NORTH ? 6D : 16D;
			
			double mid_x_min = EAST ? 6D : 4D;
			double mid_x_max = WEST ? 10D : 12D;
			double mid_y_min = UP ? 6D : 4D;
			double mid_y_max = DOWN ? 10D : 12D;
			double mid_z_min = NORTH ? 6D : 4D;
			double mid_z_max = SOUTH ? 10D : 12D;
			
			double output_x_min = WEST ? 0D : EAST ? 12D : 6D;
			double output_x_max = WEST ? 4D : EAST ? 16D : 10D;
			double output_y_min = DOWN ? 0D : UP ? 12D : 6D;
			double output_y_max = DOWN ? 4D : UP ? 4D : 10D;
			double output_z_min = NORTH ? 12D : SOUTH ? 0D : 6D;
			double output_z_max = NORTH ? 16D : SOUTH ? 4D : 10D;
			
			VoxelShape input = Block.makeCuboidShape(input_x_min, input_y_min, input_z_min, input_x_max, input_y_max, input_z_max);
			VoxelShape mid = Block.makeCuboidShape(mid_x_min, mid_y_min, mid_z_min, mid_x_max, mid_y_max, mid_z_max);
			VoxelShape output = Block.makeCuboidShape(output_x_min, output_y_min, output_z_min, output_x_max, output_y_max, output_z_max);
			
			shapes[face] = VoxelShapes.or(input, mid, output);
		}
		
		return shapes;
	}
}
