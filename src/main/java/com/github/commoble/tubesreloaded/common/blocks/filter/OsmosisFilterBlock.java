package com.github.commoble.tubesreloaded.common.blocks.filter;

import java.util.Optional;

import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;
import com.github.commoble.tubesreloaded.common.util.ClassHelper;
import com.github.commoble.tubesreloaded.common.util.WorldHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class OsmosisFilterBlock extends FilterBlock
{
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED; // if false, will not extract items automatically

	public OsmosisFilterBlock(Properties properties)
	{
		super(properties);

		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(ENABLED, true));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, ENABLED);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return TileEntityRegistrar.TE_TYPE_OSMOSIS_FILTER.create();
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		Optional<FilterTileEntity> te = WorldHelper.getTileEntityAt(OsmosisFilterTileEntity.class, worldIn, pos);
		return te.map(filter -> filter.onActivated(player, hit.getFace(), player.getHeldItem(handIn))).orElse(false);
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader worldReader, BlockPos pos, BlockPos neighbor)
	{
		boolean dormant = ClassHelper.as(worldReader, World.class).map(world -> {
			boolean isFilterNotPowered = !world.isBlockPowered(pos);
			if (isFilterNotPowered != state.get(ENABLED))	// if state has changed
			{
				world.setBlockState(pos, state.with(ENABLED, Boolean.valueOf(isFilterNotPowered)), 4);
			}
			return Boolean.valueOf(!isFilterNotPowered);
		}).orElse(false);
		
		WorldHelper.getTileEntityAt(OsmosisFilterTileEntity.class, worldReader, pos).ifPresent(te -> te.isDormant = dormant);;
	}

}
