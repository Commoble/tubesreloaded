package com.github.commoble.tubesreloaded;

import com.github.commoble.tubesreloaded.util.ConfigHelper;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

//The value here should match an entry in the META-INF/mods.toml file
@Mod(TubesReloadedMod.MODID)
public class TubesReloadedMod
{
	public static final String MODID = "tubesreloaded";
	
	public static ConfigValues config;
	
	public TubesReloadedMod()
	{
		config = ConfigHelper.register(ModConfig.Type.SERVER, ConfigValues::new);
	}
}