package com.github.commoble.tubesreloaded.client;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.github.commoble.tubesreloaded.common.brasstube.BrassTubeTileEntity;
import com.github.commoble.tubesreloaded.common.brasstube.ItemInTubeWrapper;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TubeTileEntityRenderer extends TileEntityRenderer<BrassTubeTileEntity>
{
	public void render(BrassTubeTileEntity tube, double x, double y, double z, float partialTicks, int destroyStage)
	{
		// render tick happens independantly of regular ticks and often more frequently
		if (!tube.inventory.isEmpty())
		{
			Queue<ItemInTubeWrapper> remainingWrappers = new LinkedList<ItemInTubeWrapper>();
			for (ItemInTubeWrapper wrapper : tube.inventory)
			{
				this.renderWrapper(tube, x, y, z, wrapper);
				//System.out.println(wrapper.ticksRemaining);

				Minecraft mc = Minecraft.getInstance();
				float ticksSinceLastRenderTick = mc.getTickLength();
				wrapper.partialTickElapsed += ticksSinceLastRenderTick;
				if (wrapper.partialTickElapsed >= 1F)
				{
					wrapper.partialTickElapsed -= 1F;
					if (++wrapper.ticksElapsed >= wrapper.maximumDurationInTube)
					{
						tube.sendWrapperOnward(wrapper);
					}
					else
					{
						remainingWrappers.add(wrapper);
					}
				}
				else
				{
					remainingWrappers.add(wrapper);
				}

			}
			tube.inventory = remainingWrappers;
		}
	}

	private void renderWrapper(BrassTubeTileEntity tube, double x, double y, double z, ItemInTubeWrapper wrapper)
	{
		this.doRender(tube, wrapper, x, y, z);
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
	public void doRender(BrassTubeTileEntity tube, ItemInTubeWrapper wrapper, double x, double y, double z)
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
//		GlStateManager.enableRescaleNormal();
//		GlStateManager.alphaFunc(516, 0.1F);
//		GlStateManager.enableBlend();
//		RenderHelper.enableStandardItemLighting();
////		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
//				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
//				GlStateManager.DestFactor.ZERO);
//		GlStateManager.pushMatrix();
//		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(itemstack, tube.getWorld(),
				(LivingEntity) null);
		int renderedItemCount = this.getModelCount(itemstack);
		float xStart = (float)x;
		float yStart = (float)y;
		float zStart = (float)z;
		float xEnd = xStart + (float)nextMove.getXOffset();
		float yEnd = yStart + (float)nextMove.getYOffset();;
		float zEnd = zStart + (float)nextMove.getZOffset();;
		float lerpFactor = ((float)wrapper.ticksElapsed + wrapper.partialTickElapsed) / (float)wrapper.maximumDurationInTube;	// factor in range [0,1)
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
				xAdjustment = (random.nextFloat() * 2.0F - 1.0F) * 0.05F;
				yAdjustment = (random.nextFloat() * 2.0F - 1.0F) * 0.05F;
				zAdjustment = (random.nextFloat() * 2.0F - 1.0F) * 0.05F;
				//GlStateManager.translatef((float)x+xAdjustment, (float)y+yAdjustment, (float)z+zAdjustment);
			}
			float xTranslate = xLerp + xAdjustment + 0.5F;
			float yTranslate = yLerp + yAdjustment + 0.4375F;
			float zTranslate = zLerp + zAdjustment + 0.5F;
			GlStateManager.translatef(xTranslate, yTranslate, zTranslate);// aggregate is centered
			GlStateManager.scalef(0.4F, 0.4F, 0.4F);
			

//			IBakedModel transformedModel = net.minecraftforge.client.ForgeHooksClient
//					.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
			itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND);
			GlStateManager.popMatrix();
		}
		itemRenderer.zLevel += 50F;

//		GlStateManager.popMatrix();
//		GlStateManager.disableRescaleNormal();
//		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
}
