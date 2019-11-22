package com.github.commoble.tubesreloaded.client;

import java.util.Random;

import com.github.commoble.tubesreloaded.common.ConfigValues;
import com.github.commoble.tubesreloaded.common.blocks.filter.FilterTileEntity;
import com.github.commoble.tubesreloaded.common.blocks.filter.OsmosisFilterBlock;
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
		BlockState renderState = BlockRegistrar.OSMOSIS_SLIME.getDefaultState();
		{
			long transferhash = blockpos.hashCode();
			int rate = ConfigValues.osmosis_filter_transfer_rate;
			double ticks = te.getWorld().getDayTime() + transferhash + partialTicks;
			double minZScale = 0.2D;
			double zScale = minZScale + (te.getBlockState().get(OsmosisFilterBlock.TRANSFERRING_ITEMS)
				? (-Math.cos(2*Math.PI * ticks / rate) + 1D) * 0.25D
				: 0D);
			double zTranslateFactor = 1D - zScale;
//			double zScaleFactor = zScale*zScale/8D - 7D*zScale/8D + 3D/4D;
//			double zOffset = (zScale * zScale - 7D*zScale + 6D)/64D;
			GlStateManager.pushMatrix();
			GlStateManager.translated(0, 0, z*zTranslateFactor + 0.125D);
			GlStateManager.scaled(0.999D,0.999D,zScale);
			
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
			bufferbuilder.setTranslation(x - blockpos.getX() + 0.000D, y - blockpos.getY()+ 0.000D, z - blockpos.getZ() + 0.000D);
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
		// // the client setup event runs before the model registry event
		// // so we can't cache this when the renderer is instantiated
		// // (unless we bind the renderer in a different event?)
		//// final IBakedModel BAKED_SLIME_MODEL = ClientEventHandler.BAKED_SLIME_MODEL;
		//
		// int i = 1;
		//// GlStateManager.texParameter(3553, 10242, 10497);
		//// GlStateManager.texParameter(3553, 10243, 10497);
		//// GlStateManager.disableLighting();
		//// GlStateManager.disableCull();
		//// GlStateManager.disableBlend();
		//// GlStateManager.depthMask(true);
		// this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		//
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.disableLighting();
		// GlStateManager.enableBlend();
		// GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
		// GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
		// GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		//
		// Tessellator tessellator = Tessellator.getInstance();
		// BufferBuilder bufferbuilder = tessellator.getBuffer();
		//
		// bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
		//
		// GlStateManager.translated(x + 0.5D, y+1D, z + 0.5D);
		// BlockRendererDispatcher blockrendererdispatcher =
		// Minecraft.getInstance().getBlockRendererDispatcher();
		// World world = te.getWorld();
		// BlockState state = te.getBlockState();
		// DirectionProperty FACING = BlockStateProperties.FACING;
		// blockrendererdispatcher.getBlockModelRenderer().renderModel(world,
		// blockrendererdispatcher.getModelForState(BlockRegistrar.OSMOSIS_SLIME.getDefaultState().with(FACING,
		// state.get(FACING))),
		// state,
		// te.getPos(),
		// bufferbuilder,
		// false, // "checkSides"
		// world.rand,
		// world.getSeed(),
		// EmptyModelData.INSTANCE);
		// tessellator.draw();
		//
		// GlStateManager.enableLighting();
		// GlStateManager.disableBlend();
		// GlStateManager.popMatrix();
	}

	private boolean renderStateModel(BlockPos pos, BlockState state, BufferBuilder buffer, World p_188186_4_, boolean checkSides)
	{
		if (this.blockRenderer == null)
			this.blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		return this.blockRenderer.getBlockModelRenderer().renderModel(p_188186_4_, this.blockRenderer.getModelForState(state), state, pos, buffer, checkSides, new Random(),
			state.getPositionRandom(pos));
	}
}
