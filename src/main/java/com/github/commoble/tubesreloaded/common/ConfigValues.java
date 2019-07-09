package com.github.commoble.tubesreloaded.common;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;

// I made heavy reference to GigaHerz's ToolBelt mod's config implementation while I was making this
// https://github.com/gigaherz/ToolBelt/blob/master/LICENSE.txt

@Mod.EventBusSubscriber(modid=TubesReloadedMod.MODID, bus = Bus.MOD)
public class ConfigValues
{
	// the layer between the values we want to reference in java and the actual config file somewhere
	public static final ServerConfig SERVER;	// the thing that holds data from the config file
	public static final ForgeConfigSpec SERVER_SPEC;	// used by the event handler to make sure the given config file is our config file
	
	// the actual values our java will reference
	public static int soft_tube_cap = 100;
	public static int hard_tube_cap = 200;
	public static int ticks_in_tube = 10; 
	
	// config the configs
	static
	{
		final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SERVER_SPEC = specPair.getRight();
		SERVER = specPair.getLeft();
	}
	
	// event for refreshing the config data
	@SubscribeEvent
	public static void onConfigEvent(ModConfigEvent event)
	{
		ModConfig config = event.getConfig();
		if (config.getSpec() == SERVER_SPEC)
		{
			refreshServer();
		}
	}
	
	public static class ServerConfig
	{
		public final ForgeConfigSpec.IntValue soft_tube_cap;
		public final ForgeConfigSpec.IntValue hard_tube_cap;
		public final ForgeConfigSpec.IntValue ticks_in_tube;
		
		ServerConfig(ForgeConfigSpec.Builder builder)
		{
			builder.push("general");
			soft_tube_cap = builder
					.comment("Soft cap on how many tubes can exist in a contiguous network of tubes. Items are transported slowlier in networks of greater size than this value.")
					.translation("tubesreloaded.config.soft_tube_cap")
					.defineInRange("soft_tube_cap", 100, 1, 10000);
			hard_tube_cap = builder
					.comment("Hard cap on how many tubes can exist in a contiguous network of tubes. If a player attempts to make a network of greater size from this value, not all tubes in the attempted network will become part of that network.")
					.translation("tubesreloaded.config.hard_tube_cap")
					.defineInRange("hard_tube_cap", 200, 1, 10000);
			ticks_in_tube = builder
					.comment("Base time in ticks that a moving itemstack spends in each individual tube block. Adjusted by other factors.")
					.translation("tubesreloaded.config.ticks_in_tube")
					.defineInRange("ticks_in_tube", 10, 1, 72000);
			builder.pop();
		}
	}
	
	public static void refreshServer()
	{
		soft_tube_cap = SERVER.soft_tube_cap.get();
		hard_tube_cap = SERVER.hard_tube_cap.get();
	}
}
