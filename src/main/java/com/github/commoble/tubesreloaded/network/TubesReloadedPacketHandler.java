package com.github.commoble.tubesreloaded.network;

import com.github.commoble.tubesreloaded.common.TubesReloadedMod;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class TubesReloadedPacketHandler
{
	private static final String PROTOCOL_VERSION = "1";
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(TubesReloadedMod.MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	
	static
	{
		int messageID = 0;
		INSTANCE.registerMessage(messageID++,
				TubePathPacket.class,
				TubePathPacket::encode,
				TubePathPacket::decode,
				TubePathPacket::handle);
	}
	
}
