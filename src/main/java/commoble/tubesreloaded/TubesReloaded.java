package commoble.tubesreloaded;

import commoble.databuddy.config.ConfigHelper;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

//The value here should match an entry in the META-INF/mods.toml file
@Mod(TubesReloaded.MODID)
public class TubesReloaded
{
	public static final String MODID = "tubesreloaded";
	
	public static ServerConfig serverConfig;
	
	public TubesReloaded()
	{
		serverConfig = ConfigHelper.register(
			ModLoadingContext.get(), FMLJavaModLoadingContext.get(),
			ModConfig.Type.SERVER, ServerConfig::new);
	}
}