package com.github.commoble.tubesreloaded.client;

import java.util.Random;

import com.github.commoble.tubesreloaded.common.tube.ItemInTubeWrapper;
import com.github.commoble.tubesreloaded.common.tube.TubeTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TubeTileEntityRenderer extends TileEntityRenderer<TubeTileEntity>
{
	public void render(TubeTileEntity tube, double x, double y, double z, float partialTicks, int destroyStage)
	{
		// render tick happens independantly of regular ticks and often more frequently
		if (!tube.inventory.isEmpty())
		{
			for (ItemInTubeWrapper wrapper : tube.inventory)
			{
				this.renderWrapper(tube, wrapper, x, y, z, partialTicks);
			}
		}
		if (!tube.incoming_wrapper_buffer.isEmpty())
		{
			for (ItemInTubeWrapper wrapper : tube.incoming_wrapper_buffer)
			{
				this.renderWrapper(tube, wrapper, x, y, z, partialTicks);
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
	public void renderWrapper(TubeTileEntity tube, ItemInTubeWrapper wrapper, double x, double y, double z, float partialTicks)
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
		random.setSeed((long) i);
		
		
		GlStateManager.pushMatrix();
		int renderedItemCount = this.getModelCount(itemstack);
		float xStart, yStart, zStart, xEnd, yEnd, zEnd;
		float lerpFactor = ((float)wrapper.ticksElapsed + partialTicks) / (float)wrapper.maximumDurationInTube;	// factor in range [0,1)
		if (wrapper.freshlyInserted)	// first move
		{
			xEnd = (float)x;
			yEnd = (float)y;
			zEnd = (float)z;
			xStart = xEnd - (float)nextMove.getXOffset();
			yStart = yEnd - (float)nextMove.getYOffset();
			zStart = zEnd - (float)nextMove.getZOffset();
		}
		else	// any other move
		{
			xStart = (float)x;
			yStart = (float)y;
			zStart = (float)z;
			xEnd = xStart + (float)nextMove.getXOffset();
			yEnd = yStart + (float)nextMove.getYOffset();;
			zEnd = zStart + (float)nextMove.getZOffset();;
		}
		float xLerp = MathHelper.lerp(lerpFactor, xStart, xEnd);
		float yLerp = MathHelper.lerp(lerpFactor, yStart, yEnd);
		float zLerp = MathHelper.lerp(lerpFactor, zStart, zEnd);

		itemRenderer.zLevel -= 50F;
		for (int currentModelIndex = 0; currentModelIndex < renderedItemCount; ++currentModelIndex)
		{
			GlStateManager.pushMatrix();
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
			GlStateManager.translatef(xTranslate, yTranslate, zTranslate);// aggregate is centered
			GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			
			itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
			GlStateManager.popMatrix();
		}
		itemRenderer.zLevel += 50F;

		GlStateManager.popMatrix();
	}
}
