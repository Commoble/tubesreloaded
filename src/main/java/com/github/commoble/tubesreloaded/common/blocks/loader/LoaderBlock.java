package com.github.commoble.tubesreloaded.common.blocks.loader;

import com.github.commoble.tubesreloaded.common.routing.Endpoint;
import com.github.commoble.tubesreloaded.common.util.WorldHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class LoaderBlock extends Block
{
	public static final DirectionProperty FACING = DirectionalBlock.FACING;

	public LoaderBlock(Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		ItemStack heldStack = player.getHeldItem(handIn);
		if (heldStack.getCount() > 0)
		{
			ItemStack remaining = this.insertItem(heldStack.copy(), worldIn, pos, state);
			if (remaining.getCount() < heldStack.getCount())
			{
		        worldIn.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.3F, worldIn.rand.nextFloat() * 0.25F + 5F);
				player.setHeldItem(handIn, remaining);
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	// returns the portion of the itemstack that wasn't inserted
	private ItemStack insertItem(ItemStack stack, World world, BlockPos pos, BlockState state)
	{
		// check if it can insert the item
		Direction output_dir = state.get(FACING);
		BlockPos output_pos = pos.offset(output_dir);
		ItemStack remaining = WorldHelper.getTEItemHandlerAt(world, output_pos, output_dir.getOpposite())
				.map(handler -> Endpoint.disperseItemToHandler(stack, handler)).orElse(stack.copy());

		if (remaining.getCount() > 0) // we have remaining items
		{
			// check if there is space to eject the item
			if (world.getBlockState(output_pos).isOpaqueCube(world, output_pos))
			{	// if output position is solid cube, don't eject item
				return remaining;
			}
			else
			{	// otherwise eject item
				WorldHelper.ejectItemstack(world, pos, output_dir, remaining);
				return ItemStack.EMPTY;
			}
		}
		else	// item was accepted fully
		{
			return ItemStack.EMPTY;
		}
	}

	//// facing and blockstate boilerplate

	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		// facing depends on the face of the block that was clicked on
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
