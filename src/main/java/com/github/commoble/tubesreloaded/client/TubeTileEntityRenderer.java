package com.github.commoble.tubesreloaded.client;

import java.util.Random;

import com.github.commoble.tubesreloaded.blocks.tube.ItemInTubeWrapper;
import com.github.commoble.tubesreloaded.blocks.tube.RaytraceHelper;
import com.github.commoble.tubesreloaded.blocks.tube.TubeTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TubeTileEntityRenderer extends TileEntityRenderer<TubeTileEntity>
{
	public static final ResourceLocation TEXTURE_BEACON_BEAM = new ResourceLocation("tubesreloaded:textures/block/tube");
	public static final Material CAGE_TEXTURE = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation("tubesreloaded:textures/block/tube"));
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
		Vec3d startVec = TubeTileEntity.getConnectionVector(tube.getPos());
		for (BlockPos connectionPos : tube.getRemoteConnections().values())
		{
			Vec3d endVec = TubeTileEntity.getConnectionVector(connectionPos);
			Vec3d[] points = RaytraceHelper.getInterpolatedPoints(startVec, endVec);
			for (Vec3d point : points)
			{
				tube.getWorld().addParticle(ParticleTypes.END_ROD, point.x, point.y, point.z, 0D, 0D, 0D);
			}
		}
//		this.renderLongTube(tube, partialTicks, matrix, buffer, combinedLight, combinedOverlay);
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
		if (wrapper.freshlyInserted)	// first move
		{
			xEnd = 0F;
			yEnd = 0F;
			zEnd = 0F;
			xStart = xEnd - nextMove.getXOffset();
			yStart = yEnd - nextMove.getYOffset();
			zStart = zEnd - nextMove.getZOffset();
		}
		else	// any other move
		{
			xStart = 0F;
			yStart = 0F;
			zStart = 0F;
			xEnd = xStart + nextMove.getXOffset();
			yEnd = yStart + nextMove.getYOffset();;
			zEnd = zStart + nextMove.getZOffset();;
		}
		float xLerp = MathHelper.lerp(lerpFactor, xStart, xEnd);
		float yLerp = MathHelper.lerp(lerpFactor, yStart, yEnd);
		float zLerp = MathHelper.lerp(lerpFactor, zStart, zEnd);

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
			float xTranslate = xLerp + xAdjustment + 0.5F;
			float yTranslate = yLerp + yAdjustment + 0.4375F;
			float zTranslate = zLerp + zAdjustment + 0.5F;
			matrix.translate(xTranslate, yTranslate, zTranslate);// aggregate is centered
			matrix.scale(0.5F, 0.5F, 0.5F);
			
			itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND, intA, OverlayTexture.DEFAULT_LIGHT, matrix, buffer);
			matrix.pop();
		}
		itemRenderer.zLevel += 50F;

		matrix.pop();
	}
	

	public void renderLongTube(TubeTileEntity tube, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay)
	{
		TextureAtlasSprite textureatlassprite = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation("tubesreloaded:block/tube")).getSprite();
		float renderTime = tube.getWorld().getGameTime() + partialTicks;
		
		float wobbleX = (float) Math.cos(renderTime/4F);
		float wobbleZ = (float) Math.sin(renderTime/4F);

		for (int side=0; side<4; side++)
		{
			matrix.push();
			
			double sideAngle = side * Math.PI / 2D;
			float centerX = 0.5f;
			float centerZ = 0.5f;
			double xOff = Math.cos(sideAngle)*2D/16D;
			double zOff = Math.sin(sideAngle)*2D/16D;

			
			IVertexBuilder ivertexbuilder = buffer.getBuffer(Atlases.getCutoutBlockType());
			float totalMinU = textureatlassprite.getMinU();
			float totalMinV = textureatlassprite.getMinV();
			float totalMaxU = textureatlassprite.getMaxU();
			float totalMaxV = textureatlassprite.getMaxV();
			float texWidth = totalMaxU - totalMinU;
			float texHeight = totalMaxV - totalMinV;
			float tubeStartX = ((6F / 16F) * texWidth) + totalMinU;
			float tubeStartY = totalMinV;
			float tubeWidth = (4F / 16F) * texWidth;
			float tubeHeight = (4F / 16F) * texHeight;
			float minU = tubeStartX;
			float minV = tubeStartY;
			float maxU = tubeStartX + tubeWidth;
			float maxV = tubeStartY + tubeHeight;

			float x1 = centerX + (float)(side % 2 == 0 ? xOff : zOff);
			float x2 = centerX + (float)(side % 2 == 0 ? xOff : -zOff);
			float y1 = 12F/16F;
			float y2 = 4F;
			float z1 = centerZ + (float)(side % 2 == 0 ? xOff : -zOff);
			float z2 = centerZ + (float)(side % 2 == 0 ? -xOff : -zOff);
			
			
			
			MatrixStack.Entry matrixEntry = matrix.getLast();
			
			putVertex(matrixEntry, ivertexbuilder, x1, y1, z1, minU, maxV);
			putVertex(matrixEntry, ivertexbuilder, x2, y1, z2, maxU, maxV);
			putVertex(matrixEntry, ivertexbuilder, x2 + wobbleX, y2, z2 + wobbleZ, maxU, minV);
			putVertex(matrixEntry, ivertexbuilder, x1 + wobbleX, y2, z1 + wobbleZ, minU, minV);

			matrix.pop();
		}

	}

	private static void putVertex(MatrixStack.Entry matrixEntryIn, IVertexBuilder bufferIn, float x, float y, float z, float texU, float texV)
	{
		bufferIn.pos(matrixEntryIn.getPositionMatrix(), x, y, z).color(255, 255, 255, 255).tex(texU, texV).overlay(0, 10).lightmap(240)
			.normal(matrixEntryIn.getNormalMatrix(), 0.0F, 1.0F, 0.0F).endVertex();
	}


}
