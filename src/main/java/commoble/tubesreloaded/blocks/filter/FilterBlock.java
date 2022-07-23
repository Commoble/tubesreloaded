package commoble.tubesreloaded.blocks.filter;

import commoble.tubesreloaded.TubesReloaded;
import commoble.tubesreloaded.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

public class FilterBlock extends Block implements EntityBlock
{
	public static final DirectionProperty FACING = DirectionalBlock.FACING;	// facing of output

	public static final VoxelShape[] SHAPES = makeShapes();

	public FilterBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
//		Optional<FilterTileEntity> te = WorldHelper.getTileEntityAt(FilterTileEntity.class, world, pos);
//		boolean success = te.map(filter -> filter.onActivated(player, hit.getFace(), player.getHeldItem(hand))).orElse(false);
//		return success ? ActionResultType.SUCCESS : super.onBlockActivated(state, world, pos, player, hand, hit);
		if (player instanceof ServerPlayer serverPlayer)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof FilterBlockEntity filter)
			{
				NetworkHooks.openScreen(
					serverPlayer,
					new SimpleMenuProvider(FilterMenu.createServerMenuConstructor(filter), Component.translatable(this.getDescriptionId()))
				);
			}
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	@Deprecated
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (!level.isClientSide() && !state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FilterBlockEntity filter)
		{
			filter.dropItems();
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
	
	//// facing and blockstate boilerplate

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(FACING, WorldHelper.getBlockFacingForPlacement(context));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	@Deprecated
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}
	
	// shapes
	
	public static VoxelShape[] makeShapes()
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
			double x_min = WEST ? 14D : 0D;
			double x_max = EAST ? 2D : 16D;
			double y_min = DOWN ? 14D : 0D;
			double y_max = UP ? 2D : 16D;
			double z_min = NORTH ? 14D : 0D;
			double z_max = SOUTH ? 2D : 16D;
			
			VoxelShape plate = Block.box(x_min, y_min, z_min, x_max, y_max, z_max);
			
			// vertical crossbars
			x_min = WEST ? 0D : EAST ? 0D : 6D;
			x_max = WEST ? 16D : EAST ? 16D : 10D;
			y_min = 0D;
			y_max = 16D;
			z_min = WEST ? 6D : EAST ? 6D : 0D;
			z_max = WEST ? 10D : EAST ? 10D : 16D;
			
			VoxelShape vertical = Block.box(x_min, y_min, z_min, x_max, y_max, z_max);
			
			// horizontal crossbars
			x_min = 0D;
			x_max = 16D;
			y_min = UP ? 0D : DOWN ? 0D : 6D;
			y_max = UP ? 16D : DOWN ? 16D : 10D;
			z_min = UP ? 6D : DOWN ? 6D : 0D;
			z_max = UP ? 10D : DOWN ? 10D : 16D;
			
			VoxelShape horizontal = Block.box(x_min, y_min, z_min, x_max, y_max, z_max);
			
			shapes[face] = Shapes.or(plate, vertical, horizontal);
		}
		
		return shapes;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return SHAPES[this.getShapeIndex(state)];
	}
	
	public int getShapeIndex(BlockState state)
	{
		return state.getValue(FACING).ordinal();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return TubesReloaded.get().filterEntity.get().create(pos, state);
	}
}
