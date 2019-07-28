package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.blocks.filter.FilterTileEntity;
import com.github.commoble.tubesreloaded.common.blocks.tube.TubeTileEntity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value= {Dist.CLIENT}, modid = TubesReloadedMod.MODID, bus=Bus.MOD)
public class ClientEventHandler
{
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		// register TE renderers
		ClientRegistry.bindTileEntitySpecialRenderer(TubeTileEntity.class, new TubeTileEntityRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(FilterTileEntity.class, new FilterTileEntityRenderer());
	}
}
