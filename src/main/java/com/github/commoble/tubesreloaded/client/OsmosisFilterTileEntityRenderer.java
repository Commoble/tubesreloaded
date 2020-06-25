package com.github.commoble.tubesreloaded.client;

import java.util.Random;

import com.github.commoble.tubesreloaded.TubesReloaded;
import com.github.commoble.tubesreloaded.blocks.filter.FilterTileEntity;
import com.github.commoble.tubesreloaded.blocks.filter.OsmosisFilterBlock;
import com.github.commoble.tubesreloaded.blocks.filter.OsmosisSlimeBlock;
import com.github.commoble.tubesreloaded.registry.BlockRegistrar;
import com.github.commoble.tubesreloaded.registry.ItemRegistrar;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class OsmosisFilterTileEntityRenderer extends FilterTileEntityRenderer
{
	public final ItemStack SLIME_STACK;
	
	public OsmosisFilterTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_)
	{
		super(p_i226006_1_);
		this.SLIME_STACK = new ItemStack(ItemRegistrar.OSMOSIS_SLIME);
	}

	@Override
	public void render(FilterTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int intA, int intB)
	{
		this.renderSlime(te, partialTicks, matrix, buffer, intA, intB);
		super.render(te, partialTicks, matrix, buffer, intA, intB);
	}

	private void renderSlime(FilterTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int intA, int intB)
	{
		BlockPos blockpos = te.getPos();
		BlockState filterState = te.getBlockState();
		Direction dir = filterState.get(OsmosisFilterBlock.FACING);
		BlockState renderState = BlockRegistrar.OSMOSIS_SLIME.getDefaultState().with(OsmosisSlimeBlock.FACING, dir);
		long transferhash = blockpos.hashCode();
		int rate = TubesReloaded.serverConfig.osmosis_filter_transfer_rate.get();
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
		
		double tX = dirOffsetX > 0 ? 1D : 0D;
		double tY = dirOffsetY > 0 ? 1D : 0D;
		double tZ = dirOffsetZ > 0 ? 1D : 0D;
		
		double translateX = translateFactorX * (tX * lengthTranslateFactor - 0.125D*dirOffsetX);
		double translateY = translateFactorY * (tY * lengthTranslateFactor - 0.125D*dirOffsetY);
		double translateZ = translateFactorZ * (tZ * lengthTranslateFactor - 0.125D*dirOffsetZ);
		
		matrix.push();	// push
		matrix.translate(translateX, translateY, translateZ);
		matrix.scale(scaleX, scaleY, scaleZ);
		RenderType renderType = RenderTypeLookup.getRenderType(renderState);
		net.minecraftforge.client.ForgeHooksClient.setRenderLayer(renderType);
		
		BlockRendererDispatcher blockDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		blockDispatcher.getBlockModelRenderer().renderModel(
      	  te.getWorld(),
      	  blockDispatcher.getModelForState(renderState),
      	  renderState,
      	  blockpos,
      	  matrix,
      	  buffer.getBuffer(renderType),
      	  false,
      	  new Random(),
      	  renderState.getPositionRandom(blockpos),
      	  OverlayTexture.DEFAULT_LIGHT,
      	  net.minecraftforge.client.model.data.EmptyModelData.INSTANCE
      	 );
		matrix.pop();
	}

//	private boolean renderStateModel(BlockPos pos, BlockState state, BufferBuilder buffer, World p_188186_4_, boolean checkSides)
//	{
//		if (this.blockRenderer == null)
//			this.blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
//		return this.blockRenderer.getBlockModelRenderer().renderModel(p_188186_4_, this.blockRenderer.getModelForState(state), state, pos, buffer, checkSides, new Random(),
//			state.getPositionRandom(pos));
//	}
}
