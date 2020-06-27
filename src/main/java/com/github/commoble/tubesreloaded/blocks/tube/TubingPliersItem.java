package com.github.commoble.tubesreloaded.blocks.tube;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.commoble.tubesreloaded.TubesReloaded;
import com.github.commoble.tubesreloaded.network.PacketHandler;
import com.github.commoble.tubesreloaded.network.TubeBreakPacket;

import net.minecraft.block.BlockState;
import net.minecraft.block.SixWayBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

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
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		return TubeTileEntity.getTubeTEAt(world, pos)
			.map(tube -> this.onUseOnTube(world, pos, tube, context.getItem(), context.getPlayer(), context.getFace()))
			.orElseGet(() -> super.onItemUse(context));
	}
	
	private ActionResultType onUseOnTube(World world, BlockPos pos, @Nonnull TubeTileEntity tube, ItemStack stack, PlayerEntity player, Direction activatedSide)
	{
		if (!world.isRemote)
		{
			CompoundNBT nbt = stack.getChildTag(LAST_TUBE_DATA);
			BlockState state = world.getBlockState(pos);
			
			if (nbt == null)
			{
				if (state.getBlock() instanceof TubeBlock && !state.get(SixWayBlock.FACING_TO_PROPERTY_MAP.get(activatedSide)))
				{
					CompoundNBT newNBT = new CompoundNBT();
					newNBT.put(LAST_TUBE_POS, NBTUtil.writeBlockPos(pos));
					newNBT.putInt(LAST_TUBE_SIDE, activatedSide.ordinal());
					stack.setTagInfo(LAST_TUBE_DATA, newNBT);
				}
				
			}
			else // existing position stored in stack
			{
				BlockPos lastPos = NBTUtil.readBlockPos(nbt.getCompound(LAST_TUBE_POS));
				Direction lastSide = Direction.byIndex(nbt.getInt(LAST_TUBE_SIDE));
				// if player clicked the same tube twice, clear the last-used-position
				if (lastPos.equals(pos))
				{
					stack.removeChildTag(LAST_TUBE_DATA);
				}
				// if tube was already connected to the other position, remove connections
				else if (tube.hasRemoteConnection(lastPos))
				{
					TubeTileEntity.removeConnection(world, pos, lastPos);
					stack.removeChildTag(LAST_TUBE_DATA);
				}
				else // we clicked a different tube that doesn't have an existing connection to the original tube
				{
					// if the tube is already connected on this side, cancel the connection
					if (tube.hasRemoteConnection(activatedSide))
					{
						stack.removeChildTag(LAST_TUBE_DATA);
						if (player instanceof ServerPlayerEntity && world instanceof ServerWorld)
						{
							PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new TubeBreakPacket(new Vec3d(lastPos), new Vec3d(pos)));
							
							((ServerPlayerEntity)player).playSound(SoundEvents.ENTITY_WANDERING_TRADER_HURT, SoundCategory.BLOCKS, 0.5F, 2F);
						}
						return ActionResultType.SUCCESS;
					}
					// do a raytrace to check for interruptions
					Vec3d startVec = RaytraceHelper.getTubeSideCenter(lastPos, lastSide);
					Vec3d endVec = RaytraceHelper.getTubeSideCenter(pos, activatedSide);
					Vec3d hit = RaytraceHelper.getTubeRaytraceHit(startVec, endVec, world);
					BlockState lastState = world.getBlockState(lastPos);
					
					// if tube wasn't connected but they can't be connected due to a block in the way, interrupt the connection
					if (hit != null)
					{
						stack.removeChildTag(LAST_TUBE_DATA);
						if (player instanceof ServerPlayerEntity && world instanceof ServerWorld)
						{
							PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new TubeBreakPacket(startVec, endVec));
							((ServerWorld)world).spawnParticle((ServerPlayerEntity)player, RedstoneParticleData.REDSTONE_DUST, false, hit.x, hit.y, hit.z, 5, .05, .05, .05, 0);
							
							((ServerPlayerEntity)player).playSound(SoundEvents.ENTITY_WANDERING_TRADER_HURT, SoundCategory.BLOCKS, 0.5F, 2F);
						}
					}
					else if (state.getBlock() instanceof TubeBlock && !state.get(SixWayBlock.FACING_TO_PROPERTY_MAP.get(activatedSide)))
					{
						// if tube wasn't connected to the first tube or another tube, connect them if they're close enough
						if (pos.withinDistance(lastPos, TubesReloaded.serverConfig.max_remote_tube_connection_range.get())
							&& lastState.getBlock() instanceof TubeBlock && !lastState.get(SixWayBlock.FACING_TO_PROPERTY_MAP.get(lastSide)))
						{
							
							stack.removeChildTag(LAST_TUBE_DATA);
							TubeTileEntity.getTubeTEAt(world, lastPos)
								.ifPresent(lastPost -> {
									BlockPos originalConnection = lastPost.getConnectedPos(lastSide);
									
									// if the original tube was already connected on the given side, make sure to remove the original connection first
									if (originalConnection != null)
									{
										TubeTileEntity.removeConnection(world, lastPos, originalConnection);
									}
									
									TubeTileEntity.addConnection(world, tube, activatedSide, lastPost, lastSide);
								});
							stack.damageItem(1, player, thePlayer -> thePlayer.sendBreakAnimation(EquipmentSlotType.MAINHAND));
							
						}
						else // too far away, initiate a new connection from here
						{
							stack.setTagInfo(LAST_TUBE_DATA, NBTUtil.writeBlockPos(pos));
							// TODO give feedback to player
						}
					}
				}
			}
			world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS,
				0.2F + world.rand.nextFloat()*0.1F,
				0.7F + world.rand.nextFloat()*0.1F);
		}
		
		return ActionResultType.SUCCESS;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (!worldIn.isRemote)
		{
			Optional.ofNullable(stack.getChildTag(LAST_TUBE_DATA))
				.map(nbt -> NBTUtil.readBlockPos(nbt.getCompound(LAST_TUBE_POS)))
				.filter(pos -> shouldRemoveConnection(pos, worldIn, entityIn))
				.ifPresent(pos -> breakPendingConnection(stack, pos, entityIn, worldIn));
		}
	}
	
	public static boolean shouldRemoveConnection(BlockPos connectionPos, World world, Entity holder)
	{
		double maxDistance = TubesReloaded.serverConfig.max_remote_tube_connection_range.get();
		if (holder.getPositionVec().squareDistanceTo((new Vec3d(connectionPos).add(0.5,0.5,0.5))) > maxDistance*maxDistance)
		{
			return true;
		}
		TileEntity te = world.getTileEntity(connectionPos);
		return !(te instanceof TubeTileEntity);
	}
	
	public static void breakPendingConnection(ItemStack stack, BlockPos connectingPos, Entity holder, World world)
	{
		stack.removeChildTag(LAST_TUBE_DATA);
		if (holder instanceof ServerPlayerEntity)
		{
			PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)holder),
				new TubeBreakPacket(
					TubeTileEntity.getCenter(connectingPos),
					new Vec3d(holder.getPosX(), holder.getPosYEye(), holder.getPosZ())));
		}
	}
}
