package com.github.commoble.tubesreloaded.client;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(value= Dist.CLIENT, modid = TubesReloadedMod.MODID, bus=Bus.FORGE)
public class InputEventHandler
{
	@SubscribeEvent
	public static void onKeyboardEvent(KeyInputEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		
		if (mc.player != null)
		{
			boolean sprintIsDown = mc.gameSettings.keyBindSprint.isKeyDown();
			boolean sprintWasDown = ClientData.INSTANCE.map(client -> client.getWasSprinting(mc.player)).orElse(false);
			if (sprintWasDown != sprintIsDown)	// change in sprint key detected
			{
				ClientData.INSTANCE.ifPresent(instance -> instance.setIsSprintingAndNotifyServer(mc.player, sprintIsDown));
			}
		}
	}
}
