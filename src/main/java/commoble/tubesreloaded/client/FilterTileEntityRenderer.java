package commoble.tubesreloaded.client;

import com.mojang.blaze3d.matrix.MatrixStack;

import commoble.tubesreloaded.blocks.filter.FilterBlock;
import commoble.tubesreloaded.blocks.filter.FilterTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3f;

public class FilterTileEntityRenderer extends TileEntityRenderer<FilterTileEntity>
{
	public FilterTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_)
	{
		super(p_i226006_1_);
	}

	@Override
	public void render(FilterTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int intA, int intB)
	{
		if (te.filterStack.getCount() > 0)
		{
			this.renderItem(te.filterStack, te.getBlockState().get(FilterBlock.FACING), matrix, buffer, intA);
		}
	}

	private void renderItem(ItemStack stack, Direction facing, MatrixStack matrix, IRenderTypeBuffer buffer, int intA)
	{
		ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

		matrix.push();	// push

		matrix.translate(0.501D, 0.502D, 0.503D);	// translation
		matrix.scale(0.9F, 0.9F, 0.9F);	// scale
		if (facing.getAxis() == Axis.X)
		{
			matrix.rotate(Vector3f.YP.rotationDegrees(90F));	// rotate 90 degrees about y-axis
			//matrix.func_227863_a_(90D, 0D, 1D, 0D);
		}

		
		//renderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
		renderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, intA, OverlayTexture.NO_OVERLAY, matrix, buffer);
		
		

		matrix.pop();
	}
}
