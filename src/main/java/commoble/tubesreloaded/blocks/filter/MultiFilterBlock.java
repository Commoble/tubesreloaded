package commoble.tubesreloaded.blocks.filter;

import commoble.tubesreloaded.TubesReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class MultiFilterBlock extends AbstractFilterBlock implements EntityBlock
{
	public static final DirectionProperty FACING = DirectionalBlock.FACING;
	
	public MultiFilterBlock(Properties props)
	{
		super(props);
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return TubesReloaded.get().multiFilterEntity.get().create(pos, state);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if (player instanceof ServerPlayer serverPlayer)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof MultiFilterBlockEntity filter)
			{
				NetworkHooks.openScreen(
					serverPlayer,
					new SimpleMenuProvider(MultiFilterMenu.serverMenu(filter), Component.translatable(this.getDescriptionId()))
				);
			}
		}
		
		return InteractionResult.SUCCESS;
	}
}
