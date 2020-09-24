package com.github.commoble.tubesreloaded.blocks.filter;

import java.util.Optional;

import com.github.commoble.tubesreloaded.registry.TileEntityRegistrar;
import com.github.commoble.tubesreloaded.util.ClassHelper;
import com.github.commoble.tubesreloaded.util.WorldHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public class OsmosisFilterBlock extends FilterBlock
{
	public static final BooleanProperty TRANSFERRING_ITEMS = BlockStateProperties.ENABLED; // if false, will not extract items automatically

	public OsmosisFilterBlock(Properties properties)
	{
		super(properties);

		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(TRANSFERRING_ITEMS, false));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, TRANSFERRING_ITEMS);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return TileEntityRegistrar.OSMOSIS_FILTER.create();
	}   
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		if (oldState.getBlock() != state.getBlock())
		{
			this.updateState(worldIn, pos, state);
		}
	}
	
	private void updateState(World world, BlockPos pos, BlockState state)
	{
		if (!world.isRemote)
		{
			final boolean hasRedstoneSignal = world.isBlockPowered(pos);
			final boolean active = state.get(TRANSFERRING_ITEMS);
			final Direction outputDirection = state.get(FACING);
			final Direction inputDirection = outputDirection.getOpposite();

			Optional<OsmosisFilterTileEntity> maybeFilter = WorldHelper.getTileEntityAt(OsmosisFilterTileEntity.class, world, pos);
			
			boolean checkedItemsThisTick = maybeFilter.map(OsmosisFilterTileEntity::getCheckedItemsAndMarkChecked).orElse(true);
			
			if (!checkedItemsThisTick)
			{
				final boolean canExtractItems = maybeFilter
					.filter(filter ->
						WorldHelper.getTileEntityAt(world, pos.offset(inputDirection))
						.filter(te ->
							te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputDirection)
							.filter(handler -> (Boolean)WorldHelper.doesItemHandlerHaveAnyExtractableItems(handler, filter::canItemPassThroughFilter))
							.isPresent())
						.isPresent())
					.isPresent();
				if (active && (hasRedstoneSignal || !canExtractItems))
				{
					world.setBlockState(pos, state.with(TRANSFERRING_ITEMS, Boolean.valueOf(false)), 6);
				}
				else if (!active && !hasRedstoneSignal && canExtractItems)
				{
					world.setBlockState(pos, state.with(TRANSFERRING_ITEMS, Boolean.valueOf(true)), 6);
				}
			}
		}
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		ActionResultType result = super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
		this.updateState(worldIn, pos, state);
		return result;
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		this.updateState(worldIn, pos, state);
	}
	
	@Override
	public void onNeighborChange(BlockState state, IWorldReader worldReader, BlockPos pos, BlockPos neighbor)
	{
		super.onNeighborChange(state, worldReader, pos, neighbor);
		ClassHelper.as(worldReader, World.class).ifPresent(world -> this.updateState(world, pos, state));
	}

}
