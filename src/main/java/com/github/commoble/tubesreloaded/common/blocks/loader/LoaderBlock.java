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
			this.insertItem(heldStack.copy(), worldIn, pos, state);
			player.setHeldItem(handIn, ItemStack.EMPTY);
			return true;
		}
		else
		{
			return false;
		}
	}

	private void insertItem(ItemStack stack, World world, BlockPos pos, BlockState state)
	{
		// attempt to insert item
		Direction output_dir = state.get(FACING);
		BlockPos output_pos = pos.offset(output_dir);
		ItemStack remaining = WorldHelper.getTEItemHandlerAt(world, output_pos, output_dir.getOpposite())
				.map(handler -> Endpoint.disperseItemToHandler(stack, handler)).orElse(stack.copy());

		if (remaining.getCount() > 0) // we have remaining items
		{
			WorldHelper.ejectItemstack(world, pos, output_dir, remaining);
		}
	}

	//// facing and blockstate boilerplate

	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		// facing depends on the face of the block that was clicked on
		Direction clickDir = context.getFace(); // face of the block that was clicked
		Direction placeDir = context.isPlacerSneaking() ? clickDir.getOpposite() : clickDir;
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
