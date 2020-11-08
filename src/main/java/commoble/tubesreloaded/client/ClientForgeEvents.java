package commoble.tubesreloaded.client;

import commoble.tubesreloaded.ClientProxy;
import commoble.tubesreloaded.TubesReloaded;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(value= Dist.CLIENT, modid = TubesReloaded.MODID, bus=Bus.FORGE)
public class ClientForgeEvents
{
	@SubscribeEvent
	public static void onKeyboardEvent(KeyInputEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		
		if (mc.player != null)
		{
			boolean sprintIsDown = mc.gameSettings.keyBindSprint.isKeyDown();
			boolean sprintWasDown = ClientProxy.INSTANCE.map(client -> client.getWasSprinting()).orElse(false);
			if (sprintWasDown != sprintIsDown)	// change in sprint key detected
			{
				ClientProxy.INSTANCE.ifPresent(instance -> instance.setIsSprintingAndNotifyServer(sprintIsDown));
			}
		}
	}
	
	@SubscribeEvent
	public static void onClientLogIn(ClientPlayerNetworkEvent.LoggedInEvent event)
	{
		// clean up static data on the client
		ClientProxy.INSTANCE = ClientProxy.makeClientProxy();
	}

	@SubscribeEvent
	public static void onClientLogOut(ClientPlayerNetworkEvent.LoggedOutEvent event)
	{
		// clean up static data on the client
		ClientProxy.INSTANCE = ClientProxy.makeClientProxy();
	}
}
