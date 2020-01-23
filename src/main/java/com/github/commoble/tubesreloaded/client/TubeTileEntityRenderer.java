package com.github.commoble.tubesreloaded.client;

import java.util.Random;

import com.github.commoble.tubesreloaded.common.blocks.tube.ItemInTubeWrapper;
import com.github.commoble.tubesreloaded.common.blocks.tube.TubeTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TubeTileEntityRenderer extends TileEntityRenderer<TubeTileEntity>
{
	public TubeTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_)
	{
		super(p_i226006_1_);
	}

	@Override
	public void render(TubeTileEntity tube, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int intA, int intB)
	{
		// render tick happens independently of regular ticks and often more frequently
		if (!tube.inventory.isEmpty())
		{
			for (ItemInTubeWrapper wrapper : tube.inventory)
			{
				this.renderWrapper(tube, wrapper, partialTicks, matrix, buffer, intA);
			}
		}
		if (!tube.incoming_wrapper_buffer.isEmpty())
		{
			for (ItemInTubeWrapper wrapper : tube.incoming_wrapper_buffer)
			{
				this.renderWrapper(tube, wrapper, partialTicks, matrix, buffer, intA);
			}
		}
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
}
