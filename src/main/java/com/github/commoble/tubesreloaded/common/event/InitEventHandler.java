package com.github.commoble.tubesreloaded.common.event;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber(modid = TubesReloadedMod.MODID, bus=Bus.MOD)
public class InitEventHandler
{
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event)
	{
		//TubesReloadedPacketHandler.registerMessages();
	}
	
	@SubscribeEvent
	public static void onLoadComplete(FMLLoadCompleteEvent event)
	{

	}
}
