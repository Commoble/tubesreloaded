package com.github.commoble.tubesreloaded.common.event;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;
import com.github.commoble.tubesreloaded.network.IsWasSprintPacket;
import com.github.commoble.tubesreloaded.network.PacketHandler;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = TubesReloadedMod.MODID, bus=Bus.MOD)
public class InitEventHandler
{
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event)
	{		
		// register packets
		int packetID=0;
		PacketHandler.INSTANCE.registerMessage(packetID++,
				IsWasSprintPacket.class,
				IsWasSprintPacket::encode,
				IsWasSprintPacket::decode,
				IsWasSprintPacket::whenThisPacketIsReceived
				);
	}
}
