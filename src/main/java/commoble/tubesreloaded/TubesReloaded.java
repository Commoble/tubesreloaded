package commoble.tubesreloaded;

import commoble.tubesreloaded.util.ConfigHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

//The value here should match an entry in the META-INF/mods.toml file
@Mod(TubesReloaded.MODID)
public class TubesReloaded
{
	public static final String MODID = "tubesreloaded";
	
	public static ServerConfig serverConfig;
	
	public TubesReloaded()
	{
		serverConfig = ConfigHelper.register(ModConfig.Type.SERVER, ServerConfig::new);
	}
}