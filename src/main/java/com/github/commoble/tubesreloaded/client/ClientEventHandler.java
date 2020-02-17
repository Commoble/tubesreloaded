package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.registry.BlockRegistrar;
import com.github.commoble.tubesreloaded.common.registry.TileEntityRegistrar;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value= {Dist.CLIENT}, modid = TubesReloadedMod.MODID, bus=Bus.MOD)
public class ClientEventHandler
{
//	public static final ResourceLocation SLIME_MODEL = new ResourceLocation(TubesReloadedMod.MODID, "block/osmosis_slime");
//	public static IBakedModel BAKED_SLIME_MODEL = null;
//	
//	@SubscribeEvent
//	public static void onModelRegistryEvent(ModelRegistryEvent event)
//	{
//		ModelLoader.addSpecialModel(SLIME_MODEL);
//	}
//	
//	@SubscribeEvent
//	public static void onModelBakeEvent(ModelBakeEvent event)
//	{
//		boolean contains = event.getModelRegistry().containsKey(SLIME_MODEL);
//		BAKED_SLIME_MODEL = event.getModelRegistry().get(SLIME_MODEL);
//		boolean rob = event.getModelRegistry().containsKey(SLIME_MODEL);
//		ModelManager reg = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelManager();
//		IBakedModel missing = reg.getMissingModel();
//		boolean quorn = BAKED_SLIME_MODEL == missing;
//		int x = 1;
//		IBakedModel slime = BAKED_SLIME_MODEL;
//		int y = 2;
//		
//	}
	
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		// set render types
		RenderTypeLookup.setRenderLayer(BlockRegistrar.TUBE, RenderType.cutout());	// cutout
		RenderTypeLookup.setRenderLayer(BlockRegistrar.OSMOSIS_SLIME, RenderType.translucent());
		// register TE renderers
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.TUBE, TubeTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.REDSTONE_TUBE, TubeTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.FILTER, FilterTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.OSMOSIS_FILTER, OsmosisFilterTileEntityRenderer::new);
	}
}
