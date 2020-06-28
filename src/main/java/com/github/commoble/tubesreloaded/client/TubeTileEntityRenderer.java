package com.github.commoble.tubesreloaded.client;

import java.util.Map;
import java.util.Random;

import com.github.commoble.tubesreloaded.blocks.tube.ItemInTubeWrapper;
import com.github.commoble.tubesreloaded.blocks.tube.RaytraceHelper;
import com.github.commoble.tubesreloaded.blocks.tube.RemoteConnection;
import com.github.commoble.tubesreloaded.blocks.tube.TubeBlock;
import com.github.commoble.tubesreloaded.blocks.tube.TubeTileEntity;
import com.github.commoble.tubesreloaded.blocks.tube.TubingPliersItem;
import com.github.commoble.tubesreloaded.util.DirectionTransformer;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TubeTileEntityRenderer extends TileEntityRenderer<TubeTileEntity>
{
	private final ModelRenderer field_228875_k_;

	public TubeTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_)
	{
		super(p_i226006_1_);
		this.field_228875_k_ = new ModelRenderer(32, 16, 0, 0);
		this.field_228875_k_.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
	}

	@Override
	public void render(TubeTileEntity tube, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay)
	{
		// render tick happens independently of regular ticks and often more frequently
		if (!tube.inventory.isEmpty())
		{
			for (ItemInTubeWrapper wrapper : tube.inventory)
			{
				this.renderWrapper(tube, wrapper, partialTicks, matrix, buffer, combinedLight);
			}
		}
		if (!tube.incoming_wrapper_buffer.isEmpty())
		{
			for (ItemInTubeWrapper wrapper : tube.incoming_wrapper_buffer)
			{
				this.renderWrapper(tube, wrapper, partialTicks, matrix, buffer, combinedLight);
			}
		}
		this.renderLongTubes(tube, partialTicks, matrix, buffer, combinedLight, combinedOverlay);
	}

	// ** copied from entity ItemRenderer **//

	protected int getModelCount(ItemStack stack)
	{
		int i = 1;
		if (stack.getCount() > 48)
		{
			i = 5;
		}
		else if (stack.getCount() > 32)
		{
			i = 4;
		}
		else if (stack.getCount() > 16)
		{
			i = 3;
		}
		else if (stack.getCount() > 1)
		{
			i = 2;
		}

		return i;
	}

	/**
	 * Renders an itemstack
	 */
	public void renderWrapper(TubeTileEntity tube, ItemInTubeWrapper wrapper, float partialTicks,
		MatrixStack matrix, IRenderTypeBuffer buffer, int intA)
	{
		Direction nextMove = wrapper.remainingMoves.peek();
		if (nextMove == null)
			return;
		ItemStack itemstack = wrapper.stack;
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer(); // itemrenderer knows how to render items
		Random random = new Random();
		Item item = itemstack.getItem();
		int i = itemstack.isEmpty() ? 187 : Item.getIdFromItem(item) + itemstack.getDamage(); // the random is used to
																								// offset sub-items
		random.setSeed(i);
		

		matrix.push();
		int renderedItemCount = this.getModelCount(itemstack);
		float xStart, yStart, zStart, xEnd, yEnd, zEnd;
		float lerpFactor = (wrapper.ticksElapsed + partialTicks) / wrapper.maximumDurationInTube;	// factor in range [0,1)
		Vector3d renderOffset;
		float remoteScale = 1F; // extra scaling if rendering in a narrow remote tube
		if (wrapper.freshlyInserted)	// first move
		{
			xEnd = 0F;
			yEnd = 0F;
			zEnd = 0F;
			xStart = xEnd - nextMove.getXOffset();
			yStart = yEnd - nextMove.getYOffset();
			zStart = zEnd - nextMove.getZOffset();
			float xLerp = MathHelper.lerp(lerpFactor, xStart, xEnd);
			float yLerp = MathHelper.lerp(lerpFactor, yStart, yEnd);
			float zLerp = MathHelper.lerp(lerpFactor, zStart, zEnd);
			renderOffset = new Vector3d(xLerp, yLerp, zLerp);
		}
		else	// any other move
		{
			renderOffset = getItemRenderOffset(tube, nextMove, lerpFactor);
			remoteScale = (float)getItemRenderScale(tube, nextMove, lerpFactor);
		}

		itemRenderer.zLevel -= 50F;
		for (int currentModelIndex = 0; currentModelIndex < renderedItemCount; ++currentModelIndex)
		{
			matrix.push();
			float xAdjustment = 0F;
			float yAdjustment = 0F;
			float zAdjustment = 0F;
			if (currentModelIndex > 0)
			{
				xAdjustment = (random.nextFloat() * 2.0F - 1.0F) * 0.01F;
				yAdjustment = (random.nextFloat() * 2.0F - 1.0F) * 0.01F;
				zAdjustment = (random.nextFloat() * 2.0F - 1.0F) * 0.01F;
			}
			float xTranslate = (float) (renderOffset.x + xAdjustment + 0.5F);
			float yTranslate = (float) (renderOffset.y + yAdjustment + 0.4375F);
			float zTranslate = (float) (renderOffset.z + zAdjustment + 0.5F);
			matrix.translate(xTranslate, yTranslate, zTranslate);// aggregate is centered
			float scale = remoteScale * 0.5F;
			matrix.scale(scale, scale, scale);
			
			itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND, intA, OverlayTexture.NO_OVERLAY, matrix, buffer);
			matrix.pop();
		}
		itemRenderer.zLevel += 50F;

		matrix.pop();
	}
	
	/**
	 * Get the render offset to render a travelling item
	 * @param tube
	 * @param travelDirection
	 * @param lerpFactor
	 * @return
	 */
	public static Vector3d getItemRenderOffset(TubeTileEntity tube, Direction travelDirection, float lerpFactor)
	{
		return tube.getRemoteConnection(travelDirection)
			.map(connection -> getRemoteItemRenderOffset(connection, travelDirection, tube.getPos(), lerpFactor))
			.orElse(getAdjacentRenderOffset(travelDirection, lerpFactor));
	}
	
	public static double getItemRenderScale(TubeTileEntity tube, Direction travelDirection, float lerpFactor)
	{
		return tube.getRemoteConnection(travelDirection)
			.map(connection -> getRemoteItemRenderScale(connection, travelDirection, tube.getPos(), lerpFactor))
			.orElse(1D);
	}
	
	/**
	 * Get the render offset to render an item travelling to a remote tube
	 * @param connection
	 * @param travelDirection
	 * @param fromPos
	 * @param lerpFactor
	 * @return
	 */
	public static Vector3d getRemoteItemRenderOffset(RemoteConnection connection, Direction travelDirection, BlockPos fromPos, float lerpFactor)
	{
		Vector3d startVec = TubeTileEntity.getCenter(fromPos);
		BlockPos endPos = connection.toPos;
		Vector3d endVec = TubeTileEntity.getCenter(endPos);
		Direction endSide = connection.toSide;
		Vector3d startSideVec = RaytraceHelper.getTubeSideCenter(fromPos, travelDirection);
		Vector3d endSideVec = RaytraceHelper.getTubeSideCenter(endPos, endSide);
		// render item exiting origin tube
		if (lerpFactor < 0.25F)
		{
			Vector3d sideOffset = startSideVec.subtract(startVec);
			float subLerp = lerpFactor / 0.25F;
			double x = MathHelper.lerp(subLerp, 0, sideOffset.x);
			double y = MathHelper.lerp(subLerp, 0, sideOffset.y);
			double z = MathHelper.lerp(subLerp, 0, sideOffset.z);
			return new Vector3d(x,y,z);
		}
		else if (lerpFactor < 0.75F) // render item between tubes
		{
			float subLerp = (lerpFactor - 0.25F) / 0.5F; // lerp with 0% = 0.25, 100% = 0.75
			double x = MathHelper.lerp(subLerp, startSideVec.x, endSideVec.x);
			double y = MathHelper.lerp(subLerp, startSideVec.y, endSideVec.y);
			double z = MathHelper.lerp(subLerp, startSideVec.z, endSideVec.z);
			// these values are in absolute coords
			// want to make them local to the renderer
			return new Vector3d(x - startVec.x, y - startVec.y, z - startVec.z);
			
		}
		else // render item entering destination tube
		{
//			return endVec.subtract(startVec);
			float subLerp = (lerpFactor - 0.75F) / 0.25F; // lerp with 0% = 0.75, 100% = 1.0
			double x = MathHelper.lerp(subLerp, endSideVec.x, endVec.x);
			double y = MathHelper.lerp(subLerp, endSideVec.y, endVec.y);
			double z = MathHelper.lerp(subLerp, endSideVec.z, endVec.z);
			// these values are in absolute coords
			// want to make them local to the renderer
			return new Vector3d(x - startVec.x, y - startVec.y, z - startVec.z);
		}
	}
	
	public static double getRemoteItemRenderScale(RemoteConnection connection, Direction travelDirection, BlockPos fromPos, float lerpFactor)
	{
		Direction remoteFace = connection.toSide;
		BlockPos remotePos = connection.toPos;
		double smallestScale = Math.min(getRemoteItemRenderScale(travelDirection, fromPos, remotePos), getRemoteItemRenderScale(remoteFace, remotePos, fromPos));
		if (lerpFactor < 0.25F)
		{
			double subLerp = (lerpFactor - 0.25F) / 0.25F;
			return MathHelper.lerp(subLerp, 1F, smallestScale);
		}
		else if (lerpFactor < 0.75F)
		{
			return smallestScale;
		}
		else
		{
			double subLerp = (lerpFactor - 0.75F) / 0.25F;
			return MathHelper.lerp(subLerp, smallestScale, 1F);
		}
	}
	
	public static double getRemoteItemRenderScale(Direction startSide, BlockPos startPos, BlockPos toPos)
	{
		Vector3i dist = toPos.subtract(startPos);
		Axis travelAxis = startSide.getAxis();
		Axis[] orthagonalAxes = DirectionTransformer.ORTHAGONAL_AXES[travelAxis.ordinal()];
		double parallelDistance = startSide.getAxis().getCoordinate(dist.getX(), dist.getY(), dist.getZ());
		double parallelDistanceSquared = parallelDistance * parallelDistance;
		double orthagonalDistanceSquared = 0;
		int axisCount = orthagonalAxes.length;
		for (int i=0; i<axisCount; i++)
		{
			int orthagonalDist = orthagonalAxes[i].getCoordinate(dist.getX(), dist.getY(), dist.getZ());
			orthagonalDistanceSquared += (orthagonalDist*orthagonalDist);
		}
		
		return Math.exp(- (orthagonalDistanceSquared / parallelDistanceSquared));
	}
	
	/**
	 * Get the render offset to render an item travelling to an adjacent tube
	 * @param travelDirection
	 * @param lerpFactor
	 * @return
	 */
	public static Vector3d getAdjacentRenderOffset(Direction travelDirection, float lerpFactor)
	{
		double xEnd = travelDirection.getXOffset();
		double yEnd = travelDirection.getYOffset();
		double zEnd = travelDirection.getZOffset();
		double xLerp = MathHelper.lerp(lerpFactor, 0, xEnd);
		double yLerp = MathHelper.lerp(lerpFactor, 0, yEnd);
		double zLerp = MathHelper.lerp(lerpFactor, 0, zEnd);
		return new Vector3d(xLerp, yLerp, zLerp);
	}
	

	public void renderLongTubes(TubeTileEntity tube, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay)
	{
		World world = tube.getWorld();
		BlockPos startPos = tube.getPos();
		Block block = tube.getBlockState().getBlock();
		if (block instanceof TubeBlock)
		{
			for (Map.Entry<Direction, RemoteConnection> entry : tube.getRemoteConnections().entrySet())
			{
				RemoteConnection connection = entry.getValue();
				// connections are stored in both tubes but only one tube should render each connection
				if (connection.isPrimary)
				{

					BlockPos endPos = connection.toPos;
					Direction startFace = entry.getKey();
					Direction endFace = connection.toSide;
					
					TubeQuadRenderer.renderQuads(world, partialTicks, startPos, endPos, startFace, endFace, matrix, buffer, ((TubeBlock)block));
				
				}
			}
			
			@SuppressWarnings("resource")
			PlayerEntity player = Minecraft.getInstance().player;
			if (player != null)
			{
				for (Hand hand : Hand.values())
				{
					ItemStack stack = player.getHeldItem(hand);
					if (stack.getItem() instanceof TubingPliersItem)
					{
						CompoundNBT nbt = stack.getChildTag(TubingPliersItem.LAST_TUBE_DATA);
						if (nbt != null)
						{
							BlockPos posOfLastTubeOfPlayer = NBTUtil.readBlockPos(nbt.getCompound(TubingPliersItem.LAST_TUBE_POS));
							Direction sideOfLastTubeOfPlayer = Direction.byIndex(nbt.getInt(TubingPliersItem.LAST_TUBE_SIDE));
							if (posOfLastTubeOfPlayer.equals(tube.getPos()))
							{

								EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
								int handSideID = -(hand == Hand.MAIN_HAND ? -1 : 1) * (player.getPrimaryHand() == HandSide.RIGHT ? 1 : -1);

								float swingProgress = player.getSwingProgress(partialTicks);
								float swingZ = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
								float playerAngle = MathHelper.lerp(partialTicks, player.prevRenderYawOffset, player.renderYawOffset) * ((float) Math.PI / 180F);
								double playerAngleX = MathHelper.sin(playerAngle);
								double playerAngleZ = MathHelper.cos(playerAngle);
								double handOffset = handSideID * 0.35D;
								double handX;
								double handY;
								double handZ;
								float eyeHeight;
								
								// first person
								if ((renderManager.options == null || renderManager.options.thirdPersonView <= 0))
								{
									double fov = renderManager.options.fov;
									fov = fov / 100.0D;
									Vector3d handVector = new Vector3d(-0.14 + handSideID * -0.36D * fov, -0.12 + -0.045D * fov, 0.4D);
									handVector = handVector.rotatePitch(-MathHelper.lerp(partialTicks, player.prevRotationPitch, player.rotationPitch) * ((float) Math.PI / 180F));
									handVector = handVector.rotateYaw(-MathHelper.lerp(partialTicks, player.prevRotationYaw, player.rotationYaw) * ((float) Math.PI / 180F));
									handVector = handVector.rotateYaw(swingZ * 0.5F);
									handVector = handVector.rotatePitch(-swingZ * 0.7F);
									handX = MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX()) + handVector.x;
									handY = MathHelper.lerp(partialTicks, player.prevPosY, player.getPosY()) + handVector.y + 0.3F;
									handZ = MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ()) + handVector.z;
									eyeHeight = player.getEyeHeight();
								}
								
								// third person
								else
								{
									handX = MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX()) - playerAngleZ * handOffset - playerAngleX * 0.8D;
									handY = -0.2 + player.prevPosY + player.getEyeHeight() + (player.getPosY() - player.prevPosY) * partialTicks - 0.45D;
									handZ = MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ()) - playerAngleX * handOffset + playerAngleZ * 0.8D;
									eyeHeight = player.isCrouching() ? -0.1875F : 0.0F;
								}
								Vector3d renderPlayerVec = new Vector3d(handX, handY + eyeHeight, handZ);
								Vector3d startVec = RaytraceHelper.getTubeSideCenter(posOfLastTubeOfPlayer, sideOfLastTubeOfPlayer);
								Vector3d endVec = renderPlayerVec;
								Vector3d[] points = RaytraceHelper.getInterpolatedPoints(startVec, endVec);
								for (Vector3d point : points)
								{
									world.addParticle(ParticleTypes.UNDERWATER, point.x, point.y, point.z, 0D, 0D, 0D);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isGlobalRenderer(TubeTileEntity te)
	{
		return true;
	}


}
