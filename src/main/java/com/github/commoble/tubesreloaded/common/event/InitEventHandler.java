package com.github.commoble.tubesreloaded.common.event;

import com.github.commoble.tubesreloaded.common.ConfigValues;
import com.github.commoble.tubesreloaded.common.TubesReloadedMod;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = TubesReloadedMod.MODID, bus=Bus.MOD)
public class InitEventHandler
{
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event)
	{
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigValues.SERVER_SPEC);
	}
}
