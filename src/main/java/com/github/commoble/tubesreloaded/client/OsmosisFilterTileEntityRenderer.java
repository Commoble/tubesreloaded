package com.github.commoble.tubesreloaded.client;

import java.util.Random;

import com.github.commoble.tubesreloaded.common.ConfigValues;
import com.github.commoble.tubesreloaded.common.blocks.filter.FilterTileEntity;
import com.github.commoble.tubesreloaded.common.blocks.filter.OsmosisFilterBlock;
import com.github.commoble.tubesreloaded.common.blocks.filter.OsmosisSlimeBlock;
import com.github.commoble.tubesreloaded.common.registry.BlockRegistrar;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class OsmosisFilterTileEntityRenderer extends FilterTileEntityRenderer
{
	public OsmosisFilterTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_)
	{
		super(p_i226006_1_);
	}

	@Override
	public void func_225616_a_(FilterTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int intA, int intB)
	{
		super.func_225616_a_(te, partialTicks, matrix, buffer, intA, intB);
		this.renderSlime(te, partialTicks, matrix, buffer);
	}

	private void renderSlime(FilterTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer)
	{
		BlockPos blockpos = te.getPos();
		int x = blockpos.getX();
		int y = blockpos.getY();
		int z = blockpos.getZ();
		BlockState filterState = te.getBlockState();
		Direction dir = filterState.get(OsmosisFilterBlock.FACING);
		BlockState renderState = BlockRegistrar.OSMOSIS_SLIME.getDefaultState().with(OsmosisSlimeBlock.FACING, dir);
		long transferhash = blockpos.hashCode();
		int rate = ConfigValues.osmosis_filter_transfer_rate;
		double ticks = te.getWorld().getGameTime() + transferhash + partialTicks;
		double minScale = 0.25D;
		double lengthScale = minScale + (te.getBlockState().get(OsmosisFilterBlock.TRANSFERRING_ITEMS)
			? (-Math.cos(2 * Math.PI * ticks / rate) + 1D) * 0.25D
			: 0D);
		double lengthTranslateFactor = 1D - lengthScale;
		
		
		
		

		double zFightFix = 0.9999D;
		
		int dirOffsetX = dir.getXOffset();
		int dirOffsetY = dir.getYOffset();
		int dirOffsetZ = dir.getZOffset();
		
		float scaleX = (float) (dirOffsetX == 0 ? zFightFix : lengthScale);
		float scaleY = (float) (dirOffsetY == 0 ? zFightFix : lengthScale);
		float scaleZ = (float) (dirOffsetZ == 0 ? zFightFix : lengthScale);
		
		int translateFactorX = dirOffsetX == 0 ? 0 : 1;
		int translateFactorY = dirOffsetY == 0 ? 0 : 1;
		int translateFactorZ = dirOffsetZ == 0 ? 0 : 1;
		
		double tX = dirOffsetX > 0 ? x+1D : x;
		double tY = dirOffsetY > 0 ? y+1D : y;
		double tZ = dirOffsetZ > 0 ? z+1D : z;
		
		double translateX = translateFactorX * (tX * lengthTranslateFactor - 0.125D*dirOffsetX);
		double translateY = translateFactorY * (tY * lengthTranslateFactor - 0.125D*dirOffsetY);
		double translateZ = translateFactorZ * (tZ * lengthTranslateFactor - 0.125D*dirOffsetZ);
		
		matrix.func_227860_a_();	// push
		//GlStateManager.translated(0, 0, z * lengthTranslateFactor + 0.125D);
		matrix.func_227861_a_(translateX, translateY, translateZ);	// translate
		matrix.func_227862_a_(scaleX, scaleY, scaleZ);	// scale
		
//        matrix.func_227861_a_(-0.5D, 0.0D, -0.5D);
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        for (net.minecraft.client.renderer.RenderType type : net.minecraft.client.renderer.RenderType.func_228661_n_()) {
           if (RenderTypeLookup.canRenderInLayer(renderState, type)) {
              net.minecraftforge.client.ForgeHooksClient.setRenderLayer(type);
              blockrendererdispatcher.getBlockModelRenderer().func_228802_a_(
            	  te.getWorld(),
            	  blockrendererdispatcher.getModelForState(renderState),
            	  renderState,
            	  blockpos,
            	  matrix,
            	  buffer.getBuffer(type),
            	  false,
            	  new Random(),
            	  renderState.getPositionRandom(blockpos),
            	  OverlayTexture.field_229196_a_);
           }
        }
        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);

//		Tessellator tessellator = Tessellator.getInstance();
//		BufferBuilder bufferbuilder = tessellator.getBuffer();
//		this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//		RenderHelper.disableStandardItemLighting();
//		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//		GlStateManager.enableBlend();
//		GlStateManager.disableCull();
//		if (Minecraft.isAmbientOcclusionEnabled())
//		{
//			GlStateManager.shadeModel(7425);
//		} else
//		{
//			GlStateManager.shadeModel(7424);
//		}
//
//		BlockModelRenderer.enableCache();
//		bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
//		bufferbuilder.setTranslation(x - blockpos.getX(), y - blockpos.getY(), z - blockpos.getZ());
//		World world = this.getWorld();
//		{
//			this.renderStateModel(blockpos, renderState, bufferbuilder, world, false);
//		}
//
//		bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
//		tessellator.draw();
//		BlockModelRenderer.disableCache();
//		RenderHelper.enableStandardItemLighting();

		matrix.func_227865_b_();	// pop
	}

//	private boolean renderStateModel(BlockPos pos, BlockState state, BufferBuilder buffer, World p_188186_4_, boolean checkSides)
//	{
//		if (this.blockRenderer == null)
//			this.blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
//		return this.blockRenderer.getBlockModelRenderer().renderModel(p_188186_4_, this.blockRenderer.getModelForState(state), state, pos, buffer, checkSides, new Random(),
//			state.getPositionRandom(pos));
//	}
}
