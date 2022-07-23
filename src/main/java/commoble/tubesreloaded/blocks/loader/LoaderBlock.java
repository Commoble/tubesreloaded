package commoble.tubesreloaded.blocks.loader;

import commoble.tubesreloaded.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class LoaderBlock extends Block
{
	public static final DirectionProperty FACING = DirectionalBlock.FACING;

	public LoaderBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTrace)
	{
		if (player instanceof ServerPlayer serverPlayer)
		{
			NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((id, inventory, theServerPlayer) ->
				new LoaderMenu(id, inventory, pos), Component.translatable(this.getDescriptionId()))
			);
		}

		return InteractionResult.SUCCESS;
	}

	// returns the portion of the itemstack that wasn't inserted
	public ItemStack insertItem(ItemStack stack, Level world, BlockPos pos, BlockState state)
	{
		// check if it can insert the item
		Direction outputDir = state.getValue(FACING);
		BlockPos outputPos = pos.relative(outputDir);
		ItemStack remaining = WorldHelper.getItemHandlerAt(world, outputPos, outputDir.getOpposite())
				.map(handler -> WorldHelper.disperseItemToHandler(stack, handler)).orElse(stack.copy());

		if (remaining.getCount() > 0) // we have remaining items
		{
			// check if there is space to eject the item
			if (world.getBlockState(outputPos).isCollisionShapeFullBlock(world, outputPos))
			{	// if output position is solid cube, don't eject item
				return remaining;
			}
			else
			{	// otherwise eject item
				WorldHelper.ejectItemstack(world, pos, outputDir, remaining);
		        world.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.3F, world.random.nextFloat() * 0.25F + 2F);
				return ItemStack.EMPTY;
			}
		}
		else	// item was accepted fully
		{
	        world.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.3F, world.random.nextFloat() * 0.25F + 1F);
			return ItemStack.EMPTY;
		}
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
}
