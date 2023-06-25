package commoble.tubesreloaded.blocks.filter;

import commoble.tubesreloaded.TubesReloaded;
import commoble.tubesreloaded.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class OsmosisFilterBlock extends FilterBlock
{
	public static final BooleanProperty TRANSFERRING_ITEMS = BlockStateProperties.ENABLED; // if false, will not extract items automatically

	public OsmosisFilterBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRANSFERRING_ITEMS, false));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(TRANSFERRING_ITEMS);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return TubesReloaded.get().osmosisFilterEntity.get().create(pos, state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		if (!level.isClientSide && type == TubesReloaded.get().osmosisFilterEntity.get())
		{
			return (BlockEntityTicker<T>) OsmosisFilterBlockEntity.SERVER_TICKER;
		}
		return super.getTicker(level, state, type);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		if (oldState.getBlock() != state.getBlock())
		{
			this.updateState(level, pos, state);
		}
	}
	
	private void updateState(Level level, BlockPos pos, BlockState state)
	{
		if (!level.isClientSide())
		{
			final boolean hasRedstoneSignal = level.hasNeighborSignal(pos);
			final boolean active = state.getValue(TRANSFERRING_ITEMS);
			final Direction outputDirection = state.getValue(FACING);
			final Direction inputDirection = outputDirection.getOpposite();
		
			if (level.getBlockEntity(pos) instanceof OsmosisFilterBlockEntity filter)
			{
				boolean checkedItemsThisTick = filter.getCheckedItemsAndMarkChecked();
				
				if (!checkedItemsThisTick)
				{
					BlockEntity neighborBe = level.getBlockEntity(pos.relative(inputDirection));
					boolean canExtractItems = neighborBe != null
						&& neighborBe.getCapability(ForgeCapabilities.ITEM_HANDLER, outputDirection)
							.map(handler -> WorldHelper.doesItemHandlerHaveAnyExtractableItems(handler, filter::canItemPassThroughFilter))
							.orElse(false);
					
					if (active && (hasRedstoneSignal || !canExtractItems))
					{
						level.setBlock(pos, state.setValue(TRANSFERRING_ITEMS, false), 6);
					}
					else if (!active && !hasRedstoneSignal && canExtractItems)
					{
						level.setBlock(pos, state.setValue(TRANSFERRING_ITEMS, true), 6);
					}
				}
			}
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		InteractionResult result = super.use(state, level, pos, player, hand, hit);
		this.updateState(level, pos, state);
		return result;
	}
	
	@Override
	@Deprecated
	public void neighborChanged(BlockState thisState, Level level, BlockPos thisPos, Block neighborBlock, BlockPos neighborPos, boolean isMoving)
	{
		super.neighborChanged(thisState, level, thisPos, neighborBlock, neighborPos, isMoving);
		this.updateState(level, thisPos, thisState);
	}
	
	@Override
	public void onNeighborChange(BlockState state, LevelReader levelReader, BlockPos thisPos, BlockPos neighborPos)
	{
		super.onNeighborChange(state, levelReader, thisPos, neighborPos);
		if (levelReader instanceof Level level)
		{
			this.updateState(level, thisPos, state);
		}
	}

}
