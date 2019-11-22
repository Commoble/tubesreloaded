package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.common.blocks.filter.FilterBlock;
import com.github.commoble.tubesreloaded.common.blocks.filter.FilterTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class FilterTileEntityRenderer extends TileEntityRenderer<FilterTileEntity>
{
	@Override
	public void render(FilterTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (te.filterStack.getCount() > 0)
		{
			this.renderItem(te.filterStack, x, y, z, te.getBlockState().get(FilterBlock.FACING));
		}
	}

	private void renderItem(ItemStack stack, double x, double y, double z, Direction facing)
	{
		ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

		GlStateManager.pushMatrix();

		GlStateManager.translated(x + 0.501D, y + 0.502D, z + 0.503D);
		GlStateManager.scaled(0.9D, 0.9D, 0.9D);
		if (facing.getAxis() == Axis.X)
		{
			GlStateManager.rotated(90D, 0D, 1D, 0D);
		}

		renderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);

		GlStateManager.popMatrix();
	}
}
