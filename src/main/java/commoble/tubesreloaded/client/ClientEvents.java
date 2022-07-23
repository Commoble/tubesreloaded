package commoble.tubesreloaded.client;

import commoble.tubesreloaded.ClientProxy;
import commoble.tubesreloaded.TubesReloaded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents
{
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientEvents::onClientSetup);
		modBus.addListener(ClientEvents::onRegisterRenderers);
		
		forgeBus.addListener(ClientEvents::onClientTick);
		forgeBus.addListener(ClientEvents::onClientLogIn);
		forgeBus.addListener(ClientEvents::onClientLogOut);
	}
	
	private static void onClientSetup(FMLClientSetupEvent event)
	{
		// run not-thread-safe stuff on the main thread
		event.enqueueWork(ClientEvents::afterClientSetup);
	}
	
	// run not-thread-safe stuff on the main thread
	private static void afterClientSetup()
	{
		// register screens
		MenuScreens.register(TubesReloaded.get().loaderMenu.get(), StandardSizeContainerScreenFactory.of(
			new ResourceLocation(TubesReloaded.MODID, "textures/gui/loader.png"), TubesReloaded.get().loaderBlock.get().getDescriptionId()));
		MenuScreens.register(TubesReloaded.get().filterMenu.get(), StandardSizeContainerScreenFactory.of(
			new ResourceLocation(TubesReloaded.MODID, "textures/gui/filter.png"), TubesReloaded.get().filterBlock.get().getDescriptionId()));
	}
	
	private static void onRegisterRenderers(RegisterRenderers event)
	{
		// register TE renderers
		BlockEntityRenderers.register(TubesReloaded.get().tubeEntity.get(), TubeBlockEntityRenderer::new);
		BlockEntityRenderers.register(TubesReloaded.get().redstoneTubeEntity.get(), TubeBlockEntityRenderer::new);
		BlockEntityRenderers.register(TubesReloaded.get().filterEntity.get(), FilterTileEntityRenderer::new);
		BlockEntityRenderers.register(TubesReloaded.get().osmosisFilterEntity.get(), OsmosisFilterTileEntityRenderer::new);
	}
	
	private static void onClientTick(ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		
		if (mc.player != null)
		{
			boolean sprintIsDown = mc.options.keySprint.isDown();
			boolean sprintWasDown = ClientProxy.getWasSprinting();
			if (sprintWasDown != sprintIsDown)	// change in sprint key detected
			{
				ClientProxy.setIsSprintingAndNotifyServer(sprintIsDown);
			}
		}
	}
	
	private static void onClientLogIn(ClientPlayerNetworkEvent.LoggingIn event)
	{
		// clean up static data on the client
		ClientProxy.reset();
	}

	private static void onClientLogOut(ClientPlayerNetworkEvent.LoggingOut event)
	{
		// clean up static data on the client
		ClientProxy.reset();
	}
}
