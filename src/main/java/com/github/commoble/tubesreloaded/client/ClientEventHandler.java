package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.common.tube.TubeTileEntity;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TubesReloadedMod.MODID, bus=Bus.MOD)
public class ClientEventHandler
{
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TubeTileEntity.class, new TubeTileEntityRenderer());
	}

}
