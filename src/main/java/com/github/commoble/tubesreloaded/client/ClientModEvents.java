package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.TubesReloaded;
import com.github.commoble.tubesreloaded.registry.BlockRegistrar;
import com.github.commoble.tubesreloaded.registry.ContainerRegistrar;
import com.github.commoble.tubesreloaded.registry.TileEntityRegistrar;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value= {Dist.CLIENT}, modid = TubesReloaded.MODID, bus=Bus.MOD)
public class ClientModEvents
{
	
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		// set render types
		RenderTypeLookup.setRenderLayer(BlockRegistrar.TUBE, RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BlockRegistrar.SHUNT, RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BlockRegistrar.LOADER, RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BlockRegistrar.EXTRACTOR, RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BlockRegistrar.FILTER, RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BlockRegistrar.DISTRIBUTOR, RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BlockRegistrar.OSMOSIS_SLIME, RenderType.translucent());
		// register TE renderers
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.TUBE, TubeTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.REDSTONE_TUBE, TubeTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.FILTER, FilterTileEntityRenderer::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.OSMOSIS_FILTER, OsmosisFilterTileEntityRenderer::new);
		// register screens
		ScreenManager.registerFactory(ContainerRegistrar.LOADER, StandardSizeContainerScreenFactory.of(
			new ResourceLocation(TubesReloaded.MODID, "textures/gui/loader.png"), BlockRegistrar.LOADER.getTranslationKey()));
		ScreenManager.registerFactory(ContainerRegistrar.FILTER, StandardSizeContainerScreenFactory.of(
			new ResourceLocation(TubesReloaded.MODID, "textures/gui/filter.png"), BlockRegistrar.FILTER.getTranslationKey()));
	}
}
