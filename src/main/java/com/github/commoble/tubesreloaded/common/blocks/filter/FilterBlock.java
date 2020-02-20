package com.github.commoble.tubesreloaded.common.blocks.filter;

import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;
import com.github.commoble.tubesreloaded.common.util.ClassHelper;
import com.github.commoble.tubesreloaded.common.util.DirectionHelper;
import com.github.commoble.tubesreloaded.common.util.WorldHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class FilterBlock extends Block
{
	public static final DirectionProperty FACING = DirectionalBlock.FACING;	// facing of output

	protected final VoxelShape[] shapes;

	public FilterBlock(Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
		this.shapes = this.makeShapes();
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return TileEntityRegistrar.FILTER.create();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
	{
//		Optional<FilterTileEntity> te = WorldHelper.getTileEntityAt(FilterTileEntity.class, world, pos);
//		boolean success = te.map(filter -> filter.onActivated(player, hit.getFace(), player.getHeldItem(hand))).orElse(false);
//		return success ? ActionResultType.SUCCESS : super.onBlockActivated(state, world, pos, player, hand, hit);
		ClassHelper.as(player, ServerPlayerEntity.class).ifPresent(serverPlayer ->
			WorldHelper.getTileEntityAt(FilterTileEntity.class, world, pos).ifPresent(filter -> 
				NetworkHooks.openGui(
					serverPlayer,
					new SimpleNamedContainerProvider(
						(id, inventory, theServerPlayer) -> new FilterContainer(id, inventory, filter.inventory),
						this.getNameTextComponent()
					)
				)
				
			)
		);
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	@Deprecated
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() == newState.getBlock())
		{
			// only thing super.onReplaced does is remove the tile entity
			// if the block stays the same, we specifically do NOT remove the tile entity
			// so don't do anything here
		}
		else
		{
			if (!world.isRemote)
			{
				WorldHelper.getTileEntityAt(FilterTileEntity.class, world, pos).ifPresent(te -> te.dropItems());
			}
			super.onReplaced(state, world, pos, newState, isMoving);
		}
	}
	
	//// facing and blockstate boilerplate

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.getDefaultState().with(FACING, DirectionHelper.getBlockFacingForPlacement(context));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}
	
	// shapes
	
	protected VoxelShape[] makeShapes()
	{
		VoxelShape[] shapes = new VoxelShape[6];
		
		for (int face=0; face<6; face++)
		{
			boolean DOWN = face == 0;
			boolean UP = face == 1;
			boolean NORTH = face == 2;
			boolean SOUTH = face == 3;
			boolean WEST = face == 4;
			boolean EAST = face == 5;
			
			// plate shape
			double x_min = EAST ? 14D : 0D;
			double x_max = WEST ? 2D : 16D;
			double y_min = UP ? 14D : 0D;
			double y_max = DOWN ? 2D : 16D;
			double z_min = SOUTH ? 14D : 0D;
			double z_max = NORTH ? 2D : 16D;
			
			VoxelShape plate = Block.makeCuboidShape(x_min, y_min, z_min, x_max, y_max, z_max);
			
			// vertical crossbars
			x_min = WEST ? 0D : EAST ? 0D : 6D;
			x_max = WEST ? 16D : EAST ? 16D : 10D;
			y_min = 0D;
			y_max = 16D;
			z_min = WEST ? 6D : EAST ? 6D : 0D;
			z_max = WEST ? 10D : EAST ? 10D : 16D;
			
			VoxelShape vertical = Block.makeCuboidShape(x_min, y_min, z_min, x_max, y_max, z_max);
			
			// horizontal crossbars
			x_min = 0D;
			x_max = 16D;
			y_min = UP ? 0D : DOWN ? 0D : 6D;
			y_max = UP ? 16D : DOWN ? 16D : 10D;
			z_min = UP ? 6D : DOWN ? 6D : 0D;
			z_max = UP ? 10D : DOWN ? 10D : 16D;
			
			VoxelShape horizontal = Block.makeCuboidShape(x_min, y_min, z_min, x_max, y_max, z_max);
			
			shapes[face] = VoxelShapes.or(plate, vertical, horizontal);
		}
		
		return shapes;
	}
	
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
}
