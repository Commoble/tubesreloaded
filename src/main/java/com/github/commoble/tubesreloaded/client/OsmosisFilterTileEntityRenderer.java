package com.github.commoble.tubesreloaded.client;

import java.util.Random;

import com.github.commoble.tubesreloaded.common.ConfigValues;
import com.github.commoble.tubesreloaded.common.blocks.filter.FilterTileEntity;
import com.github.commoble.tubesreloaded.common.blocks.filter.OsmosisFilterBlock;
import com.github.commoble.tubesreloaded.common.blocks.filter.OsmosisSlimeBlock;
import com.github.commoble.tubesreloaded.common.registry.BlockRegistrar;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OsmosisFilterTileEntityRenderer extends FilterTileEntityRenderer
{
	BlockRendererDispatcher blockRenderer = null;

	@Override
	public void render(FilterTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		super.render(te, x, y, z, partialTicks, destroyStage);
		this.renderSlime(te, x, y, z, partialTicks);
	}

	private void renderSlime(FilterTileEntity te, double x, double y, double z, float partialTicks)
	{
		BlockPos blockpos = te.getPos();
		BlockState filterState = te.getBlockState();
		Direction dir = filterState.get(OsmosisFilterBlock.FACING);
		BlockState renderState = BlockRegistrar.OSMOSIS_SLIME.getDefaultState().with(OsmosisSlimeBlock.FACING, dir);
		long transferhash = blockpos.hashCode();
		int rate = ConfigValues.osmosis_filter_transfer_rate;
		double ticks = te.getWorld().getDayTime() + transferhash + partialTicks;
		double minScale = 0.25D;
		double lengthScale = minScale + (te.getBlockState().get(OsmosisFilterBlock.TRANSFERRING_ITEMS)
			? (-Math.cos(2 * Math.PI * ticks / rate) + 1D) * 0.25D
			: 0D);
		double lengthTranslateFactor = 1D - lengthScale;
		
		
		
		

		double zFightFix = 0.9999D;
		
		int dirOffsetX = dir.getXOffset();
		int dirOffsetY = dir.getYOffset();
		int dirOffsetZ = dir.getZOffset();
		
		double scaleX = dirOffsetX == 0 ? zFightFix : lengthScale;
		double scaleY = dirOffsetY == 0 ? zFightFix : lengthScale;
		double scaleZ = dirOffsetZ == 0 ? zFightFix : lengthScale;
		
		int translateFactorX = dirOffsetX == 0 ? 0 : 1;
		int translateFactorY = dirOffsetY == 0 ? 0 : 1;
		int translateFactorZ = dirOffsetZ == 0 ? 0 : 1;
		
		double tX = dirOffsetX > 0 ? x+1D : x;
		double tY = dirOffsetY > 0 ? y+1D : y;
		double tZ = dirOffsetZ > 0 ? z+1D : z;
		
		double translateX = translateFactorX * (tX * lengthTranslateFactor - 0.125D*dirOffsetX);
		double translateY = translateFactorY * (tY * lengthTranslateFactor - 0.125D*dirOffsetY);
		double translateZ = translateFactorZ * (tZ * lengthTranslateFactor - 0.125D*dirOffsetZ);
		
		GlStateManager.pushMatrix();
		//GlStateManager.translated(0, 0, z * lengthTranslateFactor + 0.125D);
		GlStateManager.translated(translateX, translateY, translateZ);
		GlStateManager.scaled(scaleX, scaleY, scaleZ);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if (Minecraft.isAmbientOcclusionEnabled())
		{
			GlStateManager.shadeModel(7425);
		} else
		{
			GlStateManager.shadeModel(7424);
		}

		BlockModelRenderer.enableCache();
		bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
		bufferbuilder.setTranslation(x - blockpos.getX(), y - blockpos.getY(), z - blockpos.getZ());
		World world = this.getWorld();
		{
			this.renderStateModel(blockpos, renderState, bufferbuilder, world, false);
		}

		bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		BlockModelRenderer.disableCache();
		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}

	private boolean renderStateModel(BlockPos pos, BlockState state, BufferBuilder buffer, World p_188186_4_, boolean checkSides)
	{
		if (this.blockRenderer == null)
			this.blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		return this.blockRenderer.getBlockModelRenderer().renderModel(p_188186_4_, this.blockRenderer.getModelForState(state), state, pos, buffer, checkSides, new Random(),
			state.getPositionRandom(pos));
	}
}
