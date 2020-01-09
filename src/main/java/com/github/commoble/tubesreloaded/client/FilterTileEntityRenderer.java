package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.common.blocks.filter.FilterBlock;
import com.github.commoble.tubesreloaded.common.blocks.filter.FilterTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class FilterTileEntityRenderer extends TileEntityRenderer<FilterTileEntity>
{
	public FilterTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_)
	{
		super(p_i226006_1_);
	}

	@Override
	public void func_225616_a_(FilterTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int intA, int intB)
	{
		if (te.filterStack.getCount() > 0)
		{
			this.renderItem(te.filterStack, te.getBlockState().get(FilterBlock.FACING), matrix, buffer, intA);
		}
	}

	private void renderItem(ItemStack stack, Direction facing, MatrixStack matrix, IRenderTypeBuffer buffer, int intA)
	{
		ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

		matrix.func_227860_a_();	// push

		matrix.func_227861_a_(0.501D, 0.502D, 0.503D);	// translation
		matrix.func_227862_a_(0.9F, 0.9F, 0.9F);	// scale
		if (facing.getAxis() == Axis.X)
		{
			matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(90F));	// rotate 90 degrees about y-axis
			//matrix.func_227863_a_(90D, 0D, 1D, 0D);
		}

		
		//renderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		renderer.func_229110_a_(stack, ItemCameraTransforms.TransformType.FIXED, intA, OverlayTexture.field_229196_a_, matrix, buffer);
		
		

		matrix.func_227865_b_();	// pop
	}
}
