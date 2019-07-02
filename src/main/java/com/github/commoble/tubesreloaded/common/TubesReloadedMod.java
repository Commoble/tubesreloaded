package com.github.commoble.tubesreloaded.common;

import net.minecraftforge.fml.common.Mod;

//The value here should match an entry in the META-INF/mods.toml file
@Mod(TubesReloadedMod.MODID)
public class TubesReloadedMod
{
	public static final String MODID = "tubesreloaded";
    public static final String VERSION = "1.0.0.0";
    public static final String NAME="Tubes Reloaded";
    
    //public static final IProxy PROXY = DistExecutor.runForDist( () -> () -> new CombinedClientProxy(), () -> () -> new DedicatedServerProxy() );

	//public static int modEntityID = 0;
	
	public TubesReloadedMod()
	{
	}
}