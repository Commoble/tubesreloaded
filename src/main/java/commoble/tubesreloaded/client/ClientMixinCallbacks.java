package commoble.tubesreloaded.client;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import commoble.tubesreloaded.ClientProxy;
import commoble.tubesreloaded.blocks.tube.RaytraceHelper;
import commoble.tubesreloaded.blocks.tube.TubeTileEntity;
import commoble.tubesreloaded.blocks.tube.TubesInChunk;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.TubesReloadedBlockItemHelper;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class ClientMixinCallbacks
{
	public static void onBlockItemUse(ItemUseContext itemContext, CallbackInfoReturnable<ActionResultType> info)
	{
		ClientProxy.INSTANCE.ifPresent(proxy -> onClientBlockItemUse(proxy, itemContext, info));
	}
	
	@SuppressWarnings("deprecation")
	private static void onClientBlockItemUse(ClientProxy proxy, ItemUseContext itemContext, CallbackInfoReturnable<ActionResultType> info)
	{
		World world = itemContext.getWorld();
		ItemStack stack = itemContext.getItem();
		Item item = stack.getItem();

		// we need to check world side because physical clients can have server worlds
		if (world.isRemote && item instanceof BlockItem)
		{
			BlockItemUseContext context = new BlockItemUseContext(itemContext);
			BlockPos pos = context.getPos();
			BlockItem blockItem = (BlockItem)item;
			BlockState placementState = TubesReloadedBlockItemHelper.getStateForPlacement(blockItem, context);
			
			if (placementState != null)
			{
				Set<ChunkPos> chunkPositions = TubesInChunk.getRelevantChunkPositionsNearPos(pos);
				
				chunkLoop:
				for (ChunkPos chunkPos : chunkPositions)
				{
					if (world.isBlockLoaded(chunkPos.asBlockPos()))
					{
						Set<BlockPos> tubePositions = proxy.getTubesInChunk(chunkPos);
						
						Set<BlockPos> checkedTubePositions = new HashSet<BlockPos>();
						for (BlockPos tubePos : tubePositions)
						{
							TileEntity te = world.getTileEntity(tubePos);
							if (te instanceof TubeTileEntity)
							{
								Vector3d hit = RaytraceHelper.doesBlockStateIntersectTubeConnections(te.getPos(), pos, new FakeWorldForTubeRaytrace(world, pos, placementState), placementState, checkedTubePositions, ((TubeTileEntity)te).getRemoteConnections());
								if (hit != null)
								{
									PlayerEntity player = context.getPlayer();
									if (player != null)
									{
										world.addParticle(RedstoneParticleData.REDSTONE_DUST, hit.x, hit.y, hit.z, 0.05D, 0.05D, 0.05D);
										player.playSound(SoundEvents.ENTITY_WANDERING_TRADER_HURT, SoundCategory.BLOCKS, 0.5F, 2F);
	//									serverPlayer.connection.sendPacket(new SEntityEquipmentPacket(serverPlayer.getEntityId(), ImmutableList.of(Pair.of(EquipmentSlotType.MAINHAND, serverPlayer.getHeldItem(Hand.MAIN_HAND)))));
	//									((ServerWorld)world).spawnParticle(serverPlayer, RedstoneParticleData.REDSTONE_DUST, false, hit.x, hit.y, hit.z, 5, .05, .05, .05, 0);
	//									serverPlayer.playSound(SoundEvents.ENTITY_WANDERING_TRADER_HURT, SoundCategory.BLOCKS, 0.5F, 2F);
									}
									info.setReturnValue(ActionResultType.SUCCESS);
									break chunkLoop; // "return" here just continues the inner loop for some reason
								}
								else
								{
									checkedTubePositions.add(tubePos);
								}
							}
						}
						
					}
				}
			}
		}
	}
}
