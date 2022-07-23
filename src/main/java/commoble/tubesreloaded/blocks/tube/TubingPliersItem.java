package commoble.tubesreloaded.blocks.tube;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import commoble.tubesreloaded.TubesReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class TubingPliersItem extends Item
{
	public static final String LAST_TUBE_DATA = "last_tube_data";
	public static final String LAST_TUBE_POS = "last_tube_pos";
	public static final String LAST_TUBE_SIDE = "last_tube_side";
	
	public TubingPliersItem(Properties properties)
	{
		super(properties);
	}

	/**
	 * Called when this item is used when targetting a Block
	 */
	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (world.getBlockEntity(pos) instanceof TubeBlockEntity tube)
		{
			return this.onUseOnTube(world, pos, tube, context.getItemInHand(), context.getPlayer(), context.getClickedFace());
		}
		BlockState state = world.getBlockState(pos);
		if (state.is(TubesReloaded.Tags.Blocks.ROTATABLE_BY_PLIERS))
		{
			return this.useOnRotatable(world, pos, state);
		}
		return super.useOn(context);
	}
	
	private InteractionResult onUseOnTube(Level level, BlockPos pos, @Nonnull TubeBlockEntity tube, ItemStack stack, Player player, Direction activatedSide)
	{
		if (!level.isClientSide)
		{
			@Nullable CompoundTag nbt = stack.getTagElement(LAST_TUBE_DATA);
			BlockState state = level.getBlockState(pos);
			
			// no existing position stored in item
			if (nbt == null)
			{
				// if we clicked an unused side of a tube block
				if (state.getBlock() instanceof TubeBlock tubeBlock && !tubeBlock.hasConnectionOnSide(state, activatedSide))
				{
					CompoundTag newNBT = new CompoundTag();
					newNBT.put(LAST_TUBE_POS, NbtUtils.writeBlockPos(pos));
					newNBT.putInt(LAST_TUBE_SIDE, activatedSide.ordinal());
					stack.addTagElement(LAST_TUBE_DATA, newNBT);
				}
				
			}
			else // existing position stored in stack
			{
				BlockPos lastPos = NbtUtils.readBlockPos(nbt.getCompound(LAST_TUBE_POS));
				Direction lastSide = Direction.from3DDataValue(nbt.getInt(LAST_TUBE_SIDE));
				// if player clicked the same tube twice, clear the last-used-position
				if (lastPos.equals(pos))
				{
					stack.removeTagKey(LAST_TUBE_DATA);
				}
				// if tube was already connected to the other position, remove connections
				else if (tube.hasRemoteConnection(lastPos))
				{
					TubeBlockEntity.removeConnection(level, pos, lastPos);
					stack.removeTagKey(LAST_TUBE_DATA);
				}
				else // we clicked a different tube that doesn't have an existing connection to the original tube
				{
					// if the tube is already connected on this side, cancel the connection
					if (tube.hasRemoteConnection(activatedSide))
					{
						stack.removeTagKey(LAST_TUBE_DATA);
						if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel)
						{
							TubesReloaded.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new TubeBreakPacket(Vec3.atCenterOf(lastPos), Vec3.atCenterOf(pos)));
							
							serverPlayer.playNotifySound(SoundEvents.WANDERING_TRADER_HURT, SoundSource.BLOCKS, 0.5F, 2F);
						}
						return InteractionResult.SUCCESS;
					}
					// do a raytrace to check for interruptions
					Vec3 startVec = RaytraceHelper.getTubeSideCenter(lastPos, lastSide);
					Vec3 endVec = RaytraceHelper.getTubeSideCenter(pos, activatedSide);
					Vec3 hit = RaytraceHelper.getTubeRaytraceHit(startVec, endVec, level);
					BlockState lastState = level.getBlockState(lastPos);
					
					// if tube wasn't connected but they can't be connected due to a block in the way, interrupt the connection
					if (hit != null)
					{
						stack.removeTagKey(LAST_TUBE_DATA);
						if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel)
						{
							TubesReloaded.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new TubeBreakPacket(startVec, endVec));
							serverLevel.sendParticles(serverPlayer, DustParticleOptions.REDSTONE, false, hit.x, hit.y, hit.z, 5, .05, .05, .05, 0);
							
							serverPlayer.playNotifySound(SoundEvents.WANDERING_TRADER_HURT, SoundSource.BLOCKS, 0.5F, 2F);
						}
					}
					// if we clicked the same side of two different tubes, deny the connection attempt (fixes an edge case)
					else if (activatedSide == lastSide)
					{
						stack.removeTagKey(LAST_TUBE_DATA);
						if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel)
						{
							TubesReloaded.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new TubeBreakPacket(startVec, endVec));
							serverLevel.sendParticles(serverPlayer, DustParticleOptions.REDSTONE, false, endVec.x, endVec.y, endVec.z, 5, .05, .05, .05, 0);
							
							serverPlayer.playNotifySound(SoundEvents.WANDERING_TRADER_HURT, SoundSource.BLOCKS, 0.5F, 2F);
						}
					}
					else if (state.getBlock() instanceof TubeBlock tubeBlock && !tubeBlock.hasConnectionOnSide(state, activatedSide))
					{
						// if tube wasn't connected to the first tube or another tube, connect them if they're close enough
						if (pos.closerThan(lastPos, TubesReloaded.get().serverConfig().maxTubeConnectionRange().get())
							&& lastState.getBlock() instanceof TubeBlock lastTubeBlock && !lastTubeBlock.hasConnectionOnSide(lastState, lastSide))
						{
							
							stack.removeTagKey(LAST_TUBE_DATA);
							if (level.getBlockEntity(lastPos) instanceof TubeBlockEntity lastPost)
							{
								RemoteConnection originalConnection = lastPost.getRemoteConnection(lastSide);
								
								// if the original tube was already connected on the given side, make sure to remove the original connection first
								if (originalConnection != null)
								{
									TubeBlockEntity.removeConnection(level, lastPos, originalConnection.toPos);
								}
								
								TubeBlockEntity.addConnection(level, lastPost, lastSide, tube, activatedSide);
							}
							stack.hurtAndBreak(1, player, thePlayer -> thePlayer.broadcastBreakEvent(EquipmentSlot.MAINHAND));
							
						}
						else // too far away, initiate a new connection from here
						{
							stack.addTagElement(LAST_TUBE_DATA, NbtUtils.writeBlockPos(pos));
							// TODO give feedback to player
						}
					}
				}
			}
			level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS,
				0.1F + level.random.nextFloat()*0.1F,
				0.7F + level.random.nextFloat()*0.1F);
		}
		
		return InteractionResult.SUCCESS;
	}
	
	private InteractionResult useOnRotatable(Level level, BlockPos pos, BlockState state)
	{
		level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS,
			0.1F + level.random.nextFloat()*0.1F,
			0.7F + level.random.nextFloat()*0.1F);
		
		if (!level.isClientSide)
		{
			for (Property<?> property : new Property<?>[] {BlockStateProperties.FACING, BlockStateProperties.HORIZONTAL_FACING})
			{
				if (state.hasProperty(property))
				{
					level.setBlock(pos, state.cycle(property), Block.UPDATE_ALL);
					break;
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, level, entity, itemSlot, isSelected);
		if (!level.isClientSide)
		{
			@Nullable CompoundTag lastTubeData = stack.getTagElement(LAST_TUBE_DATA);
			if (lastTubeData != null)
			{
				BlockPos lastTubePos = NbtUtils.readBlockPos(lastTubeData.getCompound(LAST_TUBE_POS));
				if (shouldRemoveConnection(lastTubePos, level, entity))
				{
					breakPendingConnection(stack,lastTubePos,entity,level);
				}
			}
		}
	}
	
	public static boolean shouldRemoveConnection(BlockPos connectionPos, Level level, Entity holder)
	{
		double maxDistance = TubesReloaded.get().serverConfig().maxTubeConnectionRange().get();
		if (holder.position().distanceToSqr(Vec3.atCenterOf(connectionPos)) > maxDistance*maxDistance)
		{
			// too far away, remove connection
			return true;
		}
		 // if blockentity doesn't exist or isn't connectable, remove connection
		return !(level.getBlockEntity(connectionPos) instanceof TubeBlockEntity);
	}
	
	public static void breakPendingConnection(ItemStack stack, BlockPos connectingPos, Entity holder, Level level)
	{
		stack.removeTagKey(LAST_TUBE_DATA);
		if (holder instanceof ServerPlayer serverPlayer)
		{
			TubesReloaded.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
				new TubeBreakPacket(
					Vec3.atCenterOf(connectingPos),
					new Vec3(holder.getX(), holder.getEyeY(), holder.getZ())));
		}
	}
}
