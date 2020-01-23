package com.github.commoble.tubesreloaded.common.blocks.loader;

import com.github.commoble.tubesreloaded.common.util.DirectionHelper;
import com.github.commoble.tubesreloaded.common.util.WorldHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
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

	// onBlockActivated
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace)
	{
		ItemStack heldStack = player.getHeldItem(hand);
		if (heldStack.getCount() > 0)
		{
			ItemStack remaining = this.insertItem(heldStack.copy(), world, pos, state);
			if (remaining.getCount() < heldStack.getCount())
			{
		        if (!world.isRemote)
		        {
					player.setHeldItem(hand, remaining);
		        }
				return ActionResultType.SUCCESS;
			}
		}

		return super.onBlockActivated(state, world, pos, player, hand, rayTrace);
	}

	// returns the portion of the itemstack that wasn't inserted
	private ItemStack insertItem(ItemStack stack, World world, BlockPos pos, BlockState state)
	{
		// check if it can insert the item
		Direction output_dir = state.get(FACING);
		BlockPos output_pos = pos.offset(output_dir);
		ItemStack remaining = WorldHelper.getTEItemHandlerAt(world, output_pos, output_dir.getOpposite())
				.map(handler -> WorldHelper.disperseItemToHandler(stack, handler)).orElse(stack.copy());

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
		        world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.3F, world.rand.nextFloat() * 0.25F + 2F);
				return ItemStack.EMPTY;
			}
		}
		else	// item was accepted fully
		{
	        world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.3F, world.rand.nextFloat() * 0.25F + 1F);
			return ItemStack.EMPTY;
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
}
