package com.github.commoble.tubesreloaded.event;

import java.util.HashSet;
import java.util.Set;

import com.github.commoble.tubesreloaded.TubesReloaded;
import com.github.commoble.tubesreloaded.blocks.tube.TubeTileEntity;
import com.github.commoble.tubesreloaded.blocks.tube.TubesInChunk;
import com.github.commoble.tubesreloaded.blocks.tube.TubesInChunkCapability;
import com.github.commoble.tubesreloaded.registry.Names;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = TubesReloaded.MODID, bus=Bus.FORGE)
public class CommonForgeEvents
{

	@SubscribeEvent
	public static void onAttachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event)
	{
		event.addCapability(new ResourceLocation(TubesReloaded.MODID, Names.TUBES_IN_CHUNK), new TubesInChunk());
	}
	

	@SubscribeEvent
	public static void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event)
	{
		BlockPos pos = event.getPos();
		IWorld iworld = event.getWorld();
		BlockState state = event.getState();
		if (iworld instanceof World && !iworld.isRemote())
		{
			@SuppressWarnings("resource")
			World world = (World)iworld;
			
			Set<ChunkPos> chunkPositions = TubesInChunk.getRelevantChunkPositionsNearPos(pos);
			
			for (ChunkPos chunkPos : chunkPositions)
			{
				if (world.isBlockLoaded(chunkPos.asBlockPos()))
				{
					Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
					chunk.getCapability(TubesInChunkCapability.INSTANCE).ifPresent(tubes ->
					{
						Set<BlockPos> checkedTubePositions = new HashSet<BlockPos>();
						for (BlockPos tubePos : tubes.getPositions())
						{
							TileEntity te = world.getTileEntity(tubePos);
							if (te instanceof TubeTileEntity)
							{
								Vec3d hit = ((TubeTileEntity)te).doesBlockStateIntersectConnection(pos, state, checkedTubePositions);
								if (hit != null)
								{
									event.setCanceled(true);
									Entity entity = event.getEntity();
									if (entity instanceof ServerPlayerEntity)
									{
										ServerPlayerEntity serverPlayer = (ServerPlayerEntity)entity;
										serverPlayer.connection.sendPacket(new SEntityEquipmentPacket(serverPlayer.getEntityId(), EquipmentSlotType.MAINHAND, serverPlayer.getHeldItem(Hand.MAIN_HAND)));
										((ServerWorld)world).spawnParticle(serverPlayer, RedstoneParticleData.REDSTONE_DUST, false, hit.x, hit.y, hit.z, 5, .05, .05, .05, 0);
										serverPlayer.playSound(SoundEvents.ENTITY_WANDERING_TRADER_HURT, SoundCategory.BLOCKS, 0.5F, 2F);
									}
									return;
								}
								else
								{
									checkedTubePositions.add(tubePos);
								}
							}
						}
					});
				}
			}
		}
	}
}
