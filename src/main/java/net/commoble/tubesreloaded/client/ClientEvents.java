package net.commoble.tubesreloaded.client;

import net.commoble.tubesreloaded.ClientProxy;
import net.commoble.tubesreloaded.TubesReloaded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.TickEvent.ClientTickEvent;

public class ClientEvents
{
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientEvents::onRegisterScreens);
		modBus.addListener(ClientEvents::onRegisterRenderers);
		
		forgeBus.addListener(ClientEvents::onClientTick);
		forgeBus.addListener(ClientEvents::onClientLogIn);
		forgeBus.addListener(ClientEvents::onClientLogOut);
	}
	
	private static void onRegisterScreens(RegisterMenuScreensEvent event)
	{
		// register screens
		event.register(TubesReloaded.get().loaderMenu.get(), StandardSizeContainerScreenFactory.of(
			new ResourceLocation(TubesReloaded.MODID, "textures/gui/loader.png"), TubesReloaded.get().loaderBlock.get().getDescriptionId()));
		event.register(TubesReloaded.get().filterMenu.get(), StandardSizeContainerScreenFactory.of(
			new ResourceLocation(TubesReloaded.MODID, "textures/gui/filter.png"), TubesReloaded.get().filterBlock.get().getDescriptionId()));
		event.register(TubesReloaded.get().multiFilterMenu.get(), StandardSizeContainerScreenFactory.of(
			new ResourceLocation("textures/gui/container/shulker_box.png"), TubesReloaded.get().multiFilterBlock.get().getDescriptionId()));
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
