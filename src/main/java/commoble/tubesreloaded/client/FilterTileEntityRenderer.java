package commoble.tubesreloaded.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import commoble.tubesreloaded.blocks.filter.FilterBlock;
import commoble.tubesreloaded.blocks.filter.FilterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;

public class FilterTileEntityRenderer implements BlockEntityRenderer<FilterBlockEntity>
{
	public FilterTileEntityRenderer(BlockEntityRendererProvider.Context context)
	{
	}

	@Override
	public void render(FilterBlockEntity filter, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int intA, int intB)
	{
		if (filter.filterStack.getCount() > 0)
		{
			this.renderItem(filter.filterStack, filter.getBlockState().getValue(FilterBlock.FACING), matrix, buffer, intA, (int)filter.getBlockPos().asLong());
		}
	}

	private void renderItem(ItemStack stack, Direction facing, PoseStack matrix, MultiBufferSource buffer, int intA, int renderSeed)
	{
		ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

		matrix.pushPose();

		matrix.translate(0.501D, 0.502D, 0.503D);
		matrix.scale(0.9F, 0.9F, 0.9F);
		if (facing.getAxis() == Axis.X)
		{
			matrix.mulPose(Vector3f.YP.rotationDegrees(90F));	// rotate 90 degrees about y-axis
		}

		renderer.renderStatic(stack, ItemTransforms.TransformType.FIXED, intA, OverlayTexture.NO_OVERLAY, matrix, buffer, renderSeed);
		
		

		matrix.popPose();
	}
}
